package org.syriacplatform.buildtools.validation

import org.syriacplatform.buildtools.source.AuthorSourceData

class SourceValidator {

    fun validate(
        data: AuthorSourceData
    ): SourceValidationReport {
        val diagnostics = mutableListOf<SourceDiagnostic>()

        validateOccasionLinks(
            data,
            diagnostics
        )

        validateExistsInReferences(
            data,
            diagnostics
        )

        validateTextOccurrences(
            data,
            diagnostics
        )

        validatePetgomoAssignments(
            data,
            diagnostics
        )

        validateAuthoringStates(
            data,
            diagnostics
        )

        validateMelodyResolution(
            data,
            diagnostics
        )

        return SourceValidationReport(
            diagnostics = diagnostics
        )
    }

    private fun validateOccasionLinks(
        data: AuthorSourceData,
        diagnostics: MutableList<SourceDiagnostic>
    ) {
        val existsInIds = data.existsIn
            .map { it.id }
            .toSet()

        data.occasionLinks.forEach { link ->
            if (link.occasionId != data.occasion.id) {
                diagnostics += error(
                    code = "BROKEN_OCCASION_REFERENCE",
                    message =
                        "OccaExis ${link.id} references Occasion " +
                                "${link.occasionId}, but the exported " +
                                "Occasion is ${data.occasion.id}.",
                    sourceType = "OccaExis",
                    sourceId = link.id
                )
            }

            val existsInId = link.existsInId

            if (
                existsInId == null ||
                existsInId !in existsInIds
            ) {
                diagnostics += error(
                    code = "BROKEN_EXISTS_IN_REFERENCE",
                    message =
                        "OccaExis ${link.id} references missing " +
                                "ExistsIn $existsInId.",
                    sourceType = "OccaExis",
                    sourceId = link.id
                )
            }
        }
    }

    private fun validateExistsInReferences(
        data: AuthorSourceData,
        diagnostics: MutableList<SourceDiagnostic>
    ) {
        val prayerIds = data.prayers
            .map { it.id }
            .toSet()

        val qoloIds = data.qolos
            .map { it.id }
            .toSet()

        data.existsIn.forEach { item ->
            val prayerId = item.prayerId

            if (
                prayerId != null &&
                prayerId !in prayerIds
            ) {
                diagnostics += error(
                    code = "BROKEN_PRAYER_REFERENCE",
                    message =
                        "ExistsIn ${item.id} references missing " +
                                "Prayer $prayerId.",
                    sourceType = "ExistsIn",
                    sourceId = item.id
                )
            }

            val qoloId = item.qoloId

            if (
                qoloId != null &&
                qoloId !in qoloIds
            ) {
                diagnostics += error(
                    code = "BROKEN_QOLO_REFERENCE",
                    message =
                        "ExistsIn ${item.id} references missing " +
                                "Qolo $qoloId.",
                    sourceType = "ExistsIn",
                    sourceId = item.id
                )
            }
        }
    }

    private fun validateTextOccurrences(
        data: AuthorSourceData,
        diagnostics: MutableList<SourceDiagnostic>
    ) {
        val existsInIds = data.existsIn
            .map { it.id }
            .toSet()

        val textIds = data.texts
            .map { it.id }
            .toSet()

        val occurrencesByExistsIn =
            data.existsInTexts.groupBy {
                it.existsInId
            }

        data.existsInTexts.forEach { occurrence ->
            val existsInId = occurrence.existsInId

            if (
                existsInId == null ||
                existsInId !in existsInIds
            ) {
                diagnostics += error(
                    code = "BROKEN_TEXT_OCCURRENCE_PARENT",
                    message =
                        "ExistsInText ${occurrence.id} references " +
                                "missing ExistsIn $existsInId.",
                    sourceType = "ExistsInText",
                    sourceId = occurrence.id
                )
            }

            val textId = occurrence.textId

            if (
                textId == null ||
                textId !in textIds
            ) {
                diagnostics += error(
                    code = "BROKEN_TEXT_REFERENCE",
                    message =
                        "ExistsInText ${occurrence.id} references " +
                                "missing Text $textId.",
                    sourceType = "ExistsInText",
                    sourceId = occurrence.id
                )
            }
        }

        data.existsIn.forEach { item ->
            if (
                occurrencesByExistsIn[item.id]
                    .orEmpty()
                    .isEmpty()
            ) {
                diagnostics += info(
                    code = "NO_TEXT_OCCURRENCES",
                    message =
                        "ExistsIn ${item.id} currently has no " +
                                "text occurrences.",
                    sourceType = "ExistsIn",
                    sourceId = item.id
                )
            }
        }
    }

