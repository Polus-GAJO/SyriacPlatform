package org.syriacplatform.buildtools.schema

object ProjectionIdFactory {

    fun prayerSequenceId(
        occasionId: Long,
        prayerId: Long
    ): Long {
        requireSourceId(
            name = "Occasion",
            value = occasionId
        )

        requireSourceId(
            name = "Prayer",
            value = prayerId
        )

        return (occasionId shl 32) or prayerId
    }

    fun entryPointIdForOccasion(
        occasionId: Long
    ): Long {
        requireSourceId(
            name = "Occasion",
            value = occasionId
        )

        /*
         * EntryPoint has its own typed-ID namespace.
         * The current single-occasion package projection
         * therefore uses the stable Occasion identifier
         * as its deterministic input.
         */
        return occasionId
    }

    private fun requireSourceId(
        name: String,
        value: Long
    ) {
        require(
            value in 1L..Int.MAX_VALUE.toLong()
        ) {
            "$name ID $value cannot be used for " +
                    "Schema v1 projection identity. " +
                    "Expected a positive 32-bit Author " +
                    "Database identifier."
        }
    }
}