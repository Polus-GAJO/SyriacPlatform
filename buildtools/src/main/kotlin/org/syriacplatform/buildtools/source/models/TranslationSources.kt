package org.syriacplatform.buildtools.source.models

data class LanguageSource(val id: Long, val code: String, val name: String?)
data class TranslationSource(val id: Long, val languageId: Long, val content: String, val translator: String?, val source: String?, val reviewStatus: String?)
data class TranslationLinkSource(val id: Long, val translationId: Long, val textId: Long?, val petgomoId: Long?, val isPublished: Boolean)
