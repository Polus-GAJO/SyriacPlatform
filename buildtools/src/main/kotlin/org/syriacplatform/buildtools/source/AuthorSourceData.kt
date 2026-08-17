package org.syriacplatform.buildtools.source

import org.syriacplatform.buildtools.source.models.ExistsInSource
import org.syriacplatform.buildtools.source.models.ExistsInTextSource
import org.syriacplatform.buildtools.source.models.MelodySource
import org.syriacplatform.buildtools.source.models.OccaExisSource
import org.syriacplatform.buildtools.source.models.OccasionSource
import org.syriacplatform.buildtools.source.models.PetExisSource
import org.syriacplatform.buildtools.source.models.PetgomoSource
import org.syriacplatform.buildtools.source.models.PrayerSource
import org.syriacplatform.buildtools.source.models.QintoSource
import org.syriacplatform.buildtools.source.models.QoloSource
import org.syriacplatform.buildtools.source.models.TextSource

data class AuthorSourceData(
    val occasion: OccasionSource,
    val prayers: List<PrayerSource>,
    val occasionLinks: List<OccaExisSource>,
    val existsIn: List<ExistsInSource>,
    val existsInTexts: List<ExistsInTextSource>,
    val petExis: List<PetExisSource>,
    val qolos: List<QoloSource>,
    val texts: List<TextSource>,
    val petgomos: List<PetgomoSource>,
    val melodies: List<MelodySource>,
    val qintos: List<QintoSource>
)