package org.syriacplatform.content.models

import org.syriacplatform.common.types.LiturgicalItemId

/**
 * ظهور قانوني واحد داخل التسلسل الليتورجي.
 *
 * يملك العنصر هوية مستقلة عن الكيان الذي يشير إليه،
 * لأن النص أو القولو نفسه قد يظهر في مواضع وسياقات متعددة.
 */
data class LiturgicalItem(
    val id: LiturgicalItemId,
    val target: LiturgicalItemTarget
)