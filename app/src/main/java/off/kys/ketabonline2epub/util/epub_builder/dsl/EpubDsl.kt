package off.kys.ketabonline2epub.util.epub_builder.dsl

/**
 * Annotation used to define the scope of the Epub DSL.
 *
 * It prevents "leaking" of builder methods into nested scopes. For example,
 * it ensures you cannot call [off.kys.ketabonline2epub.util.epub_builder.EpubBuilder.chapter] from inside a [off.kys.ketabonline2epub.util.epub_builder.ChapterBuilder]
 * block, maintaining a strict and clean hierarchy.
 */
@DslMarker
annotation class EpubDsl