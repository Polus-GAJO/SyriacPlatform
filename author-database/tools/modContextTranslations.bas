Attribute VB_Name = "modContextTranslations"
Option Compare Database
Option Explicit

' Context-first translation browser, scoped by TextID or PetN and LanguageID.
' Requires the existing Languages, Translations, TranslationLinks tables and
' modTranslationPublishing. Does not change any existing forms or data.
' Back up the Access database before running InstallContextTranslationForm.
'
' To open from a Texts form:
'   OpenTranslationsForText CLng(Me!TextID)
' From a Petgomo form:
'   OpenTranslationsForPetgomo CLng(Me!PetN)

Private Const BROWSER_NAME As String = "frmTextTranslations"

Public Sub InstallContextTranslationForm()
    Dim f As Form
    Dim tempName As String
    Dim c As Control

    If ContextFormExists(BROWSER_NAME) Then
        MsgBox BROWSER_NAME & " already exists. No changes made.", vbInformation
        Exit Sub
    End If

    Set f = CreateForm
    tempName = f.Name
    f.Caption = "Translations by source"
    f.RecordSource = "SELECT L.LinkID, L.TextID, L.PetN, L.TranslationID, " & _
        "L.IsPublished, T.LanguageID, T.Translation, T.Translator, T.Source, " & _
        "T.ReviewStatus FROM TranslationLinks AS L INNER JOIN Translations AS T " & _
        "ON L.TranslationID = T.TranslationID"
    f.DefaultView = 1
    ' CreateForm has no header by default. Add it before referencing acHeader.
    DoCmd.RunCommand acCmdFormHdrFtr
    f.Width = 15100
    f.Section(acDetail).Height = 1500
    f.Section(acHeader).Height = 2650
    f.AllowAdditions = False
    f.AllowDeletions = False
    f.AllowEdits = True
    f.NavigationButtons = True
    f.RecordSelectors = False
    f.FilterOn = False

    AddContextLabel tempName, acHeader, "Source:", 250, 220, 1200, 360
    Set c = CreateControl(tempName, acTextBox, acHeader, , , 1500, 200, 12900, 700)
    c.Name = "txtSourcePreview"
    c.Locked = True
    c.TabStop = False
    c.ScrollBars = 2

    AddContextLabel tempName, acHeader, "Language:", 250, 1130, 1200, 360
    Set c = CreateControl(tempName, acComboBox, acHeader, , , 1500, 1080, 3000, 400)
    c.Name = "cboLanguageFilter"
    c.RowSourceType = "Table/Query"
    c.RowSource = "SELECT LanguageID, LanguageName FROM Languages ORDER BY LanguageName"
    c.ColumnCount = 2
    c.BoundColumn = 1
    c.ColumnWidths = "0cm;5cm"
    c.LimitToList = True
    c.AfterUpdate = "=RefreshContextTranslationList()"

    Set c = CreateControl(tempName, acCommandButton, acHeader, , , 4800, 1080, 2700, 450)
    c.Name = "cmdNewTranslation"
    c.Caption = "New translation"
    c.OnClick = "=NewContextTranslation()"

    Set c = CreateControl(tempName, acCommandButton, acHeader, , , 7750, 1080, 2800, 450)
    c.Name = "cmdLinkExisting"
    c.Caption = "Link existing ID"
    c.OnClick = "=LinkExistingContextTranslation()"

    Set c = CreateControl(tempName, acCommandButton, acHeader, , , 10800, 1080, 2900, 450)
    c.Name = "cmdValidate"
    c.Caption = "Check publications"
    c.OnClick = "=ValidateTranslationEditorLinks()"

    AddContextLabel tempName, acHeader, "Translation (click Edit to change text)", 250, 1990, 6500, 360

    AddContextText tempName, "txtTranslation", "Translation", 250, 80, 6300, 1050
    AddContextText tempName, "txtTranslator", "Translator", 6720, 80, 1700, 360
    AddContextText tempName, "txtReview", "ReviewStatus", 6720, 580, 1700, 360
    AddContextText tempName, "txtPublished", "IsPublished", 8520, 80, 850, 360
    AddContextText tempName, "txtTranslationID", "TranslationID", 8520, 580, 850, 360

    Set c = CreateControl(tempName, acCommandButton, acDetail, , , 9540, 90, 1450, 420)
    c.Name = "cmdEdit"
    c.Caption = "Edit"
    c.OnClick = "=EditContextTranslation()"

    Set c = CreateControl(tempName, acCommandButton, acDetail, , , 11200, 90, 1550, 420)
    c.Name = "cmdPublish"
    c.Caption = "Publish"
    c.OnClick = "=PublishContextTranslation()"

    Set c = CreateControl(tempName, acCommandButton, acDetail, , , 12900, 90, 1700, 420)
    c.Name = "cmdUnpublish"
    c.Caption = "Unpublish"
    c.OnClick = "=UnpublishContextTranslation()"

    f.OnLoad = "=LoadContextTranslationBrowser()"
    DoCmd.Close acForm, tempName, acSaveYes
    DoCmd.Rename BROWSER_NAME, acForm, tempName
    MsgBox "Created " & BROWSER_NAME & ". Open it from a Texts/Petgomo form.", vbInformation
