package diffko

import diffko.DeltaType.*
import io.kotlintest.*
import io.kotlintest.extensions.allure.AllureExtension
import io.kotlintest.matchers.collections.shouldHaveAtMostSize
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.properties.shrinking.Shrinker
import io.kotlintest.properties.shrinking.StringShrinker
import io.kotlintest.specs.ShouldSpec
import kotlin.random.Random

class MyersDiffTest : ShouldSpec() {
    override fun listeners() = listOf(AllureExtension)

    init {
        "buildPatch method"{
            should("return emty changeset on equal texts") {
                MyersDiff.buildPatch("abc", "abc") shouldHaveSize 0
            }
            should("return one addition at tail if one letter i added") {
                val buildPatch = MyersDiff.buildPatch("abc", "abcd")
                buildPatch shouldHaveSize 1
                buildPatch[0].startOriginal shouldBe 3
                buildPatch[0].endOriginal shouldBe 3
                buildPatch[0].startRevised shouldBe 3
                buildPatch[0].endRevised shouldBe 4
                buildPatch[0].deltaType shouldBe INSERT
            }
            should("return one deletion at tail if one letter is deleted") {
                val buildPatch = MyersDiff.buildPatch("abc", "ab")
                buildPatch shouldHaveSize 1
                buildPatch[0].startOriginal shouldBe 2
                buildPatch[0].endOriginal shouldBe 3
                buildPatch[0].startRevised shouldBe 2
                buildPatch[0].endRevised shouldBe 2
                buildPatch[0].deltaType shouldBe DELETE
            }
            should("detect deletion at middle as one DELETE operation") {
                val buildPatch = MyersDiff.buildPatch("abcdefghijkl", "abcdijkl")
                buildPatch shouldHaveSize 1
                buildPatch[0].deltaType shouldBe DELETE
            }
            should("return one change on all letter different") {
                val buildPatch = MyersDiff.buildPatch("abc", "defg")
                buildPatch shouldHaveSize 1
                buildPatch shouldHave changesOfType(mapOf(CHANGE to 1))
            }
            should("return one deleteion and one addition on delete from head and add to tail") {
                val buildPatch = MyersDiff.buildPatch("abc", "bcd")
                buildPatch shouldHaveSize 2
                buildPatch shouldHave changesOfType(mapOf(DELETE to 1, INSERT to 1))
            }
            should("work on both strings empty") {
                MyersDiff.buildPatch("", "") shouldHaveSize 0
            }
            should("work on one string empty and one not") {
                val diff = MyersDiff.buildPatch("", "a")
                diff shouldHaveSize 1
                diff[0].deltaType shouldBe INSERT
            }
            should("work on any generated strings") {
                assertAll(Gen.wideString(), Gen.wideString()) { source, revised ->
                    shouldNotThrow<Exception> { MyersDiff.buildPatch(source, revised) }
                }
            }
            // Following test takes looong time to complete, uncomment it only if you're sure
/*
            should("never generate more changes then length of string") {
                assertAll(Gen.string(), Gen.set(Gen.int().filter { it in 0..100 }, 10)) { source: String, changePositions: Set<Int> ->
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
                    MyersDiff.buildPatch(source, sourceBuilder.toString()) shouldHaveAtMostSize 10
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
