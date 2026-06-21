package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

/**
 * Request payload for sending a PvP invitation to a friend.
 * Mirrors the backend InviteRequest DTO.
 */
 data class InviteRequest(
    @SerializedName("inviteeUsername")
    val inviteeUsername: String,

    @SerializedName("randomBlueprintRequest")
    val randomBlueprintRequest: RandomBlueprintRequest
)
