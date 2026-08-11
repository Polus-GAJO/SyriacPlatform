package org.syriacplatform.packagevalidation

import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.models.MelodyQintoAssignment
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Petgomo
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.content.models.Qinto
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.models.TextContent
import org.syriacplatform.packageformat.models.ApplicationInfo
import org.syriacplatform.packageformat.models.BuildInfo
import org.syriacplatform.packageformat.models.CompatibilityInfo
import org.syriacplatform.packageformat.models.PackageManifest
import org.syriacplatform.packageformat.models.PackageProfile
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packageformat.parsed.PackageCollectionPresence

object PackageValidationTestFixture {

    fun validManifest(): PackageManifest {
        return PackageManifest(
            packageId = "test.package",
            packageName = "Test Package",
            schemaVersion = "1.0",
            packageVersion = "1.0.0",
            contentVersion = "1.0.0",
            application = ApplicationInfo(
                id = "test",
                name = "Test",
                platform = "SyriacPlatform",
                defaultLanguage = "en"
            ),
            profile = PackageProfile.OCCASION,
            build = BuildInfo(
                generatedAt = "2026-08-07T00:00:00Z",
                buildTool = "Test",
                buildVersion = "1.0.0",
                buildRevision = "test-001"
            ),
            compatibility = CompatibilityInfo(
                minimumCoreVersion = "1.0.0",
                targetSchemaVersion = "1.0",
                supportedFeatures = emptyList()
            )
        )
    }
    fun allCollectionsPresent(): PackageCollectionPresence {
        return PackageCollectionPresence(
            entryPoints = true,
            occasions = true,
            prayers = true,
            prayerSequences = true,
            liturgicalItems = true,
            texts = true,
            qolos = true,
            melodies = true,
            qintos = true,
            petgomos = true,
            melodyQintoAssignments = true
        )
    }
    fun packageWith(
        manifest: PackageManifest = validManifest(),
        collectionPresence: PackageCollectionPresence =
            allCollectionsPresent(),
        entryPoints: List<EntryPoint> = emptyList(),
        occasions: List<Occasion> = emptyList(),
        prayers: List<Prayer> = emptyList(),
        prayerSequences: List<PrayerSequence> = emptyList(),
        liturgicalItems: List<LiturgicalItem> = emptyList(),
        texts: List<TextContent> = emptyList(),
        petgomos: List<Petgomo> = emptyList(),
        qolos: List<Qolo> = emptyList(),
        melodies: List<Melody> = emptyList(),
        qintos: List<Qinto> = emptyList(),
        melodyQintoAssignments: List<MelodyQintoAssignment> = emptyList()
    ): ParsedApplicationPackage {
        return ParsedApplicationPackage(
            manifest = manifest,
            collectionPresence = collectionPresence,
            entryPoints = entryPoints,
            occasions = occasions,
            prayers = prayers,
            prayerSequences = prayerSequences,
            liturgicalItems = liturgicalItems,
            texts = texts,
            petgomos = petgomos,
            qolos = qolos,
            melodies = melodies,
            qintos = qintos,
            melodyQintoAssignments = melodyQintoAssignments
        )
    }
}