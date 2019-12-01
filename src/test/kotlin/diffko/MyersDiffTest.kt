package diffko

import diffko.DeltaType.*
import io.kotlintest.Matcher
import io.kotlintest.MatcherResult
import io.kotlintest.extensions.TestListener
import io.kotlintest.extensions.allure.AllureExtension
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.properties.shrinking.Shrinker
import io.kotlintest.properties.shrinking.StringShrinker
import io.kotlintest.shouldBe
import io.kotlintest.shouldHave
import io.kotlintest.specs.ShouldSpec
import kotlin.random.Random

class MyersDiffTest : ShouldSpec(){
    override fun listeners() = listOf(AllureExtension)

    init{
        "computeDiff method"{
            should("return emty changeset on equal texts") {
                MyersDiff.computeDiff("abc", "abc") shouldHaveSize 0
            }
            should("return one addition at tail if one letter i added") {
                val computeDiff = MyersDiff.computeDiff("abc", "abcd")
                computeDiff shouldHaveSize 1
                computeDiff[0].startOriginal shouldBe 3
                computeDiff[0].endOriginal shouldBe 3
                computeDiff[0].startRevised shouldBe 3
                computeDiff[0].endRevised shouldBe 4
                computeDiff[0].deltaType shouldBe INSERT
            }
            should("return one deletion at tail if one letter is deleted") {
                val computeDiff = MyersDiff.computeDiff("abc", "ab")
                computeDiff shouldHaveSize 1
                computeDiff[0].startOriginal shouldBe 2
                computeDiff[0].endOriginal shouldBe 3
                computeDiff[0].startRevised shouldBe 2
                computeDiff[0].endRevised shouldBe 2
                computeDiff[0].deltaType shouldBe DELETE
            }
            should("detect deletion at middle as one DELETE operation") {
                val computeDiff = MyersDiff.computeDiff("abcdefghijkl", "abcdijkl")
                computeDiff shouldHaveSize 1
                computeDiff[0].deltaType shouldBe DELETE
            }
            should("return one change on all letter different") {
                val computeDiff = MyersDiff.computeDiff("abc", "defg")
                computeDiff shouldHaveSize 1
                computeDiff shouldHave changesOfType(mapOf(CHANGE to 1))
            }
            should("return one deleteion and one addition on delete from head and add to tail") {
                val computeDiff = MyersDiff.computeDiff("abc", "bcd")
                computeDiff shouldHaveSize 2
                computeDiff shouldHave changesOfType(mapOf(DELETE to 1, INSERT to 1))
            }
            should("work on both strings empty") {
                MyersDiff.computeDiff("", "") shouldHaveSize 0
            }
            should("work on one string empty and one not") {
                val diff = MyersDiff.computeDiff("", "a")
                diff shouldHaveSize 1
                diff[0].deltaType shouldBe INSERT
            }
            should("work on any generated strings") {
                assertAll(Gen.wideString(), Gen.wideString()) { source, revised ->
                    MyersDiff.computeDiff(source, revised)
                }
            }
            should("work correctly on empty and long") {
                MyersDiff.computeDiff("", ";isdfjlkjfhlkdjfhldkjfhlsd hsldjhsjkfhg eryf o87ry oiu4gt kajrhfb kdjfgykudyfg kfg kejh")
            }
            // Following test takes looong time to complete, uncomment it only if you're sure
/*
        should("never generate more changes then length of string") {
            assertAll(Gen.string(), Gen.list(Gen.int().filter { it in 0..100 })) { source: String, changePositions: List<Int> ->
                val sourceBuilder = StringBuilder(source)
                for (changePosition in changePositions) {
                    if (changePosition < source.length) {
                        sourceBuilder.replace(
                                changePosition,
                                changePosition + 1,
                                Random.nextInt(0, Char.MAX_HIGH_SURROGATE.toInt()).toChar().toString()
                        )
                    }
                }
                MyersDiff.computeDiff(source, sourceBuilder.toString()) shouldHaveAtMostSize source.length
            }
        }
*/
        }
    }
}

fun changesOfType(expected: Map<DeltaType, Int>) = object : Matcher<List<Change>> {
    fun Map<DeltaType, Int>.textual() = map { "${it.value} changes of type ${it.key}" }.joinToString(", ")
    override fun test(value: List<Change>): MatcherResult {
        val actual = value
                .groupBy { it.deltaType }
                .mapValues { it.value.size }
        return MatcherResult(
                actual == expected,
                "changeset should contain ${expected.textual()}, but contains ${actual.textual()}",
                "changeset should contain ${expected.textual()}, but contains ${actual.textual()}"
        )
    }
}

fun Gen.Companion.wideString(maxSize: Int = 100): Gen<String> = object : Gen<String> {
    val literals = listOf("", "\n", "\nabc\n123\n", "\u006c\u0069b/\u0062\u002f\u006d\u0069nd/m\u0061x\u002e\u0070h\u0070")
    override fun constants(): Iterable<String> = literals
    override fun random(): Sequence<String> = generateSequence { widePrintableString(Random.nextInt(maxSize)) }
    override fun shrinker(): Shrinker<String>? = StringShrinker
}

fun widePrintableString(length: Int): String =
        (0 until length).map { Random.nextInt(0, Char.MAX_HIGH_SURROGATE.toInt()).toChar() }.joinToString("")
