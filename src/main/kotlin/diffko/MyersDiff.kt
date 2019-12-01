package diffko

import diffko.DeltaType.*

/**
 * A clean-room implementation of Eugene Myers greedy differencing algorithm.
 */
object MyersDiff {

    fun computeDiff(source: String, revised: String) = buildPatch(
            findPath(source, revised),
            source.length,
            revised.length
    )

    private fun findPath(orig: String, rev: String): DiffChain? {
        val origChars = orig.toCharArray()
        val revChars = rev.toCharArray()
        val N = orig.length
        val M = rev.length
        val MAX = N + M
        val size = 2 * (MAX + 1)
        val diagonal = IntArray(size)
        val pretendents = arrayOfNulls<DiffChain>(size)
        for (D in 0..MAX) {
            for (k in -D..D step 2) {
                var index: Int
                var x: Int
                if (k == -D || (k != D && diagonal[MAX + k - 1] < diagonal[MAX + k + 1])) {
                    index = MAX + k + 1
                    x = diagonal[index]
                } else {
                    index = MAX + k - 1
                    x = diagonal[index] + 1
                }
                var y = x - k
                var chain = pretendents[index]
                while (x < N && y < M && origChars[x] == revChars[y]) {
                    chain = DiffChain(x, y, chain)
                    x++
                    y++
                }
                if (x >= N && y >= M) {
                    val result = mutableListOf<DiffChain?>()
                    while (chain != null) {
                        result.add(chain)
                        chain = chain.previous
                    }
                    return if (result.isEmpty()) null else result[0]
                }
                diagonal[MAX + k] = x
                pretendents[MAX + k] = chain
            }
        }
        throw IllegalStateException("could not find a diff path")
    }

    /**
     * Constructs a [Patch] from a difference path.
     *
     * @param actualPath The path.
     * @return A [Patch] script corresponding to the path.
     * path.
     */
    private fun buildPatch(actualPath: DiffChain?, sourceLength: Int, revisedLength: Int): List<Change> {
        val result = arrayListOf<Change>()
        val list = (actualPath ?: listOf<DiffChain>())
                .reversed()
                .map { it.copy(previous = null) }
                .plus(DiffChain(sourceLength, revisedLength, null))
        var i = -1
        var j = -1
        for (chain in list) {
            val x = chain.x
            val y = chain.y

            val sourceDelta = x - i
            val revDelta = y - j
            if (sourceDelta != 1 || revDelta != 1) {
                result.add(Change(
                        when {
                            revDelta > 1 && sourceDelta > 1 -> CHANGE
                            revDelta > 1 -> INSERT
                            sourceDelta > 1 -> DELETE
                            else -> throw IllegalStateException("Uknown combination of changes")
                        },
                        startOriginal = i + 1,
                        endOriginal = x,
                        startRevised = j + 1,
                        endRevised = y
                ))
            }

            i = x
            j = y
        }

        return result
    }
}