End Sub

Public Sub OpenTranslationsForText(ByVal textId As Long)
    OpenContextTranslations "Text", textId
End Sub

Public Sub OpenTranslationsForPetgomo(ByVal petN As Long)
    OpenContextTranslations "Petgomo", petN
End Sub

Private Sub OpenContextTranslations(ByVal sourceType As String, ByVal sourceId As Long)
    If sourceId <= 0 Then Err.Raise vbObjectError + 5320, , "Invalid source ID."
    ' Access does not re-run OnLoad/OpenArgs when OpenForm targets an already-open form.
    ' Close and reopen so the new source context is always applied.
    If CurrentProject.AllForms(BROWSER_NAME).IsLoaded Then
        DoCmd.Close acForm, BROWSER_NAME, acSaveNo
    End If
    DoCmd.OpenForm BROWSER_NAME, acNormal, , , , , _
                   sourceType & "|" & CStr(sourceId)
End Sub

Public Function LoadContextTranslationBrowser() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Dim parts() As String
    Dim entityName As String
    Dim entityId As Long
    Dim sql As String
    Dim rs As DAO.Recordset

    Set f = Forms(BROWSER_NAME)
    If IsNull(f.OpenArgs) Then Err.Raise vbObjectError + 5300, , "Open this form from a source record."
    parts = Split(CStr(f.OpenArgs), "|")
    If UBound(parts) <> 1 Then Err.Raise vbObjectError + 5301, , "Invalid source argument."
    entityName = parts(0)
    entityId = CLng(parts(1))
    If entityId <= 0 Then Err.Raise vbObjectError + 5302, , "Invalid source ID."

    Select Case entityName
        Case "Text"
            sql = "SELECT TheText AS Preview FROM Texts WHERE TextID=" & CStr(entityId)
        Case "Petgomo"
            sql = "SELECT Petgomo AS Preview FROM Petgomo WHERE PetN=" & CStr(entityId)
        Case Else
            Err.Raise vbObjectError + 5303, , "Unknown source type."
    End Select

    Set rs = CurrentDb.OpenRecordset(sql, dbOpenSnapshot)
    If rs.EOF Then Err.Raise vbObjectError + 5304, , "Source record does not exist."
    f!txtSourcePreview = Nz(rs!Preview, "")
    rs.Close

    f!cboLanguageFilter = DLookup("LanguageID", "Languages", "LanguageCode='ar'")
    If IsNull(f!cboLanguageFilter) Then f!cboLanguageFilter = DMin("LanguageID", "Languages")
    RefreshContextTranslationList
    LoadContextTranslationBrowser = True
    Exit Function
Failed:
    MsgBox "Cannot open translation context: " & Err.Description, vbExclamation
End Function

Public Function RefreshContextTranslationList() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Dim parts() As String
    Dim target As String
    Set f = Forms(BROWSER_NAME)
    If IsNull(f.OpenArgs) Or IsNull(f!cboLanguageFilter) Then Exit Function
    parts = Split(CStr(f.OpenArgs), "|")
    If UBound(parts) <> 1 Then Exit Function
    If parts(0) = "Text" Then
        target = "TextID"
    ElseIf parts(0) = "Petgomo" Then
        target = "PetN"
    Else
        Exit Function
    End If
    ' Restrict the RecordSource itself, including when zero translations exist.
    ' A bound form with no matching rows must remain empty rather than showing
    ' stale records from a previously selected source.
    f.RecordSource = "SELECT L.LinkID, L.TextID, L.PetN, L.TranslationID, " & _
        "L.IsPublished, T.LanguageID, T.Translation, T.Translator, T.Source, " & _
        "T.ReviewStatus FROM TranslationLinks AS L INNER JOIN Translations AS T " & _
        "ON L.TranslationID = T.TranslationID WHERE L.[" & target & "]=" & _
        CStr(CLng(parts(1))) & " AND T.LanguageID=" & _
        CStr(CLng(f!cboLanguageFilter))
    f.FilterOn = False
    f.Requery
    RefreshContextTranslationList = True
    Exit Function
Failed:
    MsgBox "Cannot filter translations: " & Err.Description, vbExclamation
End Function

