package org.syriacplatform

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.content.models.MediaAsset

val LocalNotationCache = compositionLocalOf<NotationSessionCache?> { null }

/** UI-thread cache scoped to a single content package/session. */
class NotationSessionCache {
    private val images = linkedMapOf<String, ImageBitmap>()
    private val lists = linkedMapOf<MelodyId, List<MediaAsset>>()
    private val maxBytes = 48L * 1024 * 1024
    private fun bytes(image: ImageBitmap) = image.width.toLong() * image.height * 4

    fun image(path: String): ImageBitmap? = images.remove(path)?.also { images[path] = it }
    fun putImage(path: String, image: ImageBitmap) {
        images.remove(path)
        if (bytes(image) > maxBytes) return
        images[path] = image
        while (images.values.sumOf { bytes(it) } > maxBytes) {
            images.remove(images.keys.first())
        }
    }
    fun notations(id: MelodyId): List<MediaAsset>? = lists.remove(id)?.also { lists[id] = it }
    fun putNotations(id: MelodyId, assets: List<MediaAsset>) {
        lists.remove(id)
        lists[id] = assets
        while (lists.size > 64) lists.remove(lists.keys.first())
    }
    fun clear() { images.clear(); lists.clear() }
}
