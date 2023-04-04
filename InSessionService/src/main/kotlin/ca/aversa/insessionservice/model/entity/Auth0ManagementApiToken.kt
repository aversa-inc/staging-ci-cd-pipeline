package ca.aversa.insessionservice.model.entity

import com.google.gson.annotations.SerializedName

data class Auth0ManagementApiToken(

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("scope")
    val scope: String,

    @SerializedName("expires_in")
    val expiresIn: Int,

    @SerializedName("token_type")
    val tokenType: String
)
