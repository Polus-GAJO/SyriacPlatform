Attribute VB_Name = "modTranslationLinkExistingInline"
Option Compare Database
Option Explicit

' In-place improvement of frmTextTranslations: select an existing translation
' from a dropdown rather than typing TranslationID.
' Back up Access DB and close frmTextTranslations before installation.
' Run InstallExistingTranslationPicker once.

Private Const FORM_NAME As String = "frmTextTranslations"

Public Sub InstallExistingTranslationPicker()
    Dim f As Form, c As Control, obj As Control, msg As String
    On Error GoTo Failed
    If CurrentProject.AllForms(FORM_NAME).IsLoaded Then
        MsgBox "Close frmTextTranslations first.", vbExclamation
        Exit Sub
    End If
    DoCmd.OpenForm FORM_NAME, acDesign, , , , acHidden
    Set f = Forms(FORM_NAME)
    For Each obj In f.Controls
        If StrComp(obj.Name, "cboExistingTranslation", vbTextCompare) = 0 Then
            DoCmd.Close acForm, FORM_NAME, acSaveNo
            MsgBox "Existing translation picker already installed.", vbInformation
            Exit Sub
        End If
    Next obj
    ' Put the picker in the header; preserve all existing controls and records.
    Dim topPos As Long
    topPos = f.Section(acHeader).Height + 200
    f.Section(acHeader).Height = topPos + 850
    Set c = CreateControl(FORM_NAME, acLabel, acHeader, , , 250, topPos + 70, 2100, 360)
    c.Caption = "Existing translation:"
    Set c = CreateControl(FORM_NAME, acComboBox, acHeader, , , 2450, topPos, 7400, 400)
    c.Name = "cboExistingTranslation"
    c.RowSourceType = "Table/Query"
    c.RowSource = "SELECT TranslationID, Left(Nz([Translation],''),100) AS Preview " & _
                  "FROM Translations ORDER BY TranslationID DESC"
    c.ColumnCount = 2
    c.BoundColumn = 1
    c.ColumnWidths = "1.6cm;10cm"
    c.ListWidth = 9200
    c.ListRows = 18
    c.LimitToList = True
    c.AutoExpand = True
    Set c = CreateControl(FORM_NAME, acCommandButton, acHeader, , , 10200, topPos, 2800, 450)
    c.Name = "cmdAttachExisting"
    c.Caption = "Attach selected"
    c.OnClick = "=AttachSelectedTranslation()"
    f!cmdLinkExisting.OnClick = "=FocusExistingTranslationPicker()"
    DoCmd.Close acForm, FORM_NAME, acSaveYes
    MsgBox "Existing translation picker installed.", vbInformation
    Exit Sub
Failed:
    msg = CStr(Err.Number) & " - " & Err.Description
    On Error Resume Next
    DoCmd.Close acForm, FORM_NAME, acSaveNo
    MsgBox "Installation failed: " & msg, vbExclamation
End Sub

Public Function FocusExistingTranslationPicker() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Forms(FORM_NAME)
    RefreshExistingTranslationPicker
    f!cboExistingTranslation.SetFocus
    f!cboExistingTranslation.Dropdown
    FocusExistingTranslationPicker = True
    Exit Function
Failed:
    MsgBox "Cannot open translation list: " & Err.Description, vbExclamation
End Function

Public Function RefreshExistingTranslationPicker() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Forms(FORM_NAME)
    If IsNull(f!cboLanguageFilter) Then Exit Function
    f!cboExistingTranslation.RowSource = _
        "SELECT TranslationID, Left(Nz([Translation],''),100) AS Preview " & _
        "FROM Translations WHERE LanguageID=" & CStr(CLng(f!cboLanguageFilter)) & _
        " ORDER BY TranslationID DESC"
    f!cboExistingTranslation.Requery
    RefreshExistingTranslationPicker = True
    Exit Function
Failed:
    MsgBox "Cannot refresh existing translations: " & Err.Description, vbExclamation
End Function

Public Function AttachSelectedTranslation() As Boolean
    Dim f As Form, parts() As String, sourceField As String
    Dim sourceId As Long, translationId As Long
    Dim rs As DAO.Recordset
    Dim errorText As String
    On Error GoTo Failed
    Set f = Forms(FORM_NAME)
    If IsNull(f!cboExistingTranslation) Then
        MsgBox "Choose an existing translation first.", vbExclamation
        Exit Function
    End If
    If IsNull(f!cboLanguageFilter) Then Err.Raise vbObjectError + 5700, , "Select a language."
    translationId = CLng(f!cboExistingTranslation)
    If DCount("*", "Translations", "TranslationID=" & translationId & _
        " AND LanguageID=" & CLng(f!cboLanguageFilter)) <> 1 Then
        Err.Raise vbObjectError + 5701, , "Translation is not in the selected language."
    End If
    If Len(CurrentTranslationContext()) = 0 Then Err.Raise vbObjectError + 5702, , "Missing source context."
    parts = Split(CurrentTranslationContext(), "|")
    If UBound(parts) <> 1 Then Err.Raise vbObjectError + 5703, , "Invalid source context."
    Select Case parts(0)
        Case "Text": sourceField = "TextID"
        Case "Petgomo": sourceField = "PetN"
        Case Else: Err.Raise vbObjectError + 5704, , "Unknown source type."
    End Select
    sourceId = CLng(parts(1))
    If sourceId <= 0 Then Err.Raise vbObjectError + 5705, , "Invalid source ID."
    If DCount("*", "TranslationLinks", "TranslationID=" & translationId & _
        " AND [" & sourceField & "]=" & sourceId) > 0 Then
        MsgBox "This translation is already linked to the current source.", vbInformation
        Exit Function
    End If
    Set rs = CurrentDb.OpenRecordset("TranslationLinks", dbOpenDynaset)
    rs.AddNew
    rs!TranslationID = translationId
    rs.Fields(sourceField).Value = sourceId
    rs!IsPublished = False
    rs.Update
    rs.Close
    f!cboExistingTranslation = Null
    f.Requery
    RefreshContextTranslationList
    MsgBox "Existing translation linked (not published).", vbInformation
    AttachSelectedTranslation = True
    Exit Function
Failed:
    errorText = Err.Description
    On Error Resume Next
    If Not rs Is Nothing Then rs.Close
    MsgBox "Cannot attach translation: " & errorText, vbExclamation
End Function
