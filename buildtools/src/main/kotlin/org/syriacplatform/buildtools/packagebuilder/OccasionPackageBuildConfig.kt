package org.syriacplatform.buildtools.packagebuilder

data class OccasionPackageBuildConfig(
    val occasionId: Long,
    val packageId: String,
    val packageName: String,
    val packageVersion: String,
    val contentVersion: String,
    val applicationId: String,
    val applicationName: String,
    val platform: String,
    val defaultLanguage: String,
    val generatedAt: String,
    val buildTool: String,
    val buildVersion: String,
    val buildRevision: String
) {
    companion object {

        fun developmentPreview(
            occasionId: Long
        ): OccasionPackageBuildConfig {
            require(
                occasionId > 0
            ) {
                "Occasion id must be positive."
            }

            return OccasionPackageBuildConfig(
                occasionId = occasionId,
                packageId =
                    "org.syriacplatform.preview.occasion$occasionId",
                packageName =
                    "Occasion $occasionId Development Preview",
                packageVersion = "0.1.0",
                contentVersion =
                    "occasion-$occasionId-preview",
                applicationId =
                    "syriacplatform-reference",
                applicationName =
                    "SyriacPlatform Reference",
                platform = "generic",
                defaultLanguage = "syr",
                generatedAt =
                    "2026-08-18T00:00:00Z",
                buildTool =
                    "SyriacPlatform Build Tools",
                buildVersion = "0.1.0",
                buildRevision =
                    "working-tree"
            )
        }
    }
}