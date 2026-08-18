package org.syriacplatform.buildtools.preview

import org.syriacplatform.buildtools.schema.SchemaV1CompositionDraft

/**
 * Development-package boundary for the representative source.
 *
 * Melody incompleteness no longer removes a legal Qolo occurrence.
 * A composition may contain:
 *
 * - a resolved effective Melody;
 * - no resolved Melody;
 * - multiple Melody candidates.
 *
 * Only genuinely package-blocking diagnostics prevent the preview
 * from being emitted.
 */
class DevelopmentPreviewSlice {

    fun create(
        source: SchemaV1CompositionDraft
    ): SchemaV1CompositionDraft {
        require(
            !source.hasBlockingDiagnostics
        ) {
            "Development preview cannot be created from " +
                    "a package-blocking composition."
        }

        require(
            source.resolvedItemCount > 0
        ) {
            "Development preview contains no liturgical items."
        }

        return source
    }
}