Attribute VB_Name = "modTranslationNewInline"
Option Compare Database
Option Explicit

' Inline creation panel on frmTextTranslations (no additional forms).
' Run InstallInlineNewTranslation once after making a backup.
' The existing source and selected language are read from OpenArgs / cboLanguageFilter.
' No source IDs or language IDs are typed by the editor.

Private Const FORM_NAME As String = "frmTextTranslations"

Public Sub InstallInlineNewTranslation()
    Dim f As Form, c As Control, oldHeader As Long
    Dim msg As String
    On Error GoTo Failed
    If CurrentProject.AllForms(FORM_NAME).IsLoaded Then
        MsgBox "Close frmTextTranslations before upgrading.", vbExclamation
        Exit Sub
    End If
    DoCmd.OpenForm FORM_NAME, acDesign, , , , acHidden
    Set f = Forms(FORM_NAME)
    If HasNewControl(f, "txtNewTranslation") Then
        DoCmd.Close acForm, FORM_NAME, acSaveNo
        MsgBox "New translation panel is already installed.", vbInformation
        Exit Sub
    End If

    ' Move existing detail header content only by expanding the header.
    oldHeader = f.Section(acHeader).Height
    f.Section(acHeader).Height = oldHeader + 3300
    Set c = CreateControl(FORM_NAME, acLabel, acHeader, , , 250, oldHeader + 130, 4500, 330)
    c.Caption = "New translation (selected source and language)"
    Set c = CreateControl(FORM_NAME, acTextBox, acHeader, , , 250, oldHeader + 520, 9000, 1400)
    c.Name = "txtNewTranslation"
    c.EnterKeyBehavior = True
    c.ScrollBars = 2

    Set c = CreateControl(FORM_NAME, acTextBox, acHeader, , , 250, oldHeader + 2080, 2600, 360)
    c.Name = "txtNewTranslator"
    Set c = CreateControl(FORM_NAME, acLabel, acHeader, , , 250, oldHeader + 2460, 2600, 300)
    c.Caption = "Translator (optional)"

    Set c = CreateControl(FORM_NAME, acTextBox, acHeader, , , 3050, oldHeader + 2080, 2800, 360)
    c.Name = "txtNewSource"
    Set c = CreateControl(FORM_NAME, acLabel, acHeader, , , 3050, oldHeader + 2460, 2800, 300)
    c.Caption = "Source (optional)"

    Set c = CreateControl(FORM_NAME, acCommandButton, acHeader, , , 9600, oldHeader + 530, 2100, 480)
    c.Name = "cmdSaveNewTranslation"
    c.Caption = "Save new"
    c.OnClick = "=SaveNewInlineTranslation()"
    Set c = CreateControl(FORM_NAME, acCommandButton, acHeader, , , 9600, oldHeader + 1170, 2100, 480)
    c.Name = "cmdCancelNewTranslation"
    c.Caption = "Clear new"
    c.OnClick = "=ClearNewInlineTranslation()"

    ' Previous button used InputBox. Replace it with focus on the inline panel.
    f!cmdNewTranslation.OnClick = "=FocusNewInlineTranslation()"
    DoCmd.Close acForm, FORM_NAME, acSaveYes
    MsgBox "Inline new-translation panel installed.", vbInformation
    Exit Sub
Failed:
    msg = CStr(Err.Number) & " - " & Err.Description
    On Error Resume Next
    DoCmd.Close acForm, FORM_NAME, acSaveNo
    MsgBox "Upgrade failed: " & msg, vbExclamation
End Sub

Public Function FocusNewInlineTranslation() As Boolean
    On Error GoTo Failed
    Forms(FORM_NAME)!txtNewTranslation.SetFocus
    FocusNewInlineTranslation = True
    Exit Function
Failed:
    MsgBox Err.Description, vbExclamation
End Function

Public Function ClearNewInlineTranslation() As Boolean
    Dim f As Form
    Set f = Forms(FORM_NAME)
    f!txtNewTranslation = Null
    f!txtNewTranslator = Null
    f!txtNewSource = Null
    ClearNewInlineTranslation = True
End Function

Public Function SaveNewInlineTranslation() As Boolean
    Dim f As Form, args() As String, targetField As String
    Dim sourceId As Long, languageId As Long, newId As Long
    Dim db As DAO.Database, ws As DAO.Workspace
    Dim translations As DAO.Recordset, links As DAO.Recordset
    Dim activeTransaction As Boolean
    Dim msg As String
    On Error GoTo Failed
    Set f = Forms(FORM_NAME)
    If Len(CurrentTranslationContext()) = 0 Then Err.Raise vbObjectError + 5600, , "No source context."
    args = Split(CurrentTranslationContext(), "|")
    If UBound(args) <> 1 Then Err.Raise vbObjectError + 5601, , "Invalid source context."
    Select Case args(0)
        Case "Text": targetField = "TextID"
        Case "Petgomo": targetField = "PetN"
        Case Else: Err.Raise vbObjectError + 5602, , "Unknown source type."
    End Select
    sourceId = CLng(args(1))
    If sourceId <= 0 Then Err.Raise vbObjectError + 5603, , "Invalid source ID."
    If IsNull(f!cboLanguageFilter) Then Err.Raise vbObjectError + 5604, , "Select a language."
    languageId = CLng(f!cboLanguageFilter)
    If Len(Trim$(Nz(f!txtNewTranslation, ""))) = 0 Then
        MsgBox "Enter the translation text first.", vbExclamation
        Exit Function
    End If

    Set db = CurrentDb
    Set ws = DBEngine.Workspaces(0)
    ws.BeginTrans
    activeTransaction = True
    Set translations = db.OpenRecordset("Translations", dbOpenDynaset)
    translations.AddNew
    translations!LanguageID = languageId
    translations!Translation = CStr(f!txtNewTranslation)
    translations!ReviewStatus = "Draft"
    If Len(Trim$(Nz(f!txtNewTranslator, ""))) > 0 Then translations!Translator = CStr(f!txtNewTranslator)
    If Len(Trim$(Nz(f!txtNewSource, ""))) > 0 Then translations!Source = CStr(f!txtNewSource)
    translations.Update
    translations.Bookmark = translations.LastModified
    newId = CLng(translations!TranslationID)
    translations.Close

    Set links = db.OpenRecordset("TranslationLinks", dbOpenDynaset)
    links.AddNew
    links!TranslationID = newId
    links.Fields(targetField).Value = sourceId
    links!IsPublished = False
    links.Update
    links.Close
    ws.CommitTrans
    activeTransaction = False

    ClearNewInlineTranslation
    f.Requery
    RefreshContextTranslationList
    MsgBox "Translation " & newId & " saved as Draft (not published).", vbInformation
    SaveNewInlineTranslation = True
    Exit Function
Failed:
    msg = CStr(Err.Number) & " - " & Err.Description
    On Error Resume Next
    If Not links Is Nothing Then links.Close
    If Not translations Is Nothing Then translations.Close
    If activeTransaction Then ws.Rollback
    MsgBox "Could not create translation: " & msg, vbExclamation
End Function

Private Function HasNewControl(ByVal f As Form, ByVal controlName As String) As Boolean
    Dim c As Control
    For Each c In f.Controls
        If StrComp(c.Name, controlName, vbTextCompare) = 0 Then
            HasNewControl = True
            Exit Function
        End If
    Next c
End Function
