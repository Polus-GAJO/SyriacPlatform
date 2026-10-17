Attribute VB_Name = "modTranslationForms"
Option Compare Database
Option Explicit

' Creates a translation editor and its linked-entity subform in Access.
' Requires modTranslationSchema and modTranslationPublishing.
' Back up the database first. Run InstallTranslationForms in Immediate Window.
' Existing forms with these names are NEVER overwritten.

Public Sub InstallTranslationForms()
    If FormExists("frmTranslationEditor") Or FormExists("sfrmTranslationLinks") Then
        MsgBox "Translation forms already exist. No changes made.", vbExclamation
        Exit Sub
    End If
    CreateLinksForm
    CreateEditorForm
    MsgBox "Created frmTranslationEditor and sfrmTranslationLinks. Open frmTranslationEditor.", vbInformation
End Sub

Private Sub CreateLinksForm()
    Dim f As Form
    Dim tempName As String
    Set f = CreateForm
    tempName = f.Name
    f.RecordSource = "TranslationLinks"
    f.Caption = "Linked texts and Petgomo"
    f.DefaultView = 1 ' Continuous Forms
    f.Width = 11500
    f.Section(acDetail).Height = 750
    f.Section(acHeader).Height = 550
    f.NavigationButtons = True
    f.RecordSelectors = True

    AddHeader tempName, "TextID (optional)", 200, 170, 1700
    AddHeader tempName, "PetN (optional)", 2100, 170, 1700
    AddHeader tempName, "Published?", 4000, 170, 1300
    AddHeader tempName, "LinkID", 5550, 170, 900

    AddBoundText tempName, "txtTextID", "TextID", 200, 120, 1500, 350
    AddBoundText tempName, "txtPetN", "PetN", 2100, 120, 1500, 350
    Dim c As Control
    Set c = CreateControl(tempName, acCheckBox, acDetail, , "IsPublished", 4220, 140, 450, 350)
    c.Name = "chkPublished"
    c.Locked = True
    c.TabStop = False
    AddBoundText tempName, "txtLinkID", "LinkID", 5550, 120, 900, 350
    f.Controls("txtLinkID").Locked = True
    f.Controls("txtLinkID").TabStop = False

    Set c = CreateControl(tempName, acCommandButton, acDetail, , , 6800, 100, 1950, 430)
    c.Name = "cmdPublish"
    c.Caption = "Publish this link"
    c.OnClick = "=PublishActiveTranslationLink()"

    Set c = CreateControl(tempName, acCommandButton, acDetail, , , 8950, 100, 1800, 430)
    c.Name = "cmdUnpublish"
    c.Caption = "Unpublish"
    c.OnClick = "=UnpublishActiveTranslationLink()"

    DoCmd.Close acForm, tempName, acSaveYes
    DoCmd.Rename "sfrmTranslationLinks", acForm, tempName
End Sub

