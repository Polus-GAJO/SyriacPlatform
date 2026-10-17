Attribute VB_Name = "modTranslationPublishing"
Option Compare Database
Option Explicit

' Translation publishing rules for the Access author database.
' Import this module after modTranslationSchema.bas.
' Usage: PublishTranslationLink 123
'        UnpublishTranslationLink 123
'        ValidatePublishedTranslations
'
' Publication is scoped to one source entity (Texts.TextID OR Petgomo.PetN)
' and one language. Alternative translations are preserved.

Public Sub PublishTranslationLink(ByVal linkId As Long)
    Dim db As DAO.Database
    Dim ws As DAO.Workspace
    Dim chosen As DAO.Recordset
    Dim candidates As DAO.Recordset
    Dim languageId As Long
    Dim sourceId As Long
    Dim targetField As String
    Dim sql As String
    Dim inTransaction As Boolean

    Set db = CurrentDb
    Set ws = DBEngine.Workspaces(0)
    Set chosen = db.OpenRecordset( _
        "SELECT L.LinkID, L.TextID, L.PetN, T.LanguageID, T.Translation " & _
        "FROM TranslationLinks AS L INNER JOIN Translations AS T " & _
        "ON L.TranslationID = T.TranslationID " & _
        "WHERE L.LinkID = " & CStr(linkId), dbOpenSnapshot)

    If chosen.EOF Then Err.Raise vbObjectError + 5201, "PublishTranslationLink", "Link not found."
    If IsNull(chosen!TextID) = IsNull(chosen!PetN) Then _
        Err.Raise vbObjectError + 5202, "PublishTranslationLink", _
                  "Link must point to exactly one of TextID or PetN."
    If IsNull(chosen!Translation) Then _
        Err.Raise vbObjectError + 5203, "PublishTranslationLink", "Translation content is empty."
    If Len(Trim$(CStr(chosen!Translation))) = 0 Then _
        Err.Raise vbObjectError + 5203, "PublishTranslationLink", "Translation content is empty."

    languageId = CLng(chosen!LanguageID)
    If Not IsNull(chosen!TextID) Then
        targetField = "TextID"
        sourceId = CLng(chosen!TextID)
    Else
        targetField = "PetN"
        sourceId = CLng(chosen!PetN)
    End If
    chosen.Close

    On Error GoTo RollbackAndRaise
    ws.BeginTrans
    inTransaction = True

    ' Snapshot of all links for the same source entity and language.
    sql = "SELECT L.LinkID FROM TranslationLinks AS L " & _
          "INNER JOIN Translations AS T ON L.TranslationID = T.TranslationID " & _
          "WHERE L." & targetField & " = " & CStr(sourceId) & _
          " AND T.LanguageID = " & CStr(languageId)
    Set candidates = db.OpenRecordset(sql, dbOpenSnapshot)
    Do While Not candidates.EOF
        db.Execute "UPDATE TranslationLinks SET IsPublished = " & _
                   IIf(CLng(candidates!LinkID) = linkId, "True", "False") & _
                   " WHERE LinkID = " & CStr(CLng(candidates!LinkID)), dbFailOnError
        candidates.MoveNext
    Loop
    candidates.Close
    ws.CommitTrans
    inTransaction = False
    Exit Sub

RollbackAndRaise:
    Dim errNumber As Long
    Dim errDescription As String
    errNumber = Err.Number
    errDescription = Err.Description
    On Error Resume Next
    If Not candidates Is Nothing Then candidates.Close
    If inTransaction Then ws.Rollback
    On Error GoTo 0
    Err.Raise errNumber, "PublishTranslationLink", errDescription
End Sub

Public Sub UnpublishTranslationLink(ByVal linkId As Long)
    CurrentDb.Execute "UPDATE TranslationLinks SET IsPublished = False " & _
                      "WHERE LinkID = " & CStr(linkId), dbFailOnError
End Sub

' Returns number of conflicting (source, language) combinations.
' Run before exporting/publishing; a nonzero result blocks publication.
Public Function CountPublishedTranslationConflicts() As Long
    Dim db As DAO.Database
    Dim rs As DAO.Recordset
    Dim sql As String
    Set db = CurrentDb

    sql = "SELECT L.TextID AS SourceID, T.LanguageID, Count(*) AS N " & _
          "FROM TranslationLinks AS L INNER JOIN Translations AS T " & _
          "ON L.TranslationID = T.TranslationID " & _
          "WHERE L.IsPublished = True AND L.TextID Is Not Null " & _
          "GROUP BY L.TextID, T.LanguageID HAVING Count(*) > 1"
    Set rs = db.OpenRecordset(sql, dbOpenSnapshot)
    Do While Not rs.EOF
        CountPublishedTranslationConflicts = CountPublishedTranslationConflicts + 1
        rs.MoveNext
    Loop
    rs.Close

    sql = "SELECT L.PetN AS SourceID, T.LanguageID, Count(*) AS N " & _
          "FROM TranslationLinks AS L INNER JOIN Translations AS T " & _
          "ON L.TranslationID = T.TranslationID " & _
          "WHERE L.IsPublished = True AND L.PetN Is Not Null " & _
          "GROUP BY L.PetN, T.LanguageID HAVING Count(*) > 1"
    Set rs = db.OpenRecordset(sql, dbOpenSnapshot)
    Do While Not rs.EOF
        CountPublishedTranslationConflicts = CountPublishedTranslationConflicts + 1
        rs.MoveNext
    Loop
    rs.Close
End Function

Public Sub ValidatePublishedTranslations()
    Dim conflicts As Long
    conflicts = CountPublishedTranslationConflicts()
    If conflicts > 0 Then
        Err.Raise vbObjectError + 5204, "ValidatePublishedTranslations", _
                  CStr(conflicts) & " source/language publication conflicts found."
    End If
    MsgBox "No duplicate published translations per source and language.", vbInformation
End Sub
