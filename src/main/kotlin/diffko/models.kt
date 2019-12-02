package diffko

data class DiffChain(val x: Int, val y: Int, val previous: DiffChain?)

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