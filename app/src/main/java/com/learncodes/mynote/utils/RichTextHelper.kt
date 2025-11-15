package com.learncodes.mynote.utils

import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.EditText
import android.graphics.Typeface
import android.text.style.UnderlineSpan
import android.text.style.StrikethroughSpan

object RichTextHelper {

    fun applyBold(editText: EditText) {
        val start = editText.selectionStart
        val end = editText.selectionEnd

        if (start < end) {
            val spannable = editText.text as Spannable
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    fun applyItalic(editText: EditText) {
        val start = editText.selectionStart
        val end = editText.selectionEnd

        if (start < end) {
            val spannable = editText.text as Spannable
            spannable.setSpan(
                StyleSpan(Typeface.ITALIC),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    fun applyUnderline(editText: EditText) {
        val start = editText.selectionStart
        val end = editText.selectionEnd

        if (start < end) {
            val spannable = editText.text as Spannable
            spannable.setSpan(
                UnderlineSpan(),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    fun applyStrikethrough(editText: EditText) {
        val start = editText.selectionStart
        val end = editText.selectionEnd

        if (start < end) {
            val spannable = editText.text as Spannable
            spannable.setSpan(
                StrikethroughSpan(),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    fun removeFormatting(editText: EditText) {
        val start = editText.selectionStart
        val end = editText.selectionEnd

        if (start < end) {
            val spannable = editText.text as Spannable
            val spans = spannable.getSpans(start, end, Any::class.java)
            spans.forEach { spannable.removeSpan(it) }
        }
    }

    fun toHtml(editable: Editable): String {
        val spannable = SpannableString(editable)
        val html = StringBuilder()
        var lastIndex = 0

        val spans = spannable.getSpans(0, spannable.length, Any::class.java)

        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)

            // Add text before span
            if (spanStart > lastIndex) {
                html.append(spannable.subSequence(lastIndex, spanStart))
            }

            // Add formatted text
            val text = spannable.subSequence(spanStart, spanEnd)
            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        Typeface.BOLD -> html.append("<b>$text</b>")
                        Typeface.ITALIC -> html.append("<i>$text</i>")
                    }
                }
                is UnderlineSpan -> html.append("<u>$text</u>")
                is StrikethroughSpan -> html.append("<strike>$text</strike>")
                else -> html.append(text)
            }

            lastIndex = spanEnd
        }

        // Add remaining text
        if (lastIndex < spannable.length) {
            html.append(spannable.subSequence(lastIndex, spannable.length))
        }

        return html.toString()
    }
}