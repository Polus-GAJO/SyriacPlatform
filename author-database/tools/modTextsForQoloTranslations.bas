Attribute VB_Name = "modTextsForQoloTranslations"
Option Compare Database
Option Explicit

' Adds a button to the existing TextsForQolo form.
' Opens frmTextTranslations for the selected TextID.
' Run InstallTextsForQoloTranslations once after backing up the database.

Private Const SOURCE_FORM As String = "TextsForQolo"

Public Sub InstallTextsForQoloTranslations()
    Dim f As Form
    Dim c As Control
    Dim item As Control
    Dim x As Long, y As Long
    Dim errMsg As String

    On Error GoTo Failed
    If CurrentProject.AllForms(SOURCE_FORM).IsLoaded Then
        MsgBox "Close " & SOURCE_FORM & " before installation.", vbExclamation
        Exit Sub
    End If

    DoCmd.OpenForm SOURCE_FORM, acDesign, , , , acHidden
    Set f = Forms(SOURCE_FORM)
    For Each item In f.Controls
        If StrComp(item.Name, "cmdOpenTextTranslations", vbTextCompare) = 0 Then
            DoCmd.Close acForm, SOURCE_FORM, acSaveNo
            MsgBox "Translations button is already installed.", vbInformation
            Exit Sub
        End If
    Next item

    ' Reserve a new row at the bottom of the detail section, avoiding overlap
    ' with existing controls. In continuous forms the button repeats per row.
    y = f.Section(acDetail).Height + 90
    f.Section(acDetail).Height = y + 560
    x = 240
    Set c = CreateControl(SOURCE_FORM, acCommandButton, acDetail, , , x, y, 2300, 450)
    c.Name = "cmdOpenTextTranslations"
    c.Caption = "Translations"
    c.OnClick = "=OpenSelectedQoloTextTranslations()"
    DoCmd.Close acForm, SOURCE_FORM, acSaveYes
    MsgBox "Translations button added to " & SOURCE_FORM & ".", vbInformation
    Exit Sub
Failed:
    errMsg = CStr(Err.Number) & " - " & Err.Description
    On Error Resume Next
    DoCmd.Close acForm, SOURCE_FORM, acSaveNo
    MsgBox "Installation failed: " & errMsg, vbExclamation
End Sub

Public Function OpenSelectedQoloTextTranslations() As Boolean
    Dim f As Form
    Dim id As Variant
    Dim errMsg As String

    On Error GoTo Failed
    Set f = Screen.ActiveControl.Parent
    If f.Name <> SOURCE_FORM Then
        Err.Raise vbObjectError + 5800, , "Unexpected source form."
    End If
    If f.NewRecord Then
        MsgBox "Select a saved text first.", vbExclamation
        Exit Function
    End If
    If f.Dirty Then
        MsgBox "Save or cancel changes before opening translations.", vbExclamation
        Exit Function
    End If

    ' Read the actual current record, not an arbitrary Qolo or first row.
    ' If TextID is not in the form's RecordSource, show a clear diagnostic.
    On Error Resume Next
    Err.Clear
    id = f.Recordset.Fields("TextID").Value
    If Err.Number <> 0 Then
        Err.Clear
        id = f.Controls("TextID").Value
    End If
    If Err.Number <> 0 Then
        Err.Clear
        id = f.Controls("txtTextID").Value
    End If
    If Err.Number <> 0 Then
        On Error GoTo Failed
        Err.Raise vbObjectError + 5801, , _
            "TextID is not available in this form. Add TextID to its RecordSource."
    End If
    On Error GoTo Failed

    If IsNull(id) Or Not IsNumeric(id) Then
        MsgBox "This row has no valid TextID.", vbExclamation
        Exit Function
    End If
    If CLng(id) <= 0 Then
        MsgBox "This row has no valid TextID.", vbExclamation
        Exit Function
    End If

    OpenTranslationsForText CLng(id)
    OpenSelectedQoloTextTranslations = True
    Exit Function
Failed:
    errMsg = CStr(Err.Number) & " - " & Err.Description
    MsgBox "Cannot open translations: " & errMsg, vbExclamation
End Function
