package diffko

import io.kotlintest.extensions.TestListener
import io.kotlintest.extensions.allure.AllureExtension
import io.kotlintest.specs.ShouldSpec
import io.kotlintest.shouldBe

class DiffPrinterTest : ShouldSpec(){
    override fun listeners() = listOf(AllureExtension)

    init{
        "stringDiff"{
            should("render correct string on diff") {
                val source = "abc"
                val target = "bcd"
                val diff = MyersDiff.buildPatch(source, target)
                DiffPrinter.stringDiff(
                        diff,
                        source.toCharArray(),
                        target.toCharArray(),
                        "<",
                        ">",
                        "[",
                        "]"
                ) shouldBe "[a]bc<d>"
            }
            should("return expected result from input from basic task") {
                val source = """data class FooBoo(val x: Int, y: Int)
                           |
                           |val abc = 1
                           |val xyz = 2
                           |
                           |val fooBoo = FooBoo(abc, xyz)""".trimMargin()
                val revised = """data class FooBoo(val x: Int, y: Int)
                           |
                           |val def = 1
                           |val pqr = 2
                           |
                           |val fooBoo = FooBoo(def, pqr)""".trimMargin()
                val diff = MyersDiff.buildPatch(source, revised)
                DiffPrinter.stringDiff(
                        changes = diff,
                        original = source.toCharArray(),
                        revised = revised.toCharArray(),
                        newPrefix = "<<",
                        newPostfix = ">>",
                        oldPrefix = "[[",
                        oldPostfix = "]]"
                ) shouldBe """data class FooBoo(val x: Int, y: Int)
                        |
                        |val [[abc]]<<def>> = 1
                        |val [[xyz]]<<pqr>> = 2
                        |
                        |val fooBoo = FooBoo([[abc]]<<def>>, [[xyz]]<<pqr>>)""".trimMargin()

            }
        }
    }
}