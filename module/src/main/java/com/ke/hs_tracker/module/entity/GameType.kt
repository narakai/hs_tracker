package com.ke.hs_tracker.module.entity

import androidx.annotation.StringRes
import com.ke.hs_tracker.module.R

enum class GameType(@StringRes val title: Int) {
    /**
     * ζε
     */
    Ranked(R.string.module_ranked),

    /**
     * δΌι²
     */
    Casual(R.string.module_casual),

    Unknown(R.string.module_unknown)
}

val String.toGameType: GameType
    get() = GameType.values().find {
        this.contains(it.name, true)
    } ?: GameType.Unknown