Attribute VB_Name = "modTextsForQoloPetgomoTranslations"
Option Compare Database
Option Explicit

' Adds a SECOND translation button to TextsForQolo, for the Petgomo
' associated with the selected row. Existing Text translations button
' and frmTextTranslations are unchanged.
' Requires PetN to be present in the TextsForQolo RecordSource or controls.
' Back up Access database and close TextsForQolo before installation.

Private Const SOURCE_FORM As String = "TextsForQolo"

Public Sub InstallTextsForQoloPetgomoTranslations()
    Dim f As Form
    Dim c As Control
    Dim item As Control
    Dim y As Long
    Dim errMsg As String
    On Error GoTo Failed

    If CurrentProject.AllForms(SOURCE_FORM).IsLoaded Then
        MsgBox "Close " & SOURCE_FORM & " before installation.", vbExclamation
        Exit Sub
    End If

    DoCmd.OpenForm SOURCE_FORM, acDesign, , , , acHidden
    Set f = Forms(SOURCE_FORM)
    For Each item In f.Controls
        If StrComp(item.Name, "cmdOpenPetgomoTranslations", vbTextCompare) = 0 Then
            DoCmd.Close acForm, SOURCE_FORM, acSaveNo
            MsgBox "Petgomo translations button already installed.", vbInformation
            Exit Sub
        End If
    Next item

    y = f.Section(acDetail).Height + 90
    f.Section(acDetail).Height = y + 560
    Set c = CreateControl(SOURCE_FORM, acCommandButton, acDetail, , , 240, y, 2800, 450)
    c.Name = "cmdOpenPetgomoTranslations"
    c.Caption = "Petgomo translations"
    c.OnClick = "=OpenSelectedQoloPetgomoTranslations()"

    DoCmd.Close acForm, SOURCE_FORM, acSaveYes
    MsgBox "Petgomo translations button added to " & SOURCE_FORM & ".", vbInformation
    Exit Sub
Failed:
    errMsg = CStr(Err.Number) & " - " & Err.Description
    On Error Resume Next
    DoCmd.Close acForm, SOURCE_FORM, acSaveNo
    MsgBox "Installation failed: " & errMsg, vbExclamation
End Sub

Public Function OpenSelectedQoloPetgomoTranslations() As Boolean
    Dim f As Form
    Dim petId As Variant
    Dim errMsg As String
    On Error GoTo Failed

    Set f = Screen.ActiveControl.Parent
    If StrComp(f.Name, SOURCE_FORM, vbTextCompare) <> 0 Then
        Err.Raise vbObjectError + 5910, , "Unexpected source form."
    End If
    If f.NewRecord Then
        MsgBox "Select a saved row first.", vbExclamation
        Exit Function
    End If
    If f.Dirty Then
        MsgBox "Save or cancel changes before opening translations.", vbExclamation
        Exit Function
    End If

    On Error Resume Next
    Err.Clear
    petId = f.Recordset.Fields("PetN").Value
    If Err.Number <> 0 Then
        Err.Clear
        petId = f.Controls("PetN").Value
    End If
    If Err.Number <> 0 Then
        Err.Clear
        petId = f.Controls("txtPetN").Value
    End If
    If Err.Number <> 0 Then
        On Error GoTo Failed
        Err.Raise vbObjectError + 5911, , _
            "PetN is not available in TextsForQolo. Add PetN to its RecordSource."
    End If
    On Error GoTo Failed

    If IsNull(petId) Or Not IsNumeric(petId) Then
        MsgBox "This row has no valid PetN.", vbExclamation
        Exit Function
    End If
    If CLng(petId) <= 0 Then
        MsgBox "This row has no valid PetN.", vbExclamation
        Exit Function
    End If

    OpenTranslationsForPetgomo CLng(petId)
    OpenSelectedQoloPetgomoTranslations = True
    Exit Function
Failed:
    errMsg = CStr(Err.Number) & " - " & Err.Description
    MsgBox "Cannot open Petgomo translations: " & errMsg, vbExclamation
End Function

' Refreshes the command button for the currently selected record.
' In a continuous form, Enabled applies to all displayed button instances,
' not independently to each row.
Public Function UpdateQoloPetgomoTranslationButton() As Boolean
    On Error GoTo Failed
    Dim f As Form
    Dim petId As Variant
    Set f = Forms(SOURCE_FORM)
    petId = Null
    On Error Resume Next
    Err.Clear
    petId = f.Recordset.Fields("PetN").Value
    If Err.Number <> 0 Then
        Err.Clear
        petId = f.Controls("PetN").Value
    End If
    If Err.Number <> 0 Then
        Err.Clear
        petId = f.Controls("txtPetN").Value
    End If
    On Error GoTo Failed
    f.Controls("cmdOpenPetgomoTranslations").Enabled = _
        (Not f.NewRecord) And IsNumeric(Nz(petId, Null)) And (Val(Nz(petId, 0)) > 0)
    UpdateQoloPetgomoTranslationButton = True
    Exit Function
Failed:
    MsgBox "Cannot update Petgomo translation button: " & Err.Description, vbExclamation
End Function

' Installs OnCurrent only when no existing handler is present.
' An existing event is never overwritten.
Public Sub InstallQoloPetgomoButtonState()
    Dim f As Form
    On Error GoTo Failed
    If CurrentProject.AllForms(SOURCE_FORM).IsLoaded Then
        MsgBox "Close " & SOURCE_FORM & " before installation.", vbExclamation
        Exit Sub
    End If
    DoCmd.OpenForm SOURCE_FORM, acDesign, , , , acHidden
    Set f = Forms(SOURCE_FORM)
    If Len(Nz(f.OnCurrent, "")) = 0 Then
        f.OnCurrent = "=UpdateQoloPetgomoTranslationButton()"
        DoCmd.Close acForm, SOURCE_FORM, acSaveYes
        MsgBox "Petgomo translation button state installed.", vbInformation
    Else
        DoCmd.Close acForm, SOURCE_FORM, acSaveNo
        MsgBox "Form already has an On Current event. Add this call to its existing event procedure:" & vbCrLf & _
               "UpdateQoloPetgomoTranslationButton", vbInformation
    End If
    Exit Sub
Failed:
    Dim description As String
    description = Err.Description
    On Error Resume Next
    DoCmd.Close acForm, SOURCE_FORM, acSaveNo
    MsgBox "Cannot install button state: " & description, vbExclamation
End Sub
