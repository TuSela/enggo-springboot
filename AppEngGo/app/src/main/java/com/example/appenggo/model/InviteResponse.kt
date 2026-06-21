package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors backend InviteResponse DTO.
 */
 data class InviteResponse(
    @SerializedName("inviteId")
    val inviteId: Int,

    @SerializedName("inviterPlayerId")
    val inviterPlayerId: Int?,

    @SerializedName("inviteePlayerId")
    val inviteePlayerId: Int?,

    @SerializedName("inviterUsername")
    val inviterUsername: String?,

    @SerializedName("inviteeUsername")
    val inviteeUsername: String?,

    @SerializedName("randomBlueprintRequest")
    val randomBlueprintRequest: RandomBlueprintRequest?,

    @SerializedName("status")
    val status: String?
)
