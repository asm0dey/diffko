package diffko

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.file
import diffko.OutputMode.COLOR
import diffko.OutputMode.TEXT

class Diffko : CliktCommand() {
    private val mode by option(help = "Color mode (tries to highlight changes in red and green) ot Text mode (highlight canges with [[ ]] and << >>)")
            .switch("--text" to TEXT, "--color" to COLOR)
            .default(TEXT)
    private val originalFile by option("-s", "--source", help = "Original file").file(folderOkay = false, exists = true).required()
    private val revisedFile by option("-r", "--revised", help = "Revised file").file(folderOkay = false, exists = true).required()

    @ExperimentalStdlibApi
    override fun run() {
        val origText = originalFile.readText()
        val orig = origText.toList()
        val revText = revisedFile.readText()
        val rev = revText.toList()
        val changes = MyersDiff.buildPatch(origText, revText)
        DiffPrinter.printDiff(
                changes,
                orig,
                rev,
                mode.oldPrefix,
                mode.oldPostfix,
                mode.revPrefix,
                mode.revPostfix
        )
    }
}

enum class OutputMode(
        val oldPrefix: String,
        val oldPostfix: String,
        val revPrefix: String,
        val revPostfix: String
) {
    TEXT(
            oldPrefix = "[[",
            oldPostfix = "]]",
            revPrefix = "<<",
            revPostfix = ">>"
    ),
    COLOR(
            oldPrefix = "\u0027[31m",
            oldPostfix = "\u0027[0m",
            revPrefix = "\u0027[32m",
            revPostfix = "\u0027[0m"
    )
}
