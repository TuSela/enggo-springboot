package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors backend RandomBlueprintRequest used for generating a PvP quiz.
 */
 data class RandomBlueprintRequest(
    @SerializedName("difficulty")
    val difficulty: Byte,

    @SerializedName("questionTypes")
    val questionTypes: List<String> = listOf("MULTIPLE_CHOICE", "FILL_BLANK", "MATCHING"),

    @SerializedName("themeIds")
    val themeIds: List<Int>,

    @SerializedName("totalQuestions")
    val totalQuestions: Int
)
