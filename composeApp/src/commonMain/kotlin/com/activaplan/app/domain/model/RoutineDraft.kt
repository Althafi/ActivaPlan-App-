package com.activaplan.app.domain.model

/** Item di Builder; [key] stabil selama sesi edit karena latihan yang sama boleh muncul dua kali. */
data class DraftItem(val key: Int, val item: RoutineItem) {
    /** Latihan durasi (detik) bila `reps` kosong. */
    val isTimed: Boolean get() = item.reps == null

    val workValue: Int get() = item.reps ?: item.durationSeconds ?: 0

    val canDecreaseSets: Boolean get() = item.sets > RoutineRules.SETS.first
    val canIncreaseSets: Boolean get() = item.sets < RoutineRules.SETS.last
    val canDecreaseWork: Boolean get() = workValue > workRange.first
    val canIncreaseWork: Boolean get() = workValue < workRange.last

    private val workRange: IntRange get() = if (isTimed) RoutineRules.SECONDS else RoutineRules.REPS
}

/** Rutinitas yang sedang diedit di Builder; setiap operasi mengembalikan salinan baru. */
data class RoutineDraft(
    val id: String,
    val name: String = "",
    val items: List<DraftItem> = emptyList(),
    private val nextKey: Int = items.size,
) {
    val isFull: Boolean get() = items.size >= RoutineRules.MAX_ITEMS

    val isNameValid: Boolean get() = RoutineRules.isValidName(name)

    fun rename(newName: String) = copy(name = newName)

    /** Menambah di akhir; null bila sudah [RoutineRules.MAX_ITEMS]. */
    fun add(item: RoutineItem): RoutineDraft? =
        if (isFull) null else copy(items = items + DraftItem(nextKey, item), nextKey = nextKey + 1)

    fun remove(key: Int) = copy(items = items.filterNot { it.key == key })

    /** Memindah item di indeks [from] ke indeks [to]; di luar jangkauan atau sama tidak mengubah apa pun. */
    fun move(from: Int, to: Int): RoutineDraft {
        if (from !in items.indices || to !in items.indices || from == to) return this
        val list = items.toMutableList()
        list.add(to, list.removeAt(from))
        return copy(items = list)
    }

    fun adjustSets(key: Int, delta: Int) = update(key) {
        it.copy(sets = (it.sets + delta).coerceIn(RoutineRules.SETS))
    }

    /** Rep ±1 untuk latihan repetisi, detik ±[RoutineRules.SECONDS_STEP] untuk latihan durasi. */
    fun adjustWork(key: Int, delta: Int) = update(key) {
        if (it.reps != null) {
            it.copy(reps = (it.reps + delta).coerceIn(RoutineRules.REPS))
        } else {
            val seconds = (it.durationSeconds ?: RoutineRules.SECONDS.first) + delta * RoutineRules.SECONDS_STEP
            it.copy(durationSeconds = seconds.coerceIn(RoutineRules.SECONDS))
        }
    }

    fun toRoutine() = Routine(id, name.trim(), items.map { it.item })

    private fun update(key: Int, transform: (RoutineItem) -> RoutineItem) =
        copy(items = items.map { if (it.key == key) it.copy(item = transform(it.item)) else it })

    companion object {
        fun from(routine: Routine) = RoutineDraft(
            id = routine.id,
            name = routine.name,
            items = routine.items.mapIndexed { index, item -> DraftItem(index, item) },
        )
    }
}
