Attribute VB_Name = "modPetgomoTranslations"
Option Compare Database
Option Explicit

' Installs a Translations button on the specified Petgomo form.
' The form must expose PetN in its RecordSource or as a control.
' Back up the Access database and close the target form before installation.

Public Sub InstallPetgomoTranslationsButton(ByVal sourceFormName As String)
    Dim f As Form
    Dim c As Control
    Dim item As Control
    Dim y As Long
    Dim errMsg As String
    On Error GoTo Failed

    If Len(Trim$(sourceFormName)) = 0 Then
        Err.Raise vbObjectError + 5900, , "Specify the Petgomo form name."
    End If
    If CurrentProject.AllForms(sourceFormName).IsLoaded Then
        MsgBox "Close " & sourceFormName & " before installation.", vbExclamation
        Exit Sub
    End If
    DoCmd.OpenForm sourceFormName, acDesign, , , , acHidden
    Set f = Forms(sourceFormName)

    For Each item In f.Controls
        If StrComp(item.Name, "cmdOpenPetgomoTranslations", vbTextCompare) = 0 Then
            DoCmd.Close acForm, sourceFormName, acSaveNo
            MsgBox "Petgomo translations button already installed.", vbInformation
            Exit Sub
        End If
    Next item

    y = f.Section(acDetail).Height + 90
    f.Section(acDetail).Height = y + 560
    Set c = CreateControl(sourceFormName, acCommandButton, acDetail, , , 240, y, 2300, 450)
    c.Name = "cmdOpenPetgomoTranslations"
    c.Caption = "Translations"
    c.OnClick = "=OpenSelectedPetgomoTranslations()"
    DoCmd.Close acForm, sourceFormName, acSaveYes
    MsgBox "Translations button added to " & sourceFormName & ".", vbInformation
    Exit Sub
Failed:
    errMsg = CStr(Err.Number) & " - " & Err.Description
    On Error Resume Next
    DoCmd.Close acForm, sourceFormName, acSaveNo
    MsgBox "Installation failed: " & errMsg, vbExclamation
End Sub

Public Function OpenSelectedPetgomoTranslations() As Boolean
    Dim f As Form
    Dim id As Variant
    Dim errMsg As String
    On Error GoTo Failed

    Set f = Screen.ActiveControl.Parent
    If f.NewRecord Then
        MsgBox "Select a saved Petgomo first.", vbExclamation
        Exit Function
    End If
    If f.Dirty Then
        MsgBox "Save or cancel changes before opening translations.", vbExclamation
        Exit Function
    End If

    On Error Resume Next
    Err.Clear
    id = f.Recordset.Fields("PetN").Value
    If Err.Number <> 0 Then
        Err.Clear
        id = f.Controls("PetN").Value
    End If
    If Err.Number <> 0 Then
        Err.Clear
        id = f.Controls("txtPetN").Value
    End If
    If Err.Number <> 0 Then
        On Error GoTo Failed
        Err.Raise vbObjectError + 5901, , _
            "PetN is not available. Add PetN to the form RecordSource."
    End If
    On Error GoTo Failed

    If IsNull(id) Or Not IsNumeric(id) Then
        MsgBox "This row has no valid PetN.", vbExclamation
        Exit Function
    End If
    If CLng(id) <= 0 Then
        MsgBox "This row has no valid PetN.", vbExclamation
        Exit Function
    End If

    OpenTranslationsForPetgomo CLng(id)
    OpenSelectedPetgomoTranslations = True
    Exit Function
Failed:
    errMsg = CStr(Err.Number) & " - " & Err.Description
    MsgBox "Cannot open Petgomo translations: " & errMsg, vbExclamation
End Function
