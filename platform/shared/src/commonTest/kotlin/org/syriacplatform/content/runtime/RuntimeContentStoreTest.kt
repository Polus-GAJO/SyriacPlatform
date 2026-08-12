package org.syriacplatform.content.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import org.syriacplatform.common.types.GroupId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith

class RuntimeContentStoreTest {

    @Test
    fun storeBuildsContentAndIndexFromPackage() {
        val qolo =
            Qolo(
                id = QoloId(438),
                groupId = GroupId(12),
                sort = 500,
                name = "Qolo 438",
                searchName = "Qolo 438",
                poeticMeter = null
            )

        val packageData =
            packageWith(
                qolos = listOf(
                    qolo
                )
            )

        val store =
            RuntimeContentStore.from(
                packageData
            )

        assertEquals(
            listOf(qolo),
            store.content.qolos
        )

        assertEquals(
            qolo,
            store.index.qolosById[
                QoloId(438)
            ]
        )
    }
}