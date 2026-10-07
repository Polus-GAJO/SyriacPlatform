package org.syriacplatform.content.runtime

import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.MediaSegment
import org.syriacplatform.content.models.MediaTimingSet
import org.syriacplatform.content.models.PerformanceMedia
import org.syriacplatform.content.models.TextOccurrenceMediaSegment
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.models.MelodyQintoAssignment
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Petgomo
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.content.models.Qinto
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.models.TextContent
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage

/**
 * المحتوى القانوني الجاهز للاستخدام داخل Runtime.
 *
 * لا يحمل معلومات parsing أو collection presence أو manifest.
 * هذه المعلومات تخص Application Package نفسها.
 *
 * RuntimeContent يحتوي فقط على الكيانات التي يحتاجها
 * Core بعد نجاح Package Validation.
 */
data class RuntimeContent(
    val entryPoints: List<EntryPoint>,
    val occasions: List<Occasion>,
    val prayers: List<Prayer>,
    val prayerSequences: List<PrayerSequence>,
    val liturgicalItems: List<LiturgicalItem>,
    val texts: List<TextContent>,
    val petgomos: List<Petgomo>,
    val qolos: List<Qolo>,
    val melodies: List<Melody>,
    val qintos: List<Qinto>,
    val melodyQintoAssignments: List<MelodyQintoAssignment>,
    val mediaAssets: List<MediaAsset> = emptyList(),
    val performanceMedia: List<PerformanceMedia> = emptyList(),
    val mediaTimingSets: List<MediaTimingSet> = emptyList(),
    val mediaSegments: List<MediaSegment> = emptyList(),
    val textOccurrenceMediaSegments:
        List<TextOccurrenceMediaSegment> = emptyList()
) {

    companion object {

        fun from(
            packageData: ParsedApplicationPackage
        ): RuntimeContent {
            return RuntimeContent(
                entryPoints = packageData.entryPoints,
                occasions = packageData.occasions,
                prayers = packageData.prayers,
                prayerSequences = packageData.prayerSequences,
                liturgicalItems = packageData.liturgicalItems,
                texts = packageData.texts,
                petgomos = packageData.petgomos,
                qolos = packageData.qolos,
                melodies = packageData.melodies,
                qintos = packageData.qintos,
                melodyQintoAssignments =
                    packageData.melodyQintoAssignments,
                mediaAssets =
                    packageData.mediaAssets,
                performanceMedia =
                    packageData.performanceMedia,
                mediaTimingSets =
                    packageData.mediaTimingSets,
                mediaSegments =
                    packageData.mediaSegments,
                textOccurrenceMediaSegments =
                    packageData.textOccurrenceMediaSegments
            )
        }
    }
}