Private Sub CreateEditorForm()
    Dim f As Form
    Dim tempName As String
    Dim c As Control
    Set f = CreateForm
    tempName = f.Name
    f.RecordSource = "Translations"
    f.Caption = "SyriacPlatform - Translation Editor"
    f.Width = 13000
    f.Section(acDetail).Height = 8500
    f.NavigationButtons = True

    AddLabel tempName, "Translation ID", 300, 220, 1700
    AddBoundText tempName, "txtTranslationID", "TranslationID", 2400, 220, 1100, 360
    f.Controls("txtTranslationID").Locked = True
    f.Controls("txtTranslationID").TabStop = False

    AddLabel tempName, "Language", 300, 820, 1700
    Set c = CreateControl(tempName, acComboBox, acDetail, , "LanguageID", 2400, 820, 3800, 390)
    c.Name = "cboLanguage"
    c.RowSourceType = "Table/Query"
    c.RowSource = "SELECT LanguageID, LanguageName, LanguageCode FROM Languages ORDER BY LanguageName"
    c.ColumnCount = 3
    c.BoundColumn = 1
    c.ColumnWidths = "0cm;4cm;1.5cm"
    c.LimitToList = True

    AddLabel tempName, "Translation", 300, 1450, 1700
    AddBoundText tempName, "txtTranslation", "Translation", 2400, 1450, 9400, 1600
    f.Controls("txtTranslation").EnterKeyBehavior = True
    f.Controls("txtTranslation").ScrollBars = 2

    AddLabel tempName, "Translator", 300, 3280, 1700
    AddBoundText tempName, "txtTranslator", "Translator", 2400, 3280, 5500, 360
    AddLabel tempName, "Source", 300, 3810, 1700
    AddBoundText tempName, "txtSource", "Source", 2400, 3810, 5500, 360
    AddLabel tempName, "Review status", 300, 4340, 1700
    AddBoundText tempName, "txtReviewStatus", "ReviewStatus", 2400, 4340, 2500, 360

    AddLabel tempName, "Linked Texts / Petgomo (enter one ID per row)", 300, 5050, 9000
    Set c = CreateControl(tempName, acSubform, acDetail, , , 300, 5480, 12000, 1700)
    c.Name = "subTranslationLinks"
    c.SourceObject = "Form.sfrmTranslationLinks"
    c.LinkMasterFields = "TranslationID"
    c.LinkChildFields = "TranslationID"

    Set c = CreateControl(tempName, acCommandButton, acDetail, , , 300, 7500, 2900, 480)
    c.Name = "cmdValidate"
    c.Caption = "Check published links"
    c.OnClick = "=ValidateTranslationEditorLinks()"

    DoCmd.Close acForm, tempName, acSaveYes
    DoCmd.Rename "frmTranslationEditor", acForm, tempName
End Sub

Public Function PublishActiveTranslationLink() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Screen.ActiveControl.Parent
    If f.Dirty Then f.Dirty = False
    If f.NewRecord Or IsNull(f!LinkID) Then
        MsgBox "Save the link first.", vbExclamation
        Exit Function
    End If
    PublishTranslationLink CLng(f!LinkID)
    f.Requery
    PublishActiveTranslationLink = True
    Exit Function
Failed:
    MsgBox "Publication failed: " & Err.Description, vbExclamation
End Function

Public Function UnpublishActiveTranslationLink() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Set f = Screen.ActiveControl.Parent
    If f.Dirty Then f.Dirty = False
    If f.NewRecord Or IsNull(f!LinkID) Then
        MsgBox "Save the link first.", vbExclamation
        Exit Function
    End If
    UnpublishTranslationLink CLng(f!LinkID)
    f.Requery
    UnpublishActiveTranslationLink = True
    Exit Function
Failed:
    MsgBox "Unpublish failed: " & Err.Description, vbExclamation
End Function

Public Function ValidateTranslationEditorLinks() As Boolean
    On Error GoTo Failed
    ValidatePublishedTranslations
    ValidateTranslationEditorLinks = True
    Exit Function
Failed:
    MsgBox "Validation failed: " & Err.Description, vbExclamation
End Function

Private Sub AddHeader(ByVal formName As String, ByVal caption As String, _
                      ByVal x As Long, ByVal y As Long, ByVal w As Long)
    Dim c As Control
    Set c = CreateControl(formName, acLabel, acHeader, , , x, y, w, 320)
    c.Caption = caption
End Sub

Private Sub AddLabel(ByVal formName As String, ByVal caption As String, _
                     ByVal x As Long, ByVal y As Long, ByVal w As Long)
    Dim c As Control
    Set c = CreateControl(formName, acLabel, acDetail, , , x, y, w, 350)
    c.Caption = caption
End Sub

Private Sub AddBoundText(ByVal formName As String, ByVal name As String, _
                         ByVal fieldName As String, ByVal x As Long, _
                         ByVal y As Long, ByVal w As Long, ByVal h As Long)
    Dim c As Control
    Set c = CreateControl(formName, acTextBox, acDetail, , fieldName, x, y, w, h)
    c.Name = name
End Sub

Private Function FormExists(ByVal formName As String) As Boolean
    Dim obj As AccessObject
    For Each obj In CurrentProject.AllForms
        If StrComp(obj.Name, formName, vbTextCompare) = 0 Then
            FormExists = True
            Exit Function
        End If
    Next obj
End Function
