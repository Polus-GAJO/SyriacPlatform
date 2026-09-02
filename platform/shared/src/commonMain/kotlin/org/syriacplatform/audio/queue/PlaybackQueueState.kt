package org.syriacplatform.audio.queue

enum class PlaybackQueueStatus {
    Idle,
    Loading,
    Playing,
    Paused,
    Completed,
    Error
}

data class PlaybackQueueState(
    val status: PlaybackQueueStatus = PlaybackQueueStatus.Idle,
    val entries: List<PlaybackQueueEntry> = emptyList(),
    val currentIndex: Int? = null
) {
    val currentEntry: PlaybackQueueEntry?
        get() =
            currentIndex
                ?.let(entries::getOrNull)
}
