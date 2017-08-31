package com.scurab.android.playerscore

import android.text.Spanned
import android.text.InputFilter

class ScoreInputFilter : InputFilter {

    private val EMPTY = ""
    private val result = StringBuilder(16)

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence {
        return filter(source, start, end, dest.toString(), dstart, dend)
    }

    //just for easier tests for roboelectric (spanned is not shadowed)
    fun filter(source: CharSequence, start: Int, end: Int, dest: CharSequence, dstart: Int, dend: Int): CharSequence {
        val resultToWrite = buildResult(source, start, end, dest, dstart, dend)

        if (resultToWrite.length == 0) {//for empty string, everything is deleted
            return EMPTY //just return empty string to allow full deletion
        } else {
            return if (isValid(resultToWrite)) //match it
                source //fine let's allow write/delete stuff
            else
                if (start == end)//deleting
                    dest.subSequence(dstart, dend)//return old value if new result is invalid to block deleting
                else
                    EMPTY //return empty string to allow deleting
        }
    }

    private fun isValid(input: String): Boolean {
        try {
            val int = Integer.parseInt(input)
            return when (int) {
                in 1000..20000 -> int == (int / 100) * 100
                in 10001..Integer.MAX_VALUE -> false
                else -> true
            }
        } catch (t: Throwable) {
            return false
        }
    }

    private fun buildResult(source: CharSequence, start: Int, end: Int, dest: CharSequence, dstart: Int, dend: Int): String {
        result.setLength(0)//reset temp string result
        result.append(dest)

        if (start == end) {//deleting
            result.delete(dstart, dend)
        } else {
            result.insert(dstart, source)
        }
        return result.toString()
    }
}