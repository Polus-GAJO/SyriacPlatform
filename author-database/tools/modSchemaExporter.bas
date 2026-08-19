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

Private Const SAMPLE_EXPORT_ROOT As String = _
    "D:\SyriacPlatform\author-database\samples\mapping-analysis"


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



' ============================================================
' REPRESENTATIVE DATA EXPORT
' ============================================================

Private Const SAMPLE_EXPORT_ROOT As String = _
    "D:\SyriacPlatform\author-database\samples\mapping-analysis"

Private Const OCCASION_EXPORT_ROOT As String = _
    "D:\SyriacPlatform\author-database\exports"


Public Sub ExportRepresentativeData()

    Const OCCASION_ID As Long = 1

    On Error GoTo ErrorHandler

    ExportOccasionDataToFolder _
        OCCASION_ID, _
        SAMPLE_EXPORT_ROOT

    MsgBox _
        "Representative data exported successfully." & vbCrLf & vbCrLf & _
        "Occasion: " & OCCASION_ID & vbCrLf & _
        SAMPLE_EXPORT_ROOT, _
        vbInformation, _
        "SyriacPlatform Representative Export"

    Exit Sub

ErrorHandler:

    MsgBox _
        "Representative data export failed." & vbCrLf & vbCrLf & _
        "Error " & Err.Number & ":" & vbCrLf & _
        Err.Description, _
        vbCritical, _
        "SyriacPlatform Representative Export"

End Sub

Public Sub ExportOccasionData(ByVal occasionId As Long)

    Dim outputRoot As String

    On Error GoTo ErrorHandler

    If occasionId <= 0 Then
        Err.Raise _
            vbObjectError + 1000, _
            "ExportOccasionData", _
            "Occasion id must be positive."
    End If

    outputRoot = _
        OCCASION_EXPORT_ROOT & _
        "\occasion-" & _
        CStr(occasionId)

    ExportOccasionDataToFolder _
        occasionId, _
        outputRoot

    MsgBox _
        "Occasion data exported successfully." & vbCrLf & vbCrLf & _
        "Occasion: " & occasionId & vbCrLf & _
        outputRoot, _
        vbInformation, _
        "SyriacPlatform Occasion Export"

    Exit Sub

ErrorHandler:

    MsgBox _
        "Occasion data export failed." & vbCrLf & vbCrLf & _
        "Error " & Err.Number & ":" & vbCrLf & _
        Err.Description, _
        vbCritical, _
        "SyriacPlatform Occasion Export"

End Sub

Private Sub ExportOccasionDataToFolder( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    EnsureFolderExists outputRoot

    ExportOccasion occasionId, outputRoot
    ExportOccaExis occasionId, outputRoot
    ExportExistsIn occasionId, outputRoot
    ExportExistsInText occasionId, outputRoot
    ExportPetExis occasionId, outputRoot
    ExportPrayers occasionId, outputRoot
    ExportQolos occasionId, outputRoot
    ExportQintos occasionId, outputRoot
    ExportMelodies occasionId, outputRoot
    ExportTexts occasionId, outputRoot
    ExportPetgomos occasionId, outputRoot

End Sub



Private Sub ExportOccasion( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    Dim sqlText As String

    sqlText = _
        "SELECT * FROM Occasion " & _
        "WHERE OccN = " & occasionId & ";"

    ExportQueryToCsv _
        sqlText, _
        outputRoot & "\Occasion.csv"

End Sub


Private Sub ExportOccaExis( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    Dim sqlText As String

    sqlText = _
        "SELECT * FROM OccaExis " & _
        "WHERE OccN = " & occasionId & " " & _
        "ORDER BY OccaExisID;"

    ExportQueryToCsv _
        sqlText, _
        outputRoot & "\OccaExis.csv"

End Sub


Private Sub ExportExistsIn( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    Dim sqlText As String

    sqlText = "SELECT DISTINCT E.* "
    sqlText = sqlText & "FROM ExistsIn AS E "
    sqlText = sqlText & "INNER JOIN OccaExis AS OE "
    sqlText = sqlText & "ON E.ID = OE.ExistInID "
    sqlText = sqlText & "WHERE OE.OccN = " & occasionId & " "
    sqlText = sqlText & "ORDER BY E.PrayerN, E.Sort, E.ID;"

    ExportQueryToCsv _
        sqlText, _
        outputRoot & "\ExistsIn.csv"

End Sub


Private Sub ExportExistsInText( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    Dim sqlText As String

    sqlText = "SELECT EIT.* "
    sqlText = sqlText & "FROM ExistsInText AS EIT "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT DISTINCT E.ID "
    sqlText = sqlText & "FROM ExistsIn AS E "
    sqlText = sqlText & "INNER JOIN OccaExis AS OE "
    sqlText = sqlText & "ON E.ID = OE.ExistInID "
    sqlText = sqlText & "WHERE OE.OccN = " & occasionId
    sqlText = sqlText & ") AS X "
    sqlText = sqlText & "ON EIT.ExistsInID = X.ID "
    sqlText = sqlText & _
        "ORDER BY EIT.ExistsInID, EIT.SortInPra, EIT.ID;"

    ExportQueryToCsv _
        sqlText, _
        outputRoot & "\ExistsInText.csv"

End Sub


Private Sub ExportPetExis( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    Dim sqlText As String

    sqlText = "SELECT PE.* "
    sqlText = sqlText & "FROM PetExis AS PE "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT EIT.ID "
    sqlText = sqlText & "FROM ExistsInText AS EIT "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT DISTINCT E.ID "
    sqlText = sqlText & "FROM ExistsIn AS E "
    sqlText = sqlText & "INNER JOIN OccaExis AS OE "
    sqlText = sqlText & "ON E.ID = OE.ExistInID "
    sqlText = sqlText & "WHERE OE.OccN = " & occasionId
    sqlText = sqlText & ") AS X "
    sqlText = sqlText & "ON EIT.ExistsInID = X.ID"
    sqlText = sqlText & ") AS Y "
    sqlText = sqlText & "ON PE.ExistInTextID = Y.ID "
    sqlText = sqlText & _
        "ORDER BY PE.ExistInTextID, PE.PetExistID;"

    ExportQueryToCsv _
        sqlText, _
        outputRoot & "\PetExis.csv"

End Sub


Private Sub ExportPrayers( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    Dim sqlText As String

    sqlText = "SELECT DISTINCT P.* "
    sqlText = sqlText & "FROM Prayers AS P "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT DISTINCT E.PrayerN "
    sqlText = sqlText & "FROM ExistsIn AS E "
    sqlText = sqlText & "INNER JOIN OccaExis AS OE "
    sqlText = sqlText & "ON E.ID = OE.ExistInID "
    sqlText = sqlText & "WHERE OE.OccN = " & occasionId & " "
    sqlText = sqlText & "AND E.PrayerN IS NOT NULL"
    sqlText = sqlText & ") AS X "
    sqlText = sqlText & "ON P.PrayerN = X.PrayerN "
    sqlText = sqlText & "ORDER BY P.PrayerN;"

    ExportQueryToCsv _
        sqlText, _
        outputRoot & "\Prayers.csv"

End Sub


Private Sub ExportQolos( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    Dim sqlText As String

    sqlText = "SELECT DISTINCT Q.* "
    sqlText = sqlText & "FROM Qolos AS Q "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT DISTINCT E.QoloN "
    sqlText = sqlText & "FROM ExistsIn AS E "
    sqlText = sqlText & "INNER JOIN OccaExis AS OE "
    sqlText = sqlText & "ON E.ID = OE.ExistInID "
    sqlText = sqlText & "WHERE OE.OccN = " & occasionId & " "
    sqlText = sqlText & "AND E.QoloN IS NOT NULL"
    sqlText = sqlText & ") AS X "
    sqlText = sqlText & "ON Q.QoloN = X.QoloN "
    sqlText = sqlText & "ORDER BY Q.QoloN;"

    ExportQueryToCsv _
        sqlText, _
        outputRoot & "\Qolos.csv"

End Sub



Private Sub ExportQintos( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    Dim sqlText As String

    sqlText = "SELECT DISTINCT QI.* "
    sqlText = sqlText & "FROM Qinto AS QI "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT DISTINCT E.QintoN "
    sqlText = sqlText & "FROM ExistsIn AS E "
    sqlText = sqlText & "INNER JOIN OccaExis AS OE "
    sqlText = sqlText & "ON E.ID = OE.ExistInID "
    sqlText = sqlText & "WHERE OE.OccN = " & occasionId & " "
    sqlText = sqlText & "AND E.QintoN IS NOT NULL"
    sqlText = sqlText & ") AS X "
    sqlText = sqlText & "ON QI.QintoN = X.QintoN "
    sqlText = sqlText & "ORDER BY QI.QintoN;"

    ExportQueryToCsv _
        sqlText, _
        outputRoot & "\Qinto.csv"

End Sub


Private Sub ExportMelodies( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    Dim sqlText As String

    sqlText = "SELECT DISTINCT M.* "
    sqlText = sqlText & "FROM Melody AS M "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT DISTINCT E.QoloN "
    sqlText = sqlText & "FROM ExistsIn AS E "
    sqlText = sqlText & "INNER JOIN OccaExis AS OE "
    sqlText = sqlText & "ON E.ID = OE.ExistInID "
    sqlText = sqlText & "WHERE OE.OccN = " & occasionId & " "
    sqlText = sqlText & "AND E.QoloN IS NOT NULL"
    sqlText = sqlText & ") AS X "
    sqlText = sqlText & "ON M.QoloN = X.QoloN "
    sqlText = sqlText & _
        "ORDER BY M.QoloN, M.QintoN, M.MelodyN;"

    ExportQueryToCsv _
        sqlText, _
        outputRoot & "\Melody.csv"

End Sub


Private Sub ExportTexts( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    Dim sqlText As String

    sqlText = "SELECT T.* "
    sqlText = sqlText & "FROM Texts AS T "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT DISTINCT EIT.TextID "
    sqlText = sqlText & "FROM ExistsInText AS EIT "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT DISTINCT E.ID "
    sqlText = sqlText & "FROM ExistsIn AS E "
    sqlText = sqlText & "INNER JOIN OccaExis AS OE "
    sqlText = sqlText & "ON E.ID = OE.ExistInID "
    sqlText = sqlText & "WHERE OE.OccN = " & occasionId
    sqlText = sqlText & ") AS X "
    sqlText = sqlText & "ON EIT.ExistsInID = X.ID "
    sqlText = sqlText & "WHERE EIT.TextID IS NOT NULL"
    sqlText = sqlText & ") AS Y "
    sqlText = sqlText & "ON T.TextID = Y.TextID "
    sqlText = sqlText & "ORDER BY T.TextID;"

    ExportQueryToCsv _
        sqlText, _
        outputRoot & "\Texts.csv"

End Sub


Private Sub ExportPetgomos( _
    ByVal occasionId As Long, _
    ByVal outputRoot As String _
)

    Dim sqlText As String

    sqlText = "SELECT P.* "
    sqlText = sqlText & "FROM Petgomo AS P "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT DISTINCT PE.PetN "
    sqlText = sqlText & "FROM PetExis AS PE "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT EIT.ID "
    sqlText = sqlText & "FROM ExistsInText AS EIT "
    sqlText = sqlText & "INNER JOIN "
    sqlText = sqlText & "("
    sqlText = sqlText & "SELECT DISTINCT E.ID "
    sqlText = sqlText & "FROM ExistsIn AS E "
    sqlText = sqlText & "INNER JOIN OccaExis AS OE "
    sqlText = sqlText & "ON E.ID = OE.ExistInID "
    sqlText = sqlText & "WHERE OE.OccN = " & occasionId
    sqlText = sqlText & ") AS X "
    sqlText = sqlText & "ON EIT.ExistsInID = X.ID"
    sqlText = sqlText & ") AS Y "
    sqlText = sqlText & "ON PE.ExistInTextID = Y.ID "
    sqlText = sqlText & "WHERE PE.PetN IS NOT NULL"
    sqlText = sqlText & ") AS Z "
    sqlText = sqlText & "ON P.PetN = Z.PetN "
    sqlText = sqlText & "ORDER BY P.PetN;"

    ExportQueryToCsv _
        sqlText, _
        outputRoot & "\Petgomo.csv"

End Sub




Private Sub ExportQueryToCsv( _
    ByVal sqlText As String, _
    ByVal filePath As String _
)

    Dim db As DAO.Database
    Dim rs As DAO.Recordset

    Set db = CurrentDb
    Set rs = db.OpenRecordset(sqlText, dbOpenSnapshot)

    WriteRecordsetToUtf8Csv rs, filePath

    rs.Close

    Set rs = Nothing
    Set db = Nothing

End Sub


Private Sub WriteRecordsetToUtf8Csv( _
    ByVal rs As DAO.Recordset, _
    ByVal filePath As String _
)

    Dim output As String
    Dim fieldIndex As Long

    For fieldIndex = 0 To rs.Fields.Count - 1

        If fieldIndex > 0 Then
            output = output & ","
        End If

        output = output & _
            CsvValue(rs.Fields(fieldIndex).Name)

    Next fieldIndex

    output = output & vbCrLf

    Do While Not rs.EOF

        For fieldIndex = 0 To rs.Fields.Count - 1

            If fieldIndex > 0 Then
                output = output & ","
            End If

            output = output & _
                CsvValue(rs.Fields(fieldIndex).value)

        Next fieldIndex

        output = output & vbCrLf

        rs.MoveNext

    Loop

    WriteUtf8File filePath, output

End Sub


Private Function CsvValue(ByVal value As Variant) As String

    Dim text As String

    If IsNull(value) Then
        CsvValue = ""
        Exit Function
    End If

    If VarType(value) = vbBoolean Then

        If CBool(value) Then
            text = "true"
        Else
            text = "false"
        End If

    ElseIf VarType(value) = vbDate Then

        text = Format$( _
            CDate(value), _
            "yyyy-mm-dd hh:nn:ss" _
        )

    Else
        text = CStr(value)
    End If

    text = Replace(text, """", """""")

    CsvValue = """" & text & """"

End Function
