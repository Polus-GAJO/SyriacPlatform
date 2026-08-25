package org.syriacplatform.packageformat.parsed

/**
 * ظٹط³ط¬ظ„ ظˆط¬ظˆط¯ ظ…ط¬ظ…ظˆط¹ط§طھ ط§ظ„ظ…ط­طھظˆظ‰ ظپط¹ظ„ظٹظ‹ط§ ظپظٹ ط§ظ„ط­ط²ظ…ط© ط§ظ„ط£طµظ„ظٹط©.
 *
 * ظ‡ط°ظ‡ ط§ظ„ظ…ط¹ظ„ظˆظ…ط§طھ ظ…ط³طھظ‚ظ„ط© ط¹ظ† ط¹ط¯ط¯ ط§ظ„ط¹ظ†ط§طµط± ط¯ط§ط®ظ„ ط§ظ„ظ…ط¬ظ…ظˆط¹ط©:
 *
 * present = true + empty list
 * طھط¹ظ†ظٹ ط£ظ† ط§ظ„ظ…ط¬ظ…ظˆط¹ط© ظ…ظˆط¬ظˆط¯ط© ظپظٹ ط§ظ„ط­ط²ظ…ط© ظˆظ„ظƒظ†ظ‡ط§ ظپط§ط±ط؛ط©.
 *
 * present = false + empty list
 * طھط¹ظ†ظٹ ط£ظ† ط§ظ„ظ…ط¬ظ…ظˆط¹ط© ط؛ظٹط± ظ…ظˆط¬ظˆط¯ط© ط£طµظ„ظ‹ط§ ظپظٹ ط§ظ„ط­ط²ظ…ط©.
 *
 * طھط³طھط®ط¯ظ… ظ‡ط°ظ‡ ط§ظ„ظ…ط¹ظ„ظˆظ…ط§طھ ط®طµظˆطµظ‹ط§ ظپظٹ Profile ValidationطŒ
 * ط­ظٹط« ظٹظ…ظƒظ† ط£ظ† ظٹظپط±ط¶ Profile ظˆط¬ظˆط¯ ظ…ط¬ظ…ظˆط¹ط© ظ…ط¹ظٹظ†ط© ط­طھظ‰ ظ„ظˆ ظƒط§ظ†طھ ظپط§ط±ط؛ط©.
 */
data class PackageCollectionPresence(
    val entryPoints: Boolean,
    val occasions: Boolean,
    val prayers: Boolean,
    val prayerSequences: Boolean,
    val liturgicalItems: Boolean,
    val texts: Boolean,
    val qolos: Boolean,
    val melodies: Boolean,
    val qintos: Boolean,
    val petgomos: Boolean,
    val melodyQintoAssignments: Boolean,
    val mediaAssets: Boolean = false
)