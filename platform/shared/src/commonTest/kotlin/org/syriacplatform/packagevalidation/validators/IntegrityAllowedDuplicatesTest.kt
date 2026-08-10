package org.syriacplatform.packagevalidation.validators

import kotlin.test.Test
import kotlin.test.assertTrue
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.common.types.GroupId
import org.syriacplatform.content.models.Qolo

class IntegrityAllowedDuplicatesTest {

    private val validator = IntegrityValidator()

    @Test
    fun repeatedLiturgicalItemReferenceIsAllowed() {
        val packageData = packageWith(
            prayerSequences = listOf(
                PrayerSequence(
                    id = PrayerSequenceId(301),
                    prayerId = PrayerId(201),
                    liturgicalItemIds = listOf(
                        LiturgicalItemId(501),
                        LiturgicalItemId(501)
                    )
                )
            )
        )

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun repeatedTextReferenceIsAllowed() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(25)
                    )
                ),
                LiturgicalItem(
                    id = LiturgicalItemId(502),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(25)
                    )
                )
            )
        )

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun repeatedQoloReferenceIsAllowed() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Qolo(
                        qoloId = QoloId(438),
                        effectiveMelodyId = MelodyId(75)
                    )
                ),
                LiturgicalItem(
                    id = LiturgicalItemId(502),
                    target = LiturgicalItemTarget.Qolo(
                        qoloId = QoloId(438),
                        effectiveMelodyId = MelodyId(75)
                    )
                )
            )
        )

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun duplicateQoloSortValuesAreAllowed() {
        val packageData = packageWith(
            qolos = listOf(
                Qolo(
                    id = QoloId(438),
                    groupId = GroupId(12),
                    sort = 500,
                    name = "Qolo 438",
                    searchName = "Qolo 438",
                    poeticMeter = null
                ),
                Qolo(
                    id = QoloId(439),
                    groupId = GroupId(12),
                    sort = 500,
                    name = "Qolo 439",
                    searchName = "Qolo 439",
                    poeticMeter = null
                )
            )
        )

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }
}