package org.syriacplatform

import android.content.Context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import org.syriacplatform.audio.queue.PlaybackQueueController
import org.syriacplatform.audio.queue.PlaybackQueueStatus
import org.syriacplatform.audio.queue.PrayerPlaybackQueueBuilder
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.QintoId
import org.syriacplatform.content.repository.ContentRepository
import org.syriacplatform.content.repository.ApplicationPackageContentRepository
import org.syriacplatform.content.services.DefaultContentService
import org.syriacplatform.packageformat.loading.ApplicationPackageLoader
import org.syriacplatform.packageformat.loading.PackageSource
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibilityDefaults
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
    var weeklyQintoId by mutableStateOf(1L)
        private set

    fun selectWeeklyQinto(id: Long) {
        require(id in 1L..8L)
        if (weeklyQintoId == id) return
        weeklyQintoId = id
        selectedMelodyId = null
        notationOpen = false
        notationPage = 0
        recordingIds.clear()
    }
    var selectedHymnId by mutableStateOf<Long?>(null)
    var prayerItemIds by mutableStateOf<List<Long>>(emptyList())
    var selectedMelodyId by mutableStateOf<Long?>(null)
    var notationOpen by mutableStateOf(false)
    var notationPage by mutableStateOf(0)
    val recordingIds = mutableStateMapOf<Long, Long>()

    private var retainedAudio: DefaultAudioService? = null

    lateinit var queue: PlaybackQueueController
        private set
    private var preparation: Job? = null
    var queuePreparing by mutableStateOf(false)
        private set
    var queueMessage by mutableStateOf<String?>(null)
        private set

    fun stopPrayer() {
        preparation?.cancel()
        preparation = null
        queuePreparing = false
        queueMessage = null
        if (::queue.isInitialized) queue.stop()
    }

    fun playPrayer(source: PackageSource, ids: List<Long>) {
        stopPrayer()
        queuePreparing = true
        val qinto = QintoId(weeklyQintoId)
        preparation = viewModelScope.launch {
            try {
                val repository = ApplicationPackageContentRepository(
                    ApplicationPackageLoader(source), CoreCompatibilityDefaults.CURRENT
                )
                val scoped = object : ContentRepository by repository {
                    override suspend fun loadPerformanceMedia(liturgicalItemId: LiturgicalItemId) =
                        repository.loadPerformanceMediaForQinto(liturgicalItemId, qinto)
                }
                val content = DefaultContentService(scoped).also { it.initialize() }
                val result = PrayerPlaybackQueueBuilder(content).buildItems(ids.map(::LiturgicalItemId))
                coroutineContext.ensureActive()
                when (result) {
                    is Result.Failure -> queueMessage = "prepare"
                    is Result.Success -> {
                        if (result.data.isEmpty()) queueMessage = "empty"
                        else if (queue.start(result.data) is Result.Failure) queueMessage = "start"
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                queueMessage = "prepare"
            } finally {
                queuePreparing = false
            }
        }
    }

    fun audio(context: Context): DefaultAudioService {
        retainedAudio?.let { return it }
        val resolver = object : MediaResourceResolver {
            override fun resolve(mediaAsset: MediaAsset): Result<MediaResource> =
                Result.Success(MediaResource(mediaAsset.id, "asset:///shhimo/" + mediaAsset.path))
        }
        return DefaultAudioService(resolver, AndroidAudioPlayerBackend(context.applicationContext))
            .also { service ->
                service.initialize()
                retainedAudio = service
                queue = PlaybackQueueController(service)
                viewModelScope.launch {
                    service.state.collect { state ->
                        queue.handlePlaybackState(state)
                    }
                }
            }
    }

    override fun onCleared() {
        retainedAudio?.shutdown()
        retainedAudio = null
        super.onCleared()
    }
}