Public Function NewContextTranslation() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Dim parts() As String
    Dim target As String
    Dim id As Long
    Dim languageId As Long
    Dim translationText As String
    Dim translator As String
    Dim sourceNote As String
    Dim db As DAO.Database
    Dim rs As DAO.Recordset
    Dim translationId As Long
    Dim link As DAO.Recordset

    Set f = Forms(BROWSER_NAME)
    If IsNull(f!cboLanguageFilter) Then Exit Function
    parts = Split(CStr(f.OpenArgs), "|")
    target = IIf(parts(0) = "Text", "TextID", "PetN")
    id = CLng(parts(1))
    languageId = CLng(f!cboLanguageFilter)
    translationText = InputBox("Enter the translation text:", "New translation")
    If Len(Trim$(translationText)) = 0 Then Exit Function
    translator = InputBox("Translator (optional):", "New translation")
    sourceNote = InputBox("Source (optional):", "New translation")

    Set db = CurrentDb
    Set rs = db.OpenRecordset("Translations", dbOpenDynaset)
    rs.AddNew
    rs!LanguageID = languageId
    rs!Translation = translationText
    rs!Translator = IIf(Len(Trim$(translator)) = 0, Null, translator)
    rs!Source = IIf(Len(Trim$(sourceNote)) = 0, Null, sourceNote)
    rs!ReviewStatus = "Draft"
    rs.Update
    rs.Bookmark = rs.LastModified
    translationId = CLng(rs!TranslationID)
    rs.Close

    Set link = db.OpenRecordset("TranslationLinks", dbOpenDynaset)
    link.AddNew
    link!TranslationID = translationId
    link.Fields(target).Value = id
    link!IsPublished = False
    link.Update
    link.Close

    f.Requery
    RefreshContextTranslationList
    NewContextTranslation = True
    Exit Function
Failed:
    MsgBox "Cannot add translation: " & Err.Description, vbExclamation
End Function

Public Function LinkExistingContextTranslation() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Dim parts() As String
    Dim target As String
    Dim entityId As Long
    Dim translationId As Long
    Dim answer As String
    Dim rs As DAO.Recordset

    Set f = Forms(BROWSER_NAME)
    answer = InputBox("Enter the ID of an existing translation in the selected language:", "Link existing translation")
    If Len(Trim$(answer)) = 0 Then Exit Function
    If Not IsNumeric(answer) Then Err.Raise vbObjectError + 5310, , "Enter a numeric TranslationID."
    translationId = CLng(answer)
    If DCount("*", "Translations", "TranslationID=" & translationId & _
              " AND LanguageID=" & CLng(f!cboLanguageFilter)) <> 1 Then _
        Err.Raise vbObjectError + 5311, , "Translation not found in the selected language."
    parts = Split(CStr(f.OpenArgs), "|")
    target = IIf(parts(0) = "Text", "TextID", "PetN")
    entityId = CLng(parts(1))
    If DCount("*", "TranslationLinks", "TranslationID=" & translationId & _
              " AND [" & target & "]=" & entityId) > 0 Then _
        Err.Raise vbObjectError + 5312, , "This translation is already linked to this source."

    Set rs = CurrentDb.OpenRecordset("TranslationLinks", dbOpenDynaset)
    rs.AddNew
    rs!TranslationID = translationId
    rs.Fields(target).Value = entityId
    rs!IsPublished = False
    rs.Update
    rs.Close
    f.Requery
    RefreshContextTranslationList
    LinkExistingContextTranslation = True
    Exit Function
Failed:
    MsgBox "Cannot link translation: " & Err.Description, vbExclamation
End Function

Public Function EditContextTranslation() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Dim translationId As Long
    Set f = Screen.ActiveControl.Parent
    If f.NewRecord Or IsNull(f!TranslationID) Then Exit Function
    translationId = CLng(f!TranslationID)
    OpenTranslationDetail translationId
    f.Requery
    EditContextTranslation = True
    Exit Function
Failed:
    MsgBox "Cannot edit translation: " & Err.Description, vbExclamation
End Function

Public Function PublishContextTranslation() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Screen.ActiveControl.Parent
    PublishTranslationLink CLng(f!LinkID)
    f.Requery
    PublishContextTranslation = True
    Exit Function
Failed:
    MsgBox "Cannot publish: " & Err.Description, vbExclamation
End Function

Public Function UnpublishContextTranslation() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Screen.ActiveControl.Parent
    UnpublishTranslationLink CLng(f!LinkID)
    f.Requery
    UnpublishContextTranslation = True
    Exit Function
Failed:
    MsgBox "Cannot unpublish: " & Err.Description, vbExclamation
End Function

Private Sub AddContextLabel(ByVal formName As String, ByVal section As AcSection, _
                            ByVal caption As String, ByVal x As Long, _
                            ByVal y As Long, ByVal w As Long, ByVal h As Long)
    Dim c As Control
    Set c = CreateControl(formName, acLabel, section, , , x, y, w, h)
    c.Caption = caption
End Sub

Private Sub AddContextText(ByVal formName As String, ByVal name As String, _
                           ByVal fieldName As String, ByVal x As Long, _
                           ByVal y As Long, ByVal w As Long, ByVal h As Long)
    Dim c As Control
    Set c = CreateControl(formName, acTextBox, acDetail, , fieldName, x, y, w, h)
    c.Name = name
    c.Locked = True
    c.TabStop = False
End Sub

Private Function ContextFormExists(ByVal formName As String) As Boolean
    Dim obj As AccessObject
    For Each obj In CurrentProject.AllForms
        If StrComp(obj.Name, formName, vbTextCompare) = 0 Then
            ContextFormExists = True
            Exit Function
        End If
    Next obj
End Function
