package org.syriacplatform.content.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.GroupId
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PetgomoId
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.EntryPointTarget
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Petgomo
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.models.TextContent
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.content.models.LiturgicalTextRef

class RuntimeContentResolverTest {

    @Test
    fun resolverBuildsCompleteLiturgicalTraversalAndPreservesOrderAndRepetition() {
        val textItemId =
            LiturgicalItemId(501)

        val qoloItemId =
            LiturgicalItemId(502)

        val packageData =
            packageWith(
                entryPoints = listOf(
                    EntryPoint(
                        id = EntryPointId(101),
                        name = "Main Entry Point",
                        target =
                            EntryPointTarget.Occasion(
                                occasionId =
                                    OccasionId(201)
                            ),
                        isDefault = true
                    )
                ),

                occasions = listOf(
                    Occasion(
                        id = OccasionId(201),
                        name = "Test Occasion",
                        description = null,
                        prayerSequenceIds = listOf(
                            PrayerSequenceId(301)
                        )
                    )
                ),

                prayers = listOf(
                    Prayer(
                        id = PrayerId(401),
                        name = "Test Prayer",
                        description = null
                    )
                ),

                prayerSequences = listOf(
                    PrayerSequence(
                        id = PrayerSequenceId(301),
                        prayerId = PrayerId(401),

                        /*
                         * التكرار مقصود:
                         *
                         * Text
                         * Qolo
                         * Text
                         *
                         * نريد إثبات أن Runtime لا يحذف
                         * الظهور الثاني للعنصر نفسه.
                         */
                        liturgicalItemIds = listOf(
                            textItemId,
                            qoloItemId,
                            textItemId
                        )
                    )
                ),

                liturgicalItems = listOf(
                    LiturgicalItem(
                        id = textItemId,
                        target =
                            LiturgicalItemTarget.Text(
                                textId = TextId(601),
                                petgomoId =
                                    PetgomoId(701)
                            )
                    ),
                    LiturgicalItem(
                        id = qoloItemId,
                        target =
                            LiturgicalItemTarget.Qolo(
                                qoloId = QoloId(801),
                                effectiveMelodyId = MelodyId(901),
                                verses = listOf(
                                    LiturgicalTextRef(
                                        textId = TextId(601),
                                        petgomoId = PetgomoId(701)
                                    ),
                                    LiturgicalTextRef(
                                        textId = TextId(601),
                                        petgomoId = null
                                    )
                                )
                            )
                    )
                ),

                texts = listOf(
                    TextContent(
                        id = TextId(601),
                        syriac = "Test Syriac Text",
                        translations = emptyList()
                    )
                ),

                petgomos = listOf(
                    Petgomo(
                        id = PetgomoId(701),
                        syriac = "Test Petgomo",
                        translations = emptyList()
                    )
                ),

                qolos = listOf(
                    Qolo(
                        id = QoloId(801),
                        groupId = GroupId(12),
                        sort = 500,
                        name = "Test Qolo",
                        searchName = "Test Qolo",
                        poeticMeter = null
                    )
                ),

                melodies = listOf(
                    Melody(
                        id = MelodyId(901),
                        qoloId = QoloId(801),
                        name = "Test Melody",
                        searchName = "Test Melody",
                        hasRecording = false
                    )
                )
            )

        val store =
            RuntimeContentStore.from(
                packageData
            )

        val resolver =
            RuntimeContentResolver(
                store = store
            )

        val result =
            resolver.resolveDefaultEntryPoint()

        val success =
            assertIs<Result.Success<RuntimeEntryPoint>>(
                result
            )

        val runtimeEntryPoint =
            success.data

        assertEquals(
            EntryPointId(101),
            runtimeEntryPoint.entryPoint.id
        )

        val runtimeOccasion =
            runtimeEntryPoint.occasion

        assertEquals(
            OccasionId(201),
            runtimeOccasion.occasion.id
        )

        assertEquals(
            1,
            runtimeOccasion.prayerSequences.size
        )

        val runtimeSequence =
            runtimeOccasion
                .prayerSequences
                .single()

        assertEquals(
            PrayerSequenceId(301),
            runtimeSequence.sequence.id
        )

        assertEquals(
            PrayerId(401),
            runtimeSequence.prayer.id
        )

        assertEquals(
            "Test Prayer",
            runtimeSequence.prayer.name
        )

        /*
         * الترتيب والتكرار يجب أن يبقيا:
         *
         * 501 → 502 → 501
         */
        assertEquals(
            listOf(
                textItemId,
                qoloItemId,
                textItemId
            ),
            runtimeSequence.items.map { item ->
                item.item.id
            }
        )

        assertEquals(
            3,
            runtimeSequence.items.size
        )

        /*
         * العنصر الأول: Text + Petgomo
         */
        val firstTarget =
            assertIs<
                    ResolvedLiturgicalItemTarget.Text
                    >(
                runtimeSequence.items[0].target
            )

        assertEquals(
            TextId(601),
            firstTarget.text.id
        )

        assertEquals(
            "Test Syriac Text",
            firstTarget.text.syriac
        )

        assertEquals(
            PetgomoId(701),
            firstTarget.petgomo?.id
        )

        /*
         * العنصر الثاني: Qolo + effective Melody
         */
        val secondTarget =
            assertIs<
                    ResolvedLiturgicalItemTarget.Qolo
                    >(
                runtimeSequence.items[1].target
            )

        assertEquals(
            QoloId(801),
            secondTarget.qolo.id
        )

        assertEquals(
            MelodyId(901),
            secondTarget.effectiveMelody.id
        )

        assertEquals(
            2,
            secondTarget.verses.size
        )

        assertEquals(
            QoloId(801),
            secondTarget.effectiveMelody.qoloId
        )

        /*
         * أبيات الترتيلة نفسها تحفظ الترتيب والتكرار،
         * ويحتفظ كل ظهور بسياق Petgomo الخاص به.
         */
        assertEquals(
            TextId(601),
            secondTarget.verses[0].text.id
        )

        assertEquals(
            PetgomoId(701),
            secondTarget.verses[0].petgomo?.id
        )

        assertEquals(
            TextId(601),
            secondTarget.verses[1].text.id
        )

        assertNull(
            secondTarget.verses[1].petgomo
        )

        /*
         * العنصر الثالث هو الظهور الثاني
         * لنفس LiturgicalItem الأول.
         */
        val thirdTarget =
            assertIs<
                    ResolvedLiturgicalItemTarget.Text
                    >(
                runtimeSequence.items[2].target
            )

        assertEquals(
            TextId(601),
            thirdTarget.text.id
        )

        assertEquals(
            PetgomoId(701),
            thirdTarget.petgomo?.id
        )
    }

    @Test
    fun textWithoutPetgomoResolvesWithNullPetgomo() {
        val packageData =
            packageWith(
                liturgicalItems = listOf(
                    LiturgicalItem(
                        id = LiturgicalItemId(501),
                        target =
                            LiturgicalItemTarget.Text(
                                textId = TextId(601),
                                petgomoId = null
                            )
                    )
                ),
                texts = listOf(
                    TextContent(
                        id = TextId(601),
                        syriac = "Text without Petgomo",
                        translations = emptyList()
                    )
                )
            )

        val resolver =
            RuntimeContentResolver(
                store =
                    RuntimeContentStore.from(
                        packageData
                    )
            )

        val result =
            resolver.resolveLiturgicalItem(
                LiturgicalItemId(501)
            )

        val success =
            assertIs<
                    Result.Success<ResolvedLiturgicalItem>
                    >(
                result
            )

        val target =
            assertIs<
                    ResolvedLiturgicalItemTarget.Text
                    >(
                success.data.target
            )

        assertNull(
            target.petgomo
        )
    }
}