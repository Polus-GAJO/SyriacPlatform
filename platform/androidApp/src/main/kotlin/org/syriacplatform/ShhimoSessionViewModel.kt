package org.syriacplatform

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.syriacplatform.audio.android.AndroidAudioPlayerBackend
import org.syriacplatform.audio.contracts.MediaResourceResolver
import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.audio.services.DefaultAudioService
import org.syriacplatform.common.result.Result
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.presentation.shhimo.ShhimoNavigationState

/**
 * Activity recreation must not tear down the audio backend.
 * ViewModel survives rotation, but is cleared when the Activity finishes.
 */
class ShhimoSessionViewModel : ViewModel() {
    val navigation = ShhimoNavigationState()
    var selectedHymnId by mutableStateOf<Long?>(null)
    var selectedMelodyId by mutableStateOf<Long?>(null)
    var notationOpen by mutableStateOf(false)
    var notationPage by mutableStateOf(0)
    val recordingIds = mutableStateMapOf<Long, Long>()

    private var retainedAudio: DefaultAudioService? = null

    fun audio(context: Context): DefaultAudioService {
        retainedAudio?.let { return it }
        val resolver = object : MediaResourceResolver {
            override fun resolve(mediaAsset: MediaAsset): Result<MediaResource> =
                Result.Success(MediaResource(mediaAsset.id, "asset:///shhimo/" + mediaAsset.path))
        }
        return DefaultAudioService(resolver, AndroidAudioPlayerBackend(context.applicationContext))
            .also { it.initialize(); retainedAudio = it }
    }

    override fun onCleared() {
        retainedAudio?.shutdown()
        retainedAudio = null
        super.onCleared()
    }
}
