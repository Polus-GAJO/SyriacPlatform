package org.syriacplatform.packagevalidation.compatibility

import org.syriacplatform.common.types.Version

object CoreCompatibilityDefaults {

    val CURRENT =
        CoreCompatibility(
            version = Version(1, 0, 0),
            supportedSchemaVersions = setOf(
                "1.0"
            )
        )
}