    private fun validatePetgomoAssignments(
        data: AuthorSourceData,
        diagnostics: MutableList<SourceDiagnostic>
    ) {
        val occurrences = data.existsInTexts
            .associateBy { it.id }

        val petgomoIds = data.petgomos
            .map { it.id }
            .toSet()

        data.petExis.forEach { assignment ->
            val occurrenceId =
                assignment.existsInTextId

            val occurrence =
                occurrenceId?.let {
                    occurrences[it]
                }

            if (occurrence == null) {
                diagnostics += error(
                    code = "BROKEN_PETGOMO_OCCURRENCE_REFERENCE",
                    message =
                        "PetExis ${assignment.id} references " +
                                "missing ExistsInText $occurrenceId.",
                    sourceType = "PetExis",
                    sourceId = assignment.id
                )
            }

            val petgomoId = assignment.petgomoId

            if (
                petgomoId == null ||
                petgomoId !in petgomoIds
            ) {
                diagnostics += error(
                    code = "BROKEN_PETGOMO_REFERENCE",
                    message =
                        "PetExis ${assignment.id} references " +
                                "missing Petgomo $petgomoId.",
                    sourceType = "PetExis",
                    sourceId = assignment.id
                )
            }

            if (
                occurrence != null &&
                assignment.textId != null &&
                assignment.textId != occurrence.textId
            ) {
                diagnostics += error(
                    code = "PETGOMO_TEXT_MISMATCH",
                    message =
                        "PetExis ${assignment.id} has TextID " +
                                "${assignment.textId}, but " +
                                "ExistsInText $occurrenceId uses " +
                                "TextID ${occurrence.textId}.",
                    sourceType = "PetExis",
                    sourceId = assignment.id
                )
            }
        }
    }

    private fun validateAuthoringStates(
        data: AuthorSourceData,
        diagnostics: MutableList<SourceDiagnostic>
    ) {
        data.existsIn
            .filter { it.qintoId == 0L }
            .forEach { item ->
                diagnostics += warning(
                    code = "UNRESOLVED_QINTO",
                    message =
                        "ExistsIn ${item.id} has QintoN = 0; " +
                                "the qinto has not yet been " +
                                "determined in the Author Database.",
                    sourceType = "ExistsIn",
                    sourceId = item.id
                )
            }
    }

    private fun validateMelodyResolution(
        data: AuthorSourceData,
        diagnostics: MutableList<SourceDiagnostic>
    ) {
        val melodiesByQoloAndQinto =
            data.melodies.groupBy {
                it.qoloId to it.qintoId
            }

        data.existsIn.forEach { item ->
            val qoloId = item.qoloId
                ?: return@forEach

            val qintoId = item.qintoId
                ?: return@forEach

            if (qintoId <= 0L) {
                return@forEach
            }

            val candidates =
                melodiesByQoloAndQinto[
                    qoloId to qintoId
                ].orEmpty()

            when {
                candidates.isEmpty() -> {
                    diagnostics += warning(
                        code = "NO_MELODY_CANDIDATE",
                        message =
                            "ExistsIn ${item.id} uses Qolo " +
                                    "$qoloId and Qinto $qintoId, " +
                                    "but no matching Melody was found.",
                        sourceType = "ExistsIn",
                        sourceId = item.id
                    )
                }

                candidates.size > 1 -> {
                    val melodyIds = candidates
                        .map { it.id }
                        .sorted()
                        .joinToString(", ")

                    diagnostics += warning(
                        code = "AMBIGUOUS_MELODY",
                        message =
                            "ExistsIn ${item.id} uses Qolo " +
                                    "$qoloId and Qinto $qintoId, " +
                                    "which match multiple Melodies: " +
                                    melodyIds +
                                    ". Explicit resolution is required.",
                        sourceType = "ExistsIn",
                        sourceId = item.id
                    )
                }
            }
        }
    }

    private fun error(
        code: String,
        message: String,
        sourceType: String,
        sourceId: Long
    ): SourceDiagnostic {
        return SourceDiagnostic(
            severity = DiagnosticSeverity.ERROR,
            code = code,
            message = message,
            sourceType = sourceType,
            sourceId = sourceId
        )
    }

    private fun warning(
        code: String,
        message: String,
        sourceType: String,
        sourceId: Long
    ): SourceDiagnostic {
        return SourceDiagnostic(
            severity = DiagnosticSeverity.WARNING,
            code = code,
            message = message,
            sourceType = sourceType,
            sourceId = sourceId
        )
    }

    private fun info(
        code: String,
        message: String,
        sourceType: String,
        sourceId: Long
    ): SourceDiagnostic {
        return SourceDiagnostic(
            severity = DiagnosticSeverity.INFO,
            code = code,
            message = message,
            sourceType = sourceType,
            sourceId = sourceId
        )
    }
}