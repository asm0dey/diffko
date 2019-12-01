package diffko

import diffko.DeltaType.*

object DiffPrinter {
    fun printDiff(
            changes: List<Change>,
            original: List<Char>,
            revised: List<Char>,
            oldPrefix: String,
            oldPostfix: String,
            newPrefix: String,
            newPostfix: String
    ) {
        println(stringDiff(
                changes,
                original.toCharArray(),
                revised.toCharArray(),
                newPrefix,
                newPostfix,
                oldPrefix,
                oldPostfix
        ))
    }

    fun stringDiff(
            changes: List<Change>,
            original: CharArray,
            revised: CharArray,
            newPrefix: String,
            newPostfix: String,
            oldPrefix: String,
            oldPostfix: String
    ): String {
        var currentSourcePosition = 0
        val b = StringBuilder()
        for (change in changes.sortedBy { it.startOriginal }) {
            b.append(original.sliceArray(currentSourcePosition until change.startOriginal))
            when (change.deltaType) {
                CHANGE -> b.append(
                        oldPrefix,
                        String(original.sliceArray(change.startOriginal until change.endOriginal)),
                        oldPostfix,
                        newPrefix,
                        String(revised.sliceArray(change.startRevised until change.endRevised)),
                        newPostfix)
                DELETE -> b.append(
                        oldPrefix,
                        String(original.sliceArray(change.startOriginal until change.endOriginal)),
                        oldPostfix)
                INSERT -> b.append(
                        newPrefix,
                        String(revised.sliceArray(change.startRevised until change.endRevised)),
                        newPostfix)
            }
            currentSourcePosition = change.endOriginal
        }
        b.append(String(original.sliceArray(currentSourcePosition until original.size)))
        return b.toString()
    }
}