package diffko

data class DiffChain(val x: Int, val y: Int, val previous: DiffChain?) : Iterable<DiffChain> {
    override fun iterator(): Iterator<DiffChain> {
        val self = this;
        return object : Iterator<DiffChain> {
            var chain: DiffChain? = self
            override fun hasNext() = chain != null

            override fun next(): DiffChain {
                val result = chain
                chain = chain?.previous
                return result ?: throw NoSuchElementException()
            }

        }
    }
}

enum class DeltaType {
    CHANGE,
    DELETE,
    INSERT,
}

data class Change(
        val deltaType: DeltaType,
        val startOriginal: Int,
        val endOriginal: Int,
        val startRevised: Int,
        val endRevised: Int
)