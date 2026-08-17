Attribute VB_Name = "modSchemaExporter"
Option Compare Database

Option Explicit

' ============================================================
' SyriacPlatform - Author Database Schema Exporter
'
' Exports the structural schema of the Author Database to:
'
'   D:\SyriacPlatform\author-database\schema\
'
' Files:
'   tables.json
'   relationships.json
'   indexes.json
'
' The exported files are intended to be committed to Git and
' treated as the authoritative machine-readable snapshot of the
' Author Database schema used by SyriacPlatform Build Tools.
' ============================================================

Private Const EXPORT_ROOT As String = _
    "D:\SyriacPlatform\author-database\schema"

Private Const SCHEMA_VERSION As Long = 1
Private Const EXPORTER_VERSION As String = "1.0"


' ============================================================
' PUBLIC ENTRY POINT
' ============================================================

Public Sub ExportAuthorDatabaseSchema()

    On Error GoTo ErrorHandler

    Dim db As DAO.Database

    Set db = CurrentDb

    EnsureFolderExists EXPORT_ROOT

    WriteUtf8File _
        EXPORT_ROOT & "\tables.json", _
        BuildTablesJson(db)

    WriteUtf8File _
        EXPORT_ROOT & "\relationships.json", _
        BuildRelationshipsJson(db)

    WriteUtf8File _
        EXPORT_ROOT & "\indexes.json", _
        BuildIndexesJson(db)

    MsgBox _
        "Author Database schema exported successfully." & vbCrLf & vbCrLf & _
        EXPORT_ROOT, _
        vbInformation, _
        "SyriacPlatform Schema Exporter"

CleanExit:
    Set db = Nothing
    Exit Sub

ErrorHandler:

    MsgBox _
        "Schema export failed." & vbCrLf & vbCrLf & _
        "Error " & Err.Number & ":" & vbCrLf & _
        Err.Description, _
        vbCritical, _
        "SyriacPlatform Schema Exporter"

    Resume CleanExit

End Sub


' ============================================================
' TABLES
' ============================================================

Private Function BuildTablesJson(ByVal db As DAO.Database) As String

    Dim Result As String
    Dim tableNames As Variant
    Dim i As Long

    tableNames = GetUserTableNames(db)

    Result = "{" & vbCrLf
    Result = Result & _
        "  ""schemaVersion"": " & SCHEMA_VERSION & "," & vbCrLf
    Result = Result & _
        "  ""exporterVersion"": " & JsonString(EXPORTER_VERSION) & "," & vbCrLf
    Result = Result & _
        "  ""tables"": [" & vbCrLf

    If HasArrayItems(tableNames) Then

        For i = LBound(tableNames) To UBound(tableNames)

            Result = Result & _
                BuildSingleTableJson(db.TableDefs(CStr(tableNames(i))), 4)

            If i < UBound(tableNames) Then
                Result = Result & ","
            End If

            Result = Result & vbCrLf

        Next i

    End If

    Result = Result & "  ]" & vbCrLf
    Result = Result & "}" & vbCrLf

    BuildTablesJson = Result

End Function


