package org.syriacplatform.content.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith

class RuntimeContentTest {

    @Test
    fun runtimeContentIsBuiltFromValidatedPackageContent() {
        val packageData =
            packageWith(
                qolos = listOf(
                    Qolo(
                        id = QoloId(438),
                        groupId =
                            org.syriacplatform.common.types.GroupId(12),
                        sort = 500,
                        name = "Qolo 438",
                        searchName = "Qolo 438",
                        poeticMeter = null
                    )
                )
            )

        val content =
            RuntimeContent.from(
                packageData
            )

        assertEquals(
            1,
            content.qolos.size
        )

        assertEquals(
            QoloId(438),
            content.qolos.single().id
        )
    }
}