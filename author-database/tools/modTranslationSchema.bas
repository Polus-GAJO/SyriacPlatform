Attribute VB_Name = "modTranslationSchema"
Option Compare Database
Option Explicit

' SyriacPlatform translation schema v1.
' Run InstallTranslationSchema once from the VBA Immediate Window.
' Make a backup of the Access database before running.
' This installer creates only new tables and relations; it never alters
' Texts, Petgomo or existing queries/forms/reports.

Public Sub InstallTranslationSchema()
    Dim db As DAO.Database
    Set db = CurrentDb

    If TableExists(db, "Languages") Or TableExists(db, "Translations") _
        Or TableExists(db, "TranslationLinks") Then
        Err.Raise vbObjectError + 5100, "InstallTranslationSchema", _
            "At least one translation table already exists. No changes made; inspect before retrying."
    End If

    CreateLanguages db
    CreateTranslations db
    CreateTranslationLinks db

    AddForeignKey db, "relTranslationsLanguage", "Languages", "LanguageID", _
                  "Translations", "LanguageID"
    AddForeignKey db, "relTranslationLinksTranslation", "Translations", "TranslationID", _
                  "TranslationLinks", "TranslationID"
    AddForeignKey db, "relTranslationLinksText", "Texts", "TextID", _
                  "TranslationLinks", "TextID"
    AddForeignKey db, "relTranslationLinksPetgomo", "Petgomo", "PetN", _
                  "TranslationLinks", "PetN"

    db.Execute "INSERT INTO Languages (LanguageCode, LanguageName) VALUES ('ar', 'Arabic')", dbFailOnError
    db.Execute "INSERT INTO Languages (LanguageCode, LanguageName) VALUES ('en', 'English')", dbFailOnError
    db.Execute "INSERT INTO Languages (LanguageCode, LanguageName) VALUES ('fr', 'French')", dbFailOnError

    MsgBox "Translation tables, relations and initial languages created.", vbInformation
End Sub

Private Sub CreateLanguages(ByVal db As DAO.Database)
    Dim t As DAO.TableDef
    Set t = db.CreateTableDef("Languages")
    AddAutoNumber t, "LanguageID"
    AddText t, "LanguageCode", 35, True
    AddText t, "LanguageName", 100, True
    db.TableDefs.Append t
    AddIndex db.TableDefs("Languages"), "PK_Languages", "LanguageID", True, True
    AddIndex db.TableDefs("Languages"), "UX_Languages_Code", "LanguageCode", True, False
End Sub

Private Sub CreateTranslations(ByVal db As DAO.Database)
    Dim t As DAO.TableDef
    Set t = db.CreateTableDef("Translations")
    AddAutoNumber t, "TranslationID"
    AddLong t, "LanguageID", True
    t.Fields.Append t.CreateField("Translation", dbMemo)
    AddText t, "Translator", 255, False
    AddText t, "Source", 255, False
    AddText t, "ReviewStatus", 30, True
    db.TableDefs.Append t
    AddIndex db.TableDefs("Translations"), "PK_Translations", "TranslationID", True, True
    AddIndex db.TableDefs("Translations"), "IX_Translations_Language", "LanguageID", False, False
End Sub

Private Sub CreateTranslationLinks(ByVal db As DAO.Database)
    Dim t As DAO.TableDef
    Set t = db.CreateTableDef("TranslationLinks")
    AddAutoNumber t, "LinkID"
    AddLong t, "TranslationID", True
    AddLong t, "TextID", False
    AddLong t, "PetN", False
    t.Fields.Append t.CreateField("IsPublished", dbBoolean)
    ' Exactly one target per link; both empty or both filled is invalid.
    t.ValidationRule = "([TextID] Is Not Null And [PetN] Is Null) Or " & _
                       "([TextID] Is Null And [PetN] Is Not Null)"
    t.ValidationText = "Select exactly one target: TextID or PetN."
    db.TableDefs.Append t
    AddIndex db.TableDefs("TranslationLinks"), "PK_TranslationLinks", "LinkID", True, True
    AddIndex db.TableDefs("TranslationLinks"), "IX_Links_Translation", "TranslationID", False, False
    AddIndex db.TableDefs("TranslationLinks"), "IX_Links_Text", "TextID", False, False
    AddIndex db.TableDefs("TranslationLinks"), "IX_Links_Pet", "PetN", False, False
End Sub

Private Sub AddAutoNumber(ByVal t As DAO.TableDef, ByVal fieldName As String)
    Dim f As DAO.Field
    Set f = t.CreateField(fieldName, dbLong)
    f.Attributes = dbAutoIncrField
    t.Fields.Append f
End Sub

Private Sub AddLong(ByVal t As DAO.TableDef, ByVal fieldName As String, ByVal required As Boolean)
    Dim f As DAO.Field
    Set f = t.CreateField(fieldName, dbLong)
    f.Required = required
    t.Fields.Append f
End Sub

Private Sub AddText(ByVal t As DAO.TableDef, ByVal fieldName As String, _
                    ByVal size As Integer, ByVal required As Boolean)
    Dim f As DAO.Field
    Set f = t.CreateField(fieldName, dbText, size)
    f.Required = required
    f.AllowZeroLength = False
    t.Fields.Append f
End Sub

Private Sub AddIndex(ByVal t As DAO.TableDef, ByVal indexName As String, _
                     ByVal fieldName As String, ByVal unique As Boolean, _
                     ByVal primary As Boolean)
    Dim ix As DAO.Index
    Set ix = t.CreateIndex(indexName)
    ix.Fields.Append ix.CreateField(fieldName)
    ix.Unique = unique
    ix.Primary = primary
    t.Indexes.Append ix
End Sub

Private Sub AddForeignKey(ByVal db As DAO.Database, ByVal relationName As String, _
                          ByVal parentTable As String, ByVal parentField As String, _
                          ByVal childTable As String, ByVal childField As String)
    Dim rel As DAO.Relation
    Dim f As DAO.Field
    Set rel = db.CreateRelation(relationName, parentTable, childTable)
    Set f = rel.CreateField(parentField)
    f.ForeignName = childField
    rel.Fields.Append f
    db.Relations.Append rel
End Sub

Private Function TableExists(ByVal db As DAO.Database, ByVal tableName As String) As Boolean
    Dim t As DAO.TableDef
    For Each t In db.TableDefs
        If StrComp(t.Name, tableName, vbTextCompare) = 0 Then
            TableExists = True
            Exit Function
        End If
    Next t
End Function
