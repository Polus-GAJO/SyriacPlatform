Attribute VB_Name = "modTranslationPermanentDelete"
Option Compare Database
Option Explicit

' Requires Enforce Referential Integrity and Cascade Delete Related Records
' on Translations.TranslationID -> TranslationLinks.TranslationID.
' Run InstallTranslationPermanentDelete once with frmTextTranslations closed.
' Back up the database first. The action permanently deletes a shared translation.

Private Const FORM_NAME As String = "frmTextTranslations"

Public Sub InstallTranslationPermanentDelete()
    Dim f As Form
    Dim c As Control
    Dim obj As Control
    Dim topPos As Long
    Dim msg As String
    On Error GoTo Failed

    If CurrentProject.AllForms(FORM_NAME).IsLoaded Then
        MsgBox "Close frmTextTranslations before installation.", vbExclamation
        Exit Sub
    End If

    DoCmd.OpenForm FORM_NAME, acDesign, , , , acHidden
    Set f = Forms(FORM_NAME)
    For Each obj In f.Controls
        If StrComp(obj.Name, "cmdDeleteTranslationPermanent", vbTextCompare) = 0 Then
            DoCmd.Close acForm, FORM_NAME, acSaveNo
            MsgBox "Permanent delete button is already installed.", vbInformation
            Exit Sub
        End If
    Next obj

    topPos = f.Section(acDetail).Height + 100
    f.Section(acDetail).Height = topPos + 580
    Set c = CreateControl(FORM_NAME, acCommandButton, acDetail, , , _
                          10500, topPos, 3800, 460)
    c.Name = "cmdDeleteTranslationPermanent"
    c.Caption = "Delete permanently"
    c.OnClick = "=DeleteCurrentTranslationPermanently()"

    DoCmd.Close acForm, FORM_NAME, acSaveYes
    MsgBox "Permanent delete button installed.", vbInformation
    Exit Sub
Failed:
    msg = CStr(Err.Number) & " - " & Err.Description
    On Error Resume Next
    DoCmd.Close acForm, FORM_NAME, acSaveNo
    MsgBox "Installation failed: " & msg, vbExclamation
End Sub

Public Function DeleteCurrentTranslationPermanently() As Boolean
    Dim f As Form
    Dim translationId As Long
    Dim allLinks As Long
    Dim textLinks As Long
    Dim petgomoLinks As Long
    Dim publishedLinks As Long
    Dim answer As VbMsgBoxResult
    Dim db As DAO.Database
    Dim msg As String
    On Error GoTo Failed

    Set f = Forms(FORM_NAME)
    If f.NewRecord Or IsNull(f!TranslationID) Then Exit Function
    If f.Dirty Then
        MsgBox "Save or cancel edits before deleting.", vbExclamation
        Exit Function
    End If

    translationId = CLng(f!TranslationID)
    Set db = CurrentDb

    ' Confirm the expected cascade relationship is enabled before any deletion.
    If Not HasTranslationCascade(db) Then
        MsgBox "Cascade Delete is not enabled on the relationship from " & _
               "Translations to TranslationLinks. No records were deleted.", vbCritical
        Exit Function
    End If

    allLinks = DCount("*", "TranslationLinks", "TranslationID=" & translationId)
    textLinks = DCount("*", "TranslationLinks", _
                       "TranslationID=" & translationId & " AND TextID Is Not Null")
    petgomoLinks = DCount("*", "TranslationLinks", _
                          "TranslationID=" & translationId & " AND PetN Is Not Null")
    publishedLinks = DCount("*", "TranslationLinks", _
                            "TranslationID=" & translationId & " AND IsPublished=True")

    answer = MsgBox( _
        "PERMANENTLY DELETE translation ID " & translationId & "?" & vbCrLf & vbCrLf & _
        "Links to Texts: " & textLinks & vbCrLf & _
        "Links to Petgomo: " & petgomoLinks & vbCrLf & _
        "Total links: " & allLinks & vbCrLf & _
        "Published links: " & publishedLinks & vbCrLf & vbCrLf & _
        "Access will delete this translation and ALL of its links, " & _
        "including links to other sources." & vbCrLf & _
        "This cannot be undone from the form.", _
        vbYesNo + vbCritical + vbDefaultButton2, "Delete translation permanently")
    If answer <> vbYes Then Exit Function

    ' Single DELETE. The enforced cascade handles TranslationLinks atomically.
    db.Execute "DELETE FROM Translations WHERE TranslationID=" & translationId, _
               dbFailOnError
    If db.RecordsAffected <> 1 Then
        MsgBox "No translation was deleted; the record may have changed.", vbExclamation
        Exit Function
    End If

    f.Requery
    MsgBox "Translation " & translationId & " and all its links were deleted.", vbInformation
    DeleteCurrentTranslationPermanently = True
    Exit Function
Failed:
    msg = CStr(Err.Number) & " - " & Err.Description
    MsgBox "Deletion failed: " & msg, vbCritical
End Function

Private Function HasTranslationCascade(ByVal db As DAO.Database) As Boolean
    Dim rel As DAO.Relation
    Dim field As DAO.Field
    For Each rel In db.Relations
        If StrComp(rel.Table, "Translations", vbTextCompare) = 0 And _
           StrComp(rel.ForeignTable, "TranslationLinks", vbTextCompare) = 0 Then
            For Each field In rel.Fields
                If StrComp(field.Name, "TranslationID", vbTextCompare) = 0 And _
                   StrComp(field.ForeignName, "TranslationID", vbTextCompare) = 0 Then
                    HasTranslationCascade = _
                        ((rel.Attributes And dbRelationDeleteCascade) <> 0)
                    Exit Function
                End If
            Next field
        End If
    Next rel
End Function
