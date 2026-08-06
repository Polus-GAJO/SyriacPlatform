package org.syriacplatform.content.models

/**
 * طبيعة ارتباط اللحن بالقينة عند وجود توصيف صريح.
 *
 * غياب القيمة يعني أن العلاقة قائمة دون تصنيف
 * Primary أو Substitute.
 */
enum class MelodyQintoRole {
    PRIMARY,
    SUBSTITUTE
}