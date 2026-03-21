package dev.chungjungsoo.gptmobile.presentation.ui.chat

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.viewinterop.AndroidView
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownInlineContent
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import katex.hourglass.`in`.mathlib.MathView

@Composable
fun ChatMarkdown(
    content: String,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val parsed = remember(content) { parseChatMarkdown(content) }
    val highlightsBuilder = remember(isDarkTheme) {
        Highlights.Builder().theme(SyntaxThemes.atom(isDarkTheme))
    }
    val inlineMathByPlaceholder = remember(parsed.inlineMath) {
        parsed.inlineMath.associateBy { it.placeholder }
    }
    val annotator = remember(inlineMathByPlaceholder) {
        markdownAnnotator { source, child ->
            val text = source.substring(child.startOffset, child.endOffset)
            if (!containsInlineMathPlaceholder(text)) {
                false
            } else {
                appendTextWithInlineMath(this, text, inlineMathByPlaceholder)
                true
            }
        }
    }
    val inlineContent = remember(parsed.inlineMath) {
        parsed.inlineMath.associate { token ->
            token.placeholder to InlineTextContent(
                placeholder = Placeholder(
                    width = inlineMathWidth(token.tex),
                    height = 1.4.em,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                InlineMathView(token.tex)
            }
        }
    }
    val components = remember(highlightsBuilder) {
        markdownComponents(
            codeBlock = {
                MarkdownHighlightedCodeBlock(
                    it.content,
                    it.node,
                    it.typography.code,
                    highlightsBuilder,
                    true
                )
            },
            codeFence = {
                MarkdownHighlightedCodeFence(
                    it.content,
                    it.node,
                    it.typography.code,
                    highlightsBuilder,
                    true
                )
            }
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        parsed.blocks.forEach { block ->
            when (block) {
                is ChatMarkdownBlock.DisplayMath -> DisplayMathView(
                    tex = block.tex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )

                is ChatMarkdownBlock.Markdown -> Markdown(
                    block.content,
                    inlineContent = markdownInlineContent(inlineContent),
                    annotator = annotator,
                    components = components,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun appendTextWithInlineMath(
    builder: androidx.compose.ui.text.AnnotatedString.Builder,
    text: String,
    inlineMathByPlaceholder: Map<String, InlineMathToken>
) {
    var cursor = 0
    while (cursor < text.length) {
        val nextToken = inlineMathByPlaceholder.keys
            .mapNotNull { placeholder ->
                val start = text.indexOf(placeholder, cursor)
                if (start == -1) null else placeholder to start
            }
            .minByOrNull { it.second }

        if (nextToken == null) {
            builder.append(text.substring(cursor))
            return
        }

        val (placeholder, start) = nextToken
        if (start > cursor) {
            builder.append(text.substring(cursor, start))
        }
        builder.appendInlineContent(placeholder, "[math]")
        cursor = start + placeholder.length
    }
}

private fun inlineMathWidth(tex: String) = (tex.length.coerceIn(2, 24) * 0.55f).em

@Composable
private fun InlineMathView(tex: String) {
    MathViewContent(
        renderedText = "\\($tex\\)",
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DisplayMathView(
    tex: String,
    modifier: Modifier = Modifier
) {
    MathViewContent(
        renderedText = "\\[$tex\\]",
        modifier = modifier
    )
}

@Composable
private fun MathViewContent(
    renderedText: String,
    modifier: Modifier = Modifier
) {
    val textColor = LocalContentColor.current
    val density = LocalDensity.current
    val textSizePx = with(density) { 16.dp.roundToPx() }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MathView(context).apply {
                setBackgroundColor(Color.Transparent.toArgb())
                setViewBackgroundColor(Color.Transparent.toArgb())
                setClickable(false)
            }
        },
        update = { view ->
            view.setTextColor(textColor.toArgb())
            view.setTextSize(textSizePx)
            view.setDisplayText(renderedText)
        }
    )
}
