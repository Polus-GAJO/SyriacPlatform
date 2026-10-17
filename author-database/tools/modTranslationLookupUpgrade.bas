Attribute VB_Name = "modTranslationLookupUpgrade"
Option Compare Database
Option Explicit

' Non-destructive upgrade of the existing TranslationLinks subform.
' Replaces manual numeric entry with searchable dropdowns showing Syriac previews.
' Requires frmTranslationEditor / sfrmTranslationLinks from modTranslationForms.
' Back up the Access file and close both forms before running.
' Run: UpgradeTranslationLinkLookups
' Existing data is never modified. Running twice does nothing.

Public Sub UpgradeTranslationLinkLookups()
    Const FORM_NAME As String = "sfrmTranslationLinks"
    Dim f As Form
    Dim c As Control
    Dim oldText As Control
    Dim oldPet As Control
    Dim errNo As Long
    Dim errText As String

    If Not TranslationLookupFormExists(FORM_NAME) Then
        MsgBox "Subform sfrmTranslationLinks was not found. Install forms first.", vbExclamation
        Exit Sub
    End If
    If CurrentProject.AllForms(FORM_NAME).IsLoaded Or _
       CurrentProject.AllForms("frmTranslationEditor").IsLoaded Then
        MsgBox "Close both translation forms before running this upgrade.", vbExclamation
        Exit Sub
    End If

    DoCmd.OpenForm FORM_NAME, acDesign, , , , acHidden
    On Error GoTo Failed
    Set f = Forms(FORM_NAME)
    If TranslationControlExists(f, "cboTextLookup") Or _
       TranslationControlExists(f, "cboPetLookup") Then
        DoCmd.Close acForm, FORM_NAME, acSaveNo
        MsgBox "Lookup controls already exist; no changes made.", vbInformation
        Exit Sub
    End If

    Set oldText = f.Controls("txtTextID")
    Set oldPet = f.Controls("txtPetN")

    Set c = CreateControl(FORM_NAME, acComboBox, acDetail, , "TextID", _
                          oldText.Left, oldText.Top, 1750, oldText.Height)
    c.Name = "cboTextLookup"
    c.RowSourceType = "Table/Query"
    c.RowSource = "SELECT TextID, Left(Nz([TheText],''),90) AS Preview " & _
                  "FROM Texts ORDER BY TextID DESC"
    c.ColumnCount = 2
    c.BoundColumn = 1
    c.ColumnWidths = "1.3cm;6cm"
    c.ListWidth = 4700
    c.ListRows = 18
    c.LimitToList = True
    c.AutoExpand = True

    Set c = CreateControl(FORM_NAME, acComboBox, acDetail, , "PetN", _
                          oldPet.Left, oldPet.Top, 1750, oldPet.Height)
    c.Name = "cboPetLookup"
    c.RowSourceType = "Table/Query"
    c.RowSource = "SELECT PetN, Left(Nz([Petgomo],''),90) AS Preview " & _
                  "FROM Petgomo ORDER BY PetN DESC"
    c.ColumnCount = 2
    c.BoundColumn = 1
    c.ColumnWidths = "1.3cm;6cm"
    c.ListWidth = 4700
    c.ListRows = 18
    c.LimitToList = True
    c.AutoExpand = True

    oldText.Visible = False
    oldPet.Visible = False

    DoCmd.Close acForm, FORM_NAME, acSaveYes
    MsgBox "Text and Petgomo lookup dropdowns added. Open frmTranslationEditor.", vbInformation
    Exit Sub

Failed:
    errNo = Err.Number
    errText = Err.Description
    On Error Resume Next
    DoCmd.Close acForm, FORM_NAME, acSaveNo
    On Error GoTo 0
    MsgBox "Lookup upgrade failed (" & CStr(errNo) & "): " & errText, vbExclamation
End Sub

Private Function TranslationLookupFormExists(ByVal formName As String) As Boolean
    Dim obj As AccessObject
    For Each obj In CurrentProject.AllForms
        If StrComp(obj.Name, formName, vbTextCompare) = 0 Then
            TranslationLookupFormExists = True
            Exit Function
        End If
    Next obj
End Function

Private Function TranslationControlExists(ByVal f As Form, ByVal controlName As String) As Boolean
    Dim c As Control
    For Each c In f.Controls
        If StrComp(c.Name, controlName, vbTextCompare) = 0 Then
            TranslationControlExists = True
            Exit Function
        End If
    Next c
End Function
