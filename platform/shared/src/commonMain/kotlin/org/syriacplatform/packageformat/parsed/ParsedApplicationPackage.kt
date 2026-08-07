package org.syriacplatform.packageformat.parsed

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
import org.syriacplatform.packageformat.models.PackageManifest

/**
 * التمثيل القانوني الكامل للحزمة بعد فك JSON وتحويل DTOs،
 * وقبل Package Validation وRuntime Construction.
 *
 * لا يحل المراجع ولا يبني الفهارس.
 */
data class ParsedApplicationPackage(
    val manifest: PackageManifest,
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
    val melodyQintoAssignments: List<MelodyQintoAssignment>
)