Attribute VB_Name = "modTranslationDetailEditor"
Option Compare Database
Option Explicit

' Safe, bound editor for full-length translations and metadata.
' Does not alter existing forms, tables or links.
' Run InstallTranslationDetailEditor once; back up the database first.
' Use OpenTranslationDetail <TranslationID> for existing translations.
' A new translation must be created and linked through the context workflow.

Private Const EDITOR_FORM As String = "frmTranslationDetail"

Public Sub InstallTranslationDetailEditor()
    Dim f As Form
    Dim n As String
    Dim c As Control
    Dim obj As AccessObject

    For Each obj In CurrentProject.AllForms
        If StrComp(obj.Name, EDITOR_FORM, vbTextCompare) = 0 Then
            MsgBox EDITOR_FORM & " already exists; no changes made.", vbInformation
            Exit Sub
        End If
    Next obj

    Set f = CreateForm
    n = f.Name
    f.Caption = "Translation details"
    f.RecordSource = "Translations"
    f.Width = 12900
    f.Section(acDetail).Height = 8100
    f.NavigationButtons = False
    f.RecordSelectors = False
    f.AllowAdditions = False
    f.AllowDeletions = False
    f.AllowEdits = True

    DetailLabel n, "Translation ID", 300, 240
    DetailBox n, "txtTranslationID", "TranslationID", 2600, 240, 1400, 390
    f.Controls("txtTranslationID").Locked = True

    DetailLabel n, "Language", 300, 860
    Set c = CreateControl(n, acComboBox, acDetail, , "LanguageID", 2600, 860, 3800, 390)
    c.Name = "cboLanguage"
    c.RowSourceType = "Table/Query"
    c.RowSource = "SELECT LanguageID, LanguageName FROM Languages ORDER BY LanguageName"
    c.ColumnCount = 2
    c.BoundColumn = 1
    c.ColumnWidths = "0cm;5cm"
    c.LimitToList = True
    c.Locked = True ' Changing language of a shared translation requires separate workflow.

    DetailLabel n, "Translation", 300, 1560
    DetailBox n, "txtTranslation", "Translation", 2600, 1560, 9700, 2900
    f.Controls("txtTranslation").EnterKeyBehavior = True
    f.Controls("txtTranslation").ScrollBars = 2

    DetailLabel n, "Translator", 300, 4770
    DetailBox n, "txtTranslator", "Translator", 2600, 4770, 6000, 400
    DetailLabel n, "Source", 300, 5340
    DetailBox n, "txtSource", "Source", 2600, 5340, 6000, 400
    DetailLabel n, "Review status", 300, 5910

    Set c = CreateControl(n, acComboBox, acDetail, , "ReviewStatus", 2600, 5910, 2900, 400)
    c.Name = "cboReviewStatus"
    c.RowSourceType = "Value List"
    c.RowSource = "Draft;Reviewed;Approved"
    c.ColumnCount = 1
    c.LimitToList = True

    Set c = CreateControl(n, acCommandButton, acDetail, , , 2600, 6740, 2000, 500)
    c.Name = "cmdSave"
    c.Caption = "Save"
    c.OnClick = "=SaveTranslationDetail()"

    Set c = CreateControl(n, acCommandButton, acDetail, , , 4860, 6740, 2000, 500)
    c.Name = "cmdCancel"
    c.Caption = "Cancel"
    c.OnClick = "=CancelTranslationDetail()"

    DoCmd.Close acForm, n, acSaveYes
    DoCmd.Rename EDITOR_FORM, acForm, n
    MsgBox "Created " & EDITOR_FORM & ".", vbInformation
End Sub

Public Sub OpenTranslationDetail(ByVal translationId As Long)
    If translationId <= 0 Then Err.Raise vbObjectError + 5400, , "Invalid translation ID."
    If DCount("*", "Translations", "TranslationID=" & CStr(translationId)) = 0 Then _
        Err.Raise vbObjectError + 5401, , "Translation not found."
    If CurrentProject.AllForms(EDITOR_FORM).IsLoaded Then
        DoCmd.Close acForm, EDITOR_FORM, acSaveNo
    End If
    DoCmd.OpenForm EDITOR_FORM, acNormal, , "TranslationID=" & CStr(translationId), _
                   acFormEdit, acDialog
End Sub

Public Function ValidateTranslationDetail() As Boolean
    Dim f As Form
    Set f = Forms(EDITOR_FORM)
    If Len(Trim$(Nz(f!txtTranslation.Value, ""))) = 0 Then
        MsgBox "Translation cannot be empty.", vbExclamation
        ValidateTranslationDetail = False
        Exit Function
    End If
    If IsNull(f!cboReviewStatus.Value) Then
        f!cboReviewStatus.Value = "Draft"
    End If
    ValidateTranslationDetail = True
End Function

Public Function SaveTranslationDetail() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Forms(EDITOR_FORM)
    If Not ValidateTranslationDetail() Then Exit Function
    If f.Dirty Then f.Dirty = False
    DoCmd.Close acForm, EDITOR_FORM, acSaveNo
    SaveTranslationDetail = True
    Exit Function
Failed:
    MsgBox "Could not save translation: " & Err.Description, vbExclamation
End Function

Public Function CancelTranslationDetail() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Forms(EDITOR_FORM)
    If f.Dirty Then f.Undo
    DoCmd.Close acForm, EDITOR_FORM, acSaveNo
    CancelTranslationDetail = True
    Exit Function
Failed:
    MsgBox "Could not cancel: " & Err.Description, vbExclamation
End Function

Private Sub DetailLabel(ByVal n As String, ByVal title As String, _
                        ByVal x As Long, ByVal y As Long)
    Dim c As Control
    Set c = CreateControl(n, acLabel, acDetail, , , x, y, 2150, 390)
    c.Caption = title
End Sub

Private Sub DetailBox(ByVal n As String, ByVal controlName As String, _
                      ByVal fieldName As String, ByVal x As Long, _
                      ByVal y As Long, ByVal w As Long, ByVal h As Long)
    Dim c As Control
    Set c = CreateControl(n, acTextBox, acDetail, , fieldName, x, y, w, h)
    c.Name = controlName
End Sub
