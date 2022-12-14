package com.ke.hs_tracker.module.entity

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.ke.hs_tracker.module.R
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

enum class Rarity(@StringRes val titleRes: Int, @ColorRes val colorRes: Int) {

    /**
     * 免费
     */
    Free(R.string.module_rarity_free, R.color.module_rarity_common),

    /**
     * 普通
     */
    Common(R.string.module_rarity_common, R.color.module_rarity_common),

    /**
     * 稀有
     */
    Rare(R.string.module_rarity_rare, R.color.module_rarity_rare),

    /**
     * 史诗
     */
    Epic(R.string.module_rarity_epic, R.color.module_rarity_epic),

    /**
     * 传说
     */
    Legendary(R.string.module_rarity_legendary, R.color.module_rarity_legendary)
}

class RarityAdapter {


    @FromJson
    fun fromJson(value: String): Rarity {
        return EnumMoshiAdapter.fromJson(
            value, Rarity.values()
        )
    }

    @ToJson
    fun toJson(value: Rarity) = EnumMoshiAdapter.toJson(value)
}