Private Function BuildSingleTableJson( _
    ByVal tdf As DAO.TableDef, _
    ByVal indent As Long _
) As String

    Dim Result As String
    Dim i As Long
    Dim fld As DAO.Field

    Result = Spaces(indent) & "{" & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """name"": " & JsonString(tdf.Name) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """isLinked"": " & JsonBoolean(IsLinkedTable(tdf)) & "," & vbCrLf

    If IsLinkedTable(tdf) Then

        Result = Result & _
            Spaces(indent + 2) & _
            """sourceTableName"": " & _
            JsonNullableString(tdf.SourceTableName) & "," & vbCrLf

    Else

        Result = Result & _
            Spaces(indent + 2) & _
            """sourceTableName"": null," & vbCrLf

    End If

    Result = Result & _
        Spaces(indent + 2) & _
        """columns"": [" & vbCrLf

    For i = 0 To tdf.Fields.Count - 1

        Set fld = tdf.Fields(i)

        Result = Result & _
            BuildFieldJson(tdf, fld, i, indent + 4)

        If i < tdf.Fields.Count - 1 Then
            Result = Result & ","
        End If

        Result = Result & vbCrLf

    Next i

    Result = Result & _
        Spaces(indent + 2) & "]" & vbCrLf

    Result = Result & _
        Spaces(indent) & "}"

    Set fld = Nothing

    BuildSingleTableJson = Result

End Function


Private Function BuildFieldJson( _
    ByVal tdf As DAO.TableDef, _
    ByVal fld As DAO.Field, _
    ByVal ordinal As Long, _
    ByVal indent As Long _
) As String

    Dim Result As String

    Result = Spaces(indent) & "{" & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """name"": " & JsonString(fld.Name) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """ordinal"": " & ordinal & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """type"": " & JsonString(GetDaoTypeName(fld.Type)) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """daoType"": " & CLng(fld.Type) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """size"": " & CLng(fld.Size) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """nullable"": " & JsonBoolean(Not SafeFieldRequired(fld)) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """primaryKey"": " & _
        JsonBoolean(IsPrimaryKeyField(tdf, fld.Name)) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """autoIncrement"": " & _
        JsonBoolean(IsAutoIncrementField(fld)) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """allowZeroLength"": " & _
        JsonNullableBoolean(GetAllowZeroLength(fld)) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """defaultValue"": " & _
        JsonNullableString(GetFieldProperty(fld, "DefaultValue")) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """validationRule"": " & _
        JsonNullableString(GetFieldProperty(fld, "ValidationRule")) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """validationText"": " & _
        JsonNullableString(GetFieldProperty(fld, "ValidationText")) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """description"": " & _
        JsonNullableString(GetFieldProperty(fld, "Description")) & vbCrLf

    Result = Result & _
        Spaces(indent) & "}"

    BuildFieldJson = Result

End Function


' ============================================================
' RELATIONSHIPS
' ============================================================

Private Function BuildRelationshipsJson( _
    ByVal db As DAO.Database _
) As String

    Dim Result As String
    Dim relationNames As Variant
    Dim i As Long

    relationNames = GetRelationNames(db)

    Result = "{" & vbCrLf

    Result = Result & _
        "  ""schemaVersion"": " & SCHEMA_VERSION & "," & vbCrLf

    Result = Result & _
        "  ""exporterVersion"": " & JsonString(EXPORTER_VERSION) & "," & vbCrLf

    Result = Result & _
        "  ""relationships"": [" & vbCrLf

    If HasArrayItems(relationNames) Then

        For i = LBound(relationNames) To UBound(relationNames)

            Result = Result & _
                BuildSingleRelationshipJson( _
                    db.Relations(CStr(relationNames(i))), _
                    4)

            If i < UBound(relationNames) Then
                Result = Result & ","
            End If

            Result = Result & vbCrLf

        Next i

    End If

    Result = Result & "  ]" & vbCrLf
    Result = Result & "}" & vbCrLf

    BuildRelationshipsJson = Result

End Function


Private Function BuildSingleRelationshipJson( _
    ByVal rel As DAO.Relation, _
    ByVal indent As Long _
) As String

    Dim Result As String
    Dim i As Long
    Dim fld As DAO.Field

    Result = Spaces(indent) & "{" & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """name"": " & JsonString(rel.Name) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """primaryTable"": " & JsonString(rel.Table) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """foreignTable"": " & JsonString(rel.ForeignTable) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """referentialIntegrity"": " & _
        JsonBoolean((rel.Attributes And 2) = 0) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """cascadeUpdate"": " & _
        JsonBoolean((rel.Attributes And 256) <> 0) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """cascadeDelete"": " & _
        JsonBoolean((rel.Attributes And 4096) <> 0) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """attributes"": " & CLng(rel.Attributes) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """columns"": [" & vbCrLf

    For i = 0 To rel.Fields.Count - 1

        Set fld = rel.Fields(i)

        Result = Result & _
            Spaces(indent + 4) & "{" & vbCrLf

        Result = Result & _
            Spaces(indent + 6) & _
            """primaryColumn"": " & JsonString(fld.Name) & "," & vbCrLf

        Result = Result & _
            Spaces(indent + 6) & _
            """foreignColumn"": " & JsonString(fld.ForeignName) & vbCrLf

        Result = Result & _
            Spaces(indent + 4) & "}"

        If i < rel.Fields.Count - 1 Then
            Result = Result & ","
        End If

        Result = Result & vbCrLf

    Next i

    Result = Result & _
        Spaces(indent + 2) & "]" & vbCrLf

    Result = Result & _
        Spaces(indent) & "}"

    Set fld = Nothing

    BuildSingleRelationshipJson = Result

End Function


' ============================================================
' INDEXES
' ============================================================

Private Function BuildIndexesJson( _
    ByVal db As DAO.Database _
) As String

    Dim Result As String
    Dim tableNames As Variant
    Dim indexNames As Variant

    Dim tableIndex As Long
    Dim indexIndex As Long
    Dim totalIndexes As Long
    Dim writtenIndexes As Long

    Dim tdf As DAO.TableDef

    tableNames = GetUserTableNames(db)

    totalIndexes = CountAllIndexes(db, tableNames)

    Result = "{" & vbCrLf

    Result = Result & _
        "  ""schemaVersion"": " & SCHEMA_VERSION & "," & vbCrLf

    Result = Result & _
        "  ""exporterVersion"": " & JsonString(EXPORTER_VERSION) & "," & vbCrLf

    Result = Result & _
        "  ""indexes"": [" & vbCrLf

    If HasArrayItems(tableNames) Then

        For tableIndex = LBound(tableNames) To UBound(tableNames)

            Set tdf = db.TableDefs(CStr(tableNames(tableIndex)))

            indexNames = GetIndexNames(tdf)

            If HasArrayItems(indexNames) Then

                For indexIndex = LBound(indexNames) To UBound(indexNames)

                    Result = Result & _
                        BuildSingleIndexJson( _
                            tdf, _
                            tdf.Indexes(CStr(indexNames(indexIndex))), _
                            4)

                    writtenIndexes = writtenIndexes + 1

                    If writtenIndexes < totalIndexes Then
                        Result = Result & ","
                    End If

                    Result = Result & vbCrLf

                Next indexIndex

            End If

        Next tableIndex

    End If

    Result = Result & "  ]" & vbCrLf
    Result = Result & "}" & vbCrLf

    Set tdf = Nothing

    BuildIndexesJson = Result

End Function


Private Function BuildSingleIndexJson( _
    ByVal tdf As DAO.TableDef, _
    ByVal idx As DAO.Index, _
    ByVal indent As Long _
) As String

    Dim Result As String
    Dim i As Long
    Dim fld As DAO.Field

    Result = Spaces(indent) & "{" & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """table"": " & JsonString(tdf.Name) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """name"": " & JsonString(idx.Name) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """primary"": " & JsonBoolean(idx.Primary) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """unique"": " & JsonBoolean(idx.Unique) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """required"": " & JsonBoolean(idx.Required) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """ignoreNulls"": " & JsonBoolean(idx.IgnoreNulls) & "," & vbCrLf

    Result = Result & _
        Spaces(indent + 2) & _
        """columns"": [" & vbCrLf

    For i = 0 To idx.Fields.Count - 1

        Set fld = idx.Fields(i)

        Result = Result & _
            Spaces(indent + 4) & "{" & vbCrLf

        Result = Result & _
            Spaces(indent + 6) & _
            """name"": " & JsonString(fld.Name) & "," & vbCrLf

        Result = Result & _
            Spaces(indent + 6) & _
            """ordinal"": " & i & "," & vbCrLf

        Result = Result & _
            Spaces(indent + 6) & _
            """descending"": " & _
            JsonBoolean((fld.Attributes And 1) <> 0) & vbCrLf

        Result = Result & _
            Spaces(indent + 4) & "}"

        If i < idx.Fields.Count - 1 Then
            Result = Result & ","
        End If

        Result = Result & vbCrLf

    Next i

    Result = Result & _
        Spaces(indent + 2) & "]" & vbCrLf

    Result = Result & _
        Spaces(indent) & "}"

    Set fld = Nothing

    BuildSingleIndexJson = Result

End Function


' ============================================================
' TABLE / FIELD HELPERS
' ============================================================

Private Function IsUserTable(ByVal tdf As DAO.TableDef) As Boolean

    If Left$(tdf.Name, 4) = "MSys" Then Exit Function
    If Left$(tdf.Name, 1) = "~" Then Exit Function

    If (tdf.Attributes And dbSystemObject) <> 0 Then Exit Function

    IsUserTable = True

End Function


Private Function IsLinkedTable(ByVal tdf As DAO.TableDef) As Boolean

    IsLinkedTable = (Len(tdf.Connect) > 0)

End Function


Private Function IsPrimaryKeyField( _
    ByVal tdf As DAO.TableDef, _
    ByVal fieldName As String _
) As Boolean

    Dim idx As DAO.Index
    Dim fld As DAO.Field

    For Each idx In tdf.Indexes

        If idx.Primary Then

            For Each fld In idx.Fields

                If StrComp( _
                    fld.Name, _
                    fieldName, _
                    vbTextCompare) = 0 Then

                    IsPrimaryKeyField = True
                    Exit Function

                End If

            Next fld

        End If

    Next idx

End Function


Private Function IsAutoIncrementField( _
    ByVal fld As DAO.Field _
) As Boolean

    IsAutoIncrementField = _
        ((fld.Attributes And dbAutoIncrField) <> 0)

End Function


Private Function SafeFieldRequired( _
    ByVal fld As DAO.Field _
) As Boolean

    On Error Resume Next

    SafeFieldRequired = fld.Required

    If Err.Number <> 0 Then
        Err.Clear
        SafeFieldRequired = False
    End If

    On Error GoTo 0

End Function


Private Function GetAllowZeroLength( _
    ByVal fld As DAO.Field _
) As Variant

    On Error Resume Next

    Err.Clear

    Dim value As Variant
    value = fld.AllowZeroLength

    If Err.Number <> 0 Then

        Err.Clear
        GetAllowZeroLength = Null

    Else

        GetAllowZeroLength = CBool(value)

    End If

    On Error GoTo 0

End Function


Private Function GetFieldProperty( _
    ByVal fld As DAO.Field, _
    ByVal propertyName As String _
) As Variant

    On Error Resume Next

    Err.Clear

    Dim value As Variant
    value = fld.Properties(propertyName).value

    If Err.Number <> 0 Then

        Err.Clear
        GetFieldProperty = Null

    ElseIf IsNull(value) Then

        GetFieldProperty = Null

    ElseIf Len(CStr(value)) = 0 Then

        GetFieldProperty = Null

    Else

        GetFieldProperty = CStr(value)

    End If

    On Error GoTo 0

End Function


' ============================================================
' DAO TYPE NAMES
' ============================================================

Private Function GetDaoTypeName( _
    ByVal daoType As Long _
) As String

    Select Case daoType

        Case 1
            GetDaoTypeName = "Boolean"

        Case 2
            GetDaoTypeName = "Byte"

        Case 3
            GetDaoTypeName = "Integer"

        Case 4
            GetDaoTypeName = "Long"

        Case 5
            GetDaoTypeName = "Currency"

        Case 6
            GetDaoTypeName = "Single"

        Case 7
            GetDaoTypeName = "Double"

        Case 8
            GetDaoTypeName = "DateTime"

        Case 9
            GetDaoTypeName = "Binary"

        Case 10
            GetDaoTypeName = "ShortText"

        Case 11
            GetDaoTypeName = "LongBinary"

        Case 12
            GetDaoTypeName = "LongText"

        Case 15
            GetDaoTypeName = "GUID"

        Case 16
            GetDaoTypeName = "BigInt"

        Case 17
            GetDaoTypeName = "VarBinary"

        Case 18
            GetDaoTypeName = "Char"

        Case 19
            GetDaoTypeName = "Numeric"

        Case 20
            GetDaoTypeName = "Decimal"

        Case 21
            GetDaoTypeName = "Float"

        Case 22
            GetDaoTypeName = "Time"

        Case 23
            GetDaoTypeName = "Timestamp"

        Case Else
            GetDaoTypeName = "Unknown(" & CStr(daoType) & ")"

    End Select

End Function


' ============================================================
' SORTED NAME COLLECTIONS
' ============================================================

Private Function GetUserTableNames( _
    ByVal db As DAO.Database _
) As Variant

    Dim names() As String
    Dim Count As Long
    Dim tdf As DAO.TableDef

    For Each tdf In db.TableDefs

        If IsUserTable(tdf) Then
            Count = Count + 1
        End If

    Next tdf

    If Count = 0 Then

        GetUserTableNames = Empty
        Exit Function

    End If

    ReDim names(0 To Count - 1)

    Count = 0

    For Each tdf In db.TableDefs

        If IsUserTable(tdf) Then

            names(Count) = tdf.Name
            Count = Count + 1

        End If

    Next tdf

    SortStringArray names

    GetUserTableNames = names

End Function


Private Function GetRelationNames( _
    ByVal db As DAO.Database _
) As Variant

    Dim names() As String
    Dim rel As DAO.Relation
    Dim Count As Long

    For Each rel In db.Relations

        If Not IsSystemRelation(rel) Then
            Count = Count + 1
        End If

    Next rel

    If Count = 0 Then

        GetRelationNames = Empty
        Exit Function

    End If

    ReDim names(0 To Count - 1)

    Count = 0

    For Each rel In db.Relations

        If Not IsSystemRelation(rel) Then

            names(Count) = rel.Name
            Count = Count + 1

        End If

    Next rel

    SortStringArray names

    GetRelationNames = names

End Function


Private Function IsSystemRelation( _
    ByVal rel As DAO.Relation _
) As Boolean

    If Left$(rel.Name, 4) = "MSys" Then
        IsSystemRelation = True
        Exit Function
    End If

    If Left$(rel.Table, 4) = "MSys" Then
        IsSystemRelation = True
        Exit Function
    End If

    If Left$(rel.ForeignTable, 4) = "MSys" Then
        IsSystemRelation = True
        Exit Function
    End If

End Function


Private Function GetIndexNames( _
    ByVal tdf As DAO.TableDef _
) As Variant

    Dim names() As String
    Dim idx As DAO.Index
    Dim Count As Long

    Count = tdf.Indexes.Count

    If Count = 0 Then

        GetIndexNames = Empty
        Exit Function

    End If

    ReDim names(0 To Count - 1)

    Count = 0

    For Each idx In tdf.Indexes

        names(Count) = idx.Name
        Count = Count + 1

    Next idx

    SortStringArray names

    GetIndexNames = names

End Function


Private Sub SortStringArray( _
    ByRef values() As String _
)

    Dim i As Long
    Dim j As Long
    Dim temp As String

    For i = LBound(values) To UBound(values) - 1

        For j = i + 1 To UBound(values)

            If StrComp( _
                values(i), _
                values(j), _
                vbTextCompare) > 0 Then

                temp = values(i)
                values(i) = values(j)
                values(j) = temp

            End If

        Next j

    Next i

End Sub


Private Function HasArrayItems( _
    ByVal value As Variant _
) As Boolean

    On Error GoTo NoItems

    If IsEmpty(value) Then Exit Function

    Dim lowerBound As Long
    Dim upperBound As Long

    lowerBound = LBound(value)
    upperBound = UBound(value)

    HasArrayItems = (upperBound >= lowerBound)

    Exit Function

NoItems:
    HasArrayItems = False

End Function


Private Function CountAllIndexes( _
    ByVal db As DAO.Database, _
    ByVal tableNames As Variant _
) As Long

    Dim i As Long
    Dim total As Long

    If Not HasArrayItems(tableNames) Then
        CountAllIndexes = 0
        Exit Function
    End If

    For i = LBound(tableNames) To UBound(tableNames)

        total = total + _
            db.TableDefs(CStr(tableNames(i))).Indexes.Count

    Next i

    CountAllIndexes = total

End Function


' ============================================================
' JSON HELPERS
' ============================================================

Private Function JsonString( _
    ByVal value As String _
) As String

    JsonString = """" & JsonEscape(value) & """"

End Function


Private Function JsonNullableString( _
    ByVal value As Variant _
) As String

    If IsNull(value) Or IsEmpty(value) Then

        JsonNullableString = "null"

    ElseIf Len(CStr(value)) = 0 Then

        JsonNullableString = "null"

    Else

        JsonNullableString = JsonString(CStr(value))

    End If

End Function


Private Function JsonBoolean( _
    ByVal value As Boolean _
) As String

    If value Then
        JsonBoolean = "true"
    Else
        JsonBoolean = "false"
    End If

End Function


Private Function JsonNullableBoolean( _
    ByVal value As Variant _
) As String

    If IsNull(value) Or IsEmpty(value) Then

        JsonNullableBoolean = "null"

    Else

        JsonNullableBoolean = JsonBoolean(CBool(value))

    End If

End Function


Private Function JsonEscape( _
    ByVal value As String _
) As String

    Dim Result As String
    Dim i As Long
    Dim ch As String
    Dim code As Long

    For i = 1 To Len(value)

        ch = Mid$(value, i, 1)
        code = AscW(ch)

        Select Case code

            Case 34
                Result = Result & "\"""

            Case 92
                Result = Result & "\\"

            Case 8
                Result = Result & "\b"

            Case 9
                Result = Result & "\t"

            Case 10
                Result = Result & "\n"

            Case 12
                Result = Result & "\f"

            Case 13
                Result = Result & "\r"

            Case 0 To 31
                Result = Result & _
                    "\u" & Right$("0000" & Hex$(code), 4)

            Case Else
                Result = Result & ch

        End Select

    Next i

    JsonEscape = Result

End Function


' ============================================================
' FILE SYSTEM
' ============================================================

Private Sub EnsureFolderExists( _
    ByVal folderPath As String _
)

    Dim fso As Object
    Dim parentPath As String

    Set fso = CreateObject("Scripting.FileSystemObject")

    If fso.FolderExists(folderPath) Then
        Exit Sub
    End If

    parentPath = fso.GetParentFolderName(folderPath)

    If Len(parentPath) > 0 Then

        If Not fso.FolderExists(parentPath) Then
            EnsureFolderExists parentPath
        End If

    End If

    fso.CreateFolder folderPath

End Sub


Private Sub WriteUtf8File( _
    ByVal filePath As String, _
    ByVal text As String _
)

    Const adTypeText As Long = 2
    Const adSaveCreateOverWrite As Long = 2

    Dim stream As Object

    Set stream = CreateObject("ADODB.Stream")

    With stream

        .Type = adTypeText
        .Charset = "utf-8"
        .Open

        .WriteText text

        .SaveToFile _
            filePath, _
            adSaveCreateOverWrite

        .Close

    End With

    Set stream = Nothing

End Sub


Private Function Spaces( _
    ByVal Count As Long _
) As String

    Spaces = String$(Count, " ")

End Function

