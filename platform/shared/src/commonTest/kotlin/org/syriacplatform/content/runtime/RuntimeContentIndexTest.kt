package org.syriacplatform.content.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.syriacplatform.common.types.GroupId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo

class RuntimeContentIndexTest {

    @Test
    fun indexProvidesCanonicalLookupById() {
        val qolo =
            Qolo(
                id = QoloId(438),
                groupId = GroupId(12),
                sort = 500,
                name = "Qolo 438",
                searchName = "Qolo 438",
                poeticMeter = null
            )

        val content =
            RuntimeContent(
                entryPoints = emptyList(),
                occasions = emptyList(),
                prayers = emptyList(),
                prayerSequences = emptyList(),
                liturgicalItems = emptyList(),
                texts = emptyList(),
                petgomos = emptyList(),
                qolos = listOf(qolo),
                melodies = emptyList(),
                qintos = emptyList(),
                melodyQintoAssignments = emptyList()
            )

        val index =
            RuntimeContentIndex.from(
                content
            )

        assertEquals(
            qolo,
            index.qolosById[
                QoloId(438)
            ]
        )

        assertNull(
            index.qolosById[
                QoloId(999)
            ]
        )
    }
}