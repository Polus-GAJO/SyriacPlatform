package org.syriacplatform.packageformat

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.dto.PackageManifestJsonDto
import org.syriacplatform.packageformat.mappers.toDomain
import org.syriacplatform.packageformat.models.PackageManifest
import org.syriacplatform.packageformat.models.PackageProfile

class PackageManifestTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun completeManifestIsDecodedAndMapped() {
        val dto = json.decodeFromString<PackageManifestJsonDto>(
            validManifestJson
        )

        val result = dto.toDomain()
        val success =
            assertIs<Result.Success<PackageManifest>>(result)

        val manifest = success.data

        assertEquals(
            "demo.occasions",
            manifest.packageId
        )

        assertEquals(
            "Occasions Demo Package",
            manifest.packageName
        )

        assertEquals(
            "1.0",
            manifest.schemaVersion
        )

        assertEquals(
            "1.0.0",
            manifest.packageVersion
        )

        assertEquals(
            "1.0.0",
            manifest.contentVersion
        )

        assertEquals(
            PackageProfile.OCCASION,
            manifest.profile
        )

        assertEquals(
            "occasions",
            manifest.application.id
        )

        assertEquals(
            "en",
            manifest.application.defaultLanguage
        )

        assertEquals(
            "20260810-1430-001",
            manifest.build.buildRevision
        )

        assertEquals(
            "1.0.0",
            manifest.compatibility.minimumCoreVersion
        )

        assertEquals(
            listOf(
                "canonical-content",
                "media-assets",
                "search-index"
            ),
            manifest.compatibility.supportedFeatures
        )
    }

    @Test
    fun unsupportedProfileProducesFailure() {
        val dto = json.decodeFromString<PackageManifestJsonDto>(
            validManifestJson.replace(
                "\"Occasion\"",
                "\"UnsupportedProfile\""
            )
        )

        val result = dto.toDomain()
        val failure =
            assertIs<Result.Failure>(result)

        assertEquals(
            ErrorCode.UNSUPPORTED_PACKAGE_PROFILE,
            failure.error.code
        )

        assertEquals(
            "Unsupported package profile: UnsupportedProfile",
            failure.error.message
        )
    }

    @Test
    fun fullLibraryProfileSupportsCanonicalSerializedForm() {
        val dto = json.decodeFromString<PackageManifestJsonDto>(
            validManifestJson.replace(
                "\"Occasion\"",
                "\"FullLibrary\""
            )
        )

        val result = dto.toDomain()
        val success =
            assertIs<Result.Success<PackageManifest>>(result)

        assertEquals(
            PackageProfile.FULL_LIBRARY,
            success.data.profile
        )
    }

    @Test
    fun mapperPreservesSupportedFeatureOrder() {
        val dto = json.decodeFromString<PackageManifestJsonDto>(
            validManifestJson
        )

        val result = dto.toDomain()
        val success =
            assertIs<Result.Success<PackageManifest>>(result)

        assertEquals(
            listOf(
                "canonical-content",
                "media-assets",
                "search-index"
            ),
            success.data.compatibility.supportedFeatures
        )
    }

    private companion object {

        val validManifestJson = """
            {
              "packageId": "demo.occasions",
              "packageName": "Occasions Demo Package",

              "schemaVersion": "1.0",
              "packageVersion": "1.0.0",
              "contentVersion": "1.0.0",

              "application": {
                "id": "occasions",
                "name": "Occasions",
                "platform": "SyriacPlatform",
                "defaultLanguage": "en"
              },

              "profile": "Occasion",

              "build": {
                "generatedAt": "2026-08-10T14:30:00Z",
                "buildTool": "SyriacPlatform Build Tools",
                "buildVersion": "1.0.0",
                "buildRevision": "20260810-1430-001"
              },

              "compatibility": {
                "minimumCoreVersion": "1.0.0",
                "targetSchemaVersion": "1.0",
                "supportedFeatures": [
                  "canonical-content",
                  "media-assets",
                  "search-index"
                ]
              }
            }
        """.trimIndent()
    }
}