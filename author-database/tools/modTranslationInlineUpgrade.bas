Attribute VB_Name = "modTranslationInlineUpgrade"
Option Compare Database
Option Explicit

' Adds inline Edit/Save/Cancel and safe unlink to frmTextTranslations.
' This is an in-place FORM DESIGN upgrade; tables and records are not altered.
' Backup the Access database, close frmTextTranslations, and run
' UpgradeInlineTranslationEditor ONCE. No existing controls are deleted.
'
' Deletion means removing the link to the CURRENT source only, never deleting
' the shared Translations record.

Private Const FORM_NAME As String = "frmTextTranslations"
Private mEditingTranslationId As Long

Public Sub UpgradeInlineTranslationEditor()
    Dim f As Form
    Dim c As Control
    On Error GoTo Failed

    If CurrentProject.AllForms(FORM_NAME).IsLoaded Then
        MsgBox "Close frmTextTranslations before upgrading.", vbExclamation
        Exit Sub
    End If
    DoCmd.OpenForm FORM_NAME, acDesign, , , , acHidden
    Set f = Forms(FORM_NAME)
    If InlineControlExists(f, "cmdSaveInline") Then
        DoCmd.Close acForm, FORM_NAME, acSaveNo
        MsgBox "Inline editor already installed. No changes made.", vbInformation
        Exit Sub
    End If

    f.AllowEdits = True ' Needed for the unbound language selector.
    f.AllowAdditions = False
    f.AllowDeletions = False
    f.Section(acDetail).Height = 1800

    ' Existing textboxes are bound; edit mode temporarily unlocks only
    ' translation-related controls. Language, IDs and publication stay locked.
    f!txtTranslation.Locked = True
    f!txtTranslator.Locked = True
    f!txtPublished.Locked = True
    f!txtTranslationID.Locked = True
    f!txtReview.Locked = True

    Set c = CreateControl(FORM_NAME, acTextBox, acDetail, , "Source", 6720, 1040, 1700, 380)
    c.Name = "txtSourceInline"
    c.Locked = True

    Set c = CreateControl(FORM_NAME, acComboBox, acDetail, , "ReviewStatus", 6720, 580, 1700, 390)
    c.Name = "cboReviewInline"
    c.RowSourceType = "Value List"
    c.RowSource = "Draft;Reviewed;Approved"
    c.LimitToList = True
    c.Locked = True
    f!txtReview.Visible = False

    Set c = CreateControl(FORM_NAME, acCommandButton, acDetail, , , 9540, 700, 1450, 450)
    c.Name = "cmdSaveInline"
    c.Caption = "Save"
    c.OnClick = "=SaveInlineTranslation()"

    Set c = CreateControl(FORM_NAME, acCommandButton, acDetail, , , 11200, 700, 1550, 450)
    c.Name = "cmdCancelInline"
    c.Caption = "Cancel"
    c.OnClick = "=CancelInlineTranslation()"

    Set c = CreateControl(FORM_NAME, acCommandButton, acDetail, , , 12900, 700, 1700, 450)
    c.Name = "cmdDeleteLink"
    c.Caption = "Remove link"
    c.OnClick = "=RemoveCurrentTranslationLink()"

    ' The existing Edit button now unlocks this form's fields.
    f!cmdEdit.OnClick = "=BeginInlineTranslationEdit()"
    f.OnCurrent = "=InlineTranslationCurrent()"
    f.OnBeforeUpdate = "=InlineTranslationBeforeUpdate()"

    DoCmd.Close acForm, FORM_NAME, acSaveYes
    MsgBox "Inline translation editor installed. Test on sample records.", vbInformation
    Exit Sub
Failed:
    Dim message As String
    message = Err.Number & " - " & Err.Description
    On Error Resume Next
    DoCmd.Close acForm, FORM_NAME, acSaveNo
    MsgBox "Upgrade failed: " & message, vbExclamation
End Sub

Public Function BeginInlineTranslationEdit() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Forms(FORM_NAME)
    If f.NewRecord Or IsNull(f!TranslationID) Then Exit Function
    mEditingTranslationId = CLng(f!TranslationID)
    SetInlineLocked f, False
    f!txtTranslation.SetFocus
    BeginInlineTranslationEdit = True
    Exit Function
Failed:
    MsgBox "Cannot edit: " & Err.Description, vbExclamation
End Function

Public Function SaveInlineTranslation() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Forms(FORM_NAME)
    If mEditingTranslationId = 0 Then Exit Function
    If f.NewRecord Or CLng(f!TranslationID) <> mEditingTranslationId Then
        Err.Raise vbObjectError + 5500, , "Selected translation changed."
    End If
    If Len(Trim$(Nz(f!txtTranslation.Value, ""))) = 0 Then
        MsgBox "Translation cannot be empty.", vbExclamation
        Exit Function
    End If
    If IsNull(f!cboReviewInline.Value) Then f!cboReviewInline.Value = "Draft"
    If f.Dirty Then f.Dirty = False
    mEditingTranslationId = 0
    SetInlineLocked f, True
    SaveInlineTranslation = True
    Exit Function
Failed:
    MsgBox "Cannot save: " & Err.Description, vbExclamation
End Function

Public Function CancelInlineTranslation() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Forms(FORM_NAME)
    If mEditingTranslationId = 0 Then Exit Function
    If f.Dirty Then f.Undo
    mEditingTranslationId = 0
    SetInlineLocked f, True
    CancelInlineTranslation = True
    Exit Function
Failed:
    MsgBox "Cannot cancel: " & Err.Description, vbExclamation
End Function

Public Function InlineTranslationCurrent() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Forms(FORM_NAME)
    ' Access saves dirty bound records before Current fires.
    ' Reset to read-only on navigation as agreed.
    mEditingTranslationId = 0
    SetInlineLocked f, True
    InlineTranslationCurrent = True
    Exit Function
Failed:
    MsgBox "Cannot reset edit mode: " & Err.Description, vbExclamation
End Function

Public Function InlineTranslationBeforeUpdate() As Boolean
    ' Validation in BeforeUpdate must cancel the event if invalid.
    ' An expression event cannot set Cancel; therefore do not attempt
    ' to reject here. The Save button performs the required validation.
    InlineTranslationBeforeUpdate = True
End Function

Public Function RemoveCurrentTranslationLink() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Dim linkId As Long
    Dim translationId As Long
    Dim linksCount As Long
    Dim answer As VbMsgBoxResult
    Set f = Forms(FORM_NAME)
    If f.NewRecord Or IsNull(f!LinkID) Then Exit Function
    If mEditingTranslationId <> 0 Then
        MsgBox "Save or cancel your edits before removing the link.", vbExclamation
        Exit Function
    End If
    linkId = CLng(f!LinkID)
    translationId = CLng(f!TranslationID)
    linksCount = DCount("*", "TranslationLinks", "TranslationID=" & translationId)
    answer = MsgBox("Remove this translation from the CURRENT text/Petgomo only?" & vbCrLf & _
        "Translation ID: " & translationId & vbCrLf & _
        "Existing links: " & linksCount & vbCrLf & _
        "The translation itself and its other links will remain.", _
        vbYesNo + vbQuestion + vbDefaultButton2, "Remove translation link")
    If answer <> vbYes Then Exit Function
    CurrentDb.Execute "DELETE FROM TranslationLinks WHERE LinkID=" & linkId, dbFailOnError
    f.Requery
    RemoveCurrentTranslationLink = True
    Exit Function
Failed:
    MsgBox "Cannot remove link: " & Err.Description, vbExclamation
End Function

Private Sub SetInlineLocked(ByVal f As Form, ByVal isLocked As Boolean)
    f!txtTranslation.Locked = isLocked
    f!txtTranslator.Locked = isLocked
    f!txtSourceInline.Locked = isLocked
    f!cboReviewInline.Locked = isLocked
End Sub

Private Function InlineControlExists(ByVal f As Form, ByVal name As String) As Boolean
    Dim c As Control
    For Each c In f.Controls
        If StrComp(c.Name, name, vbTextCompare) = 0 Then
            InlineControlExists = True
            Exit Function
        End If
    Next c
End Function
