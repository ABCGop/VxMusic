package com.my.kizzy.gateway.entities


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeResponse(
    @SerialName("age_verification_status")
    val ageVerificationStatus: Int?,
    val avatar: String?,
    val bio: String?,
    val discriminator: String?,
    val email: String?,
    val flags: Int?,
    @SerialName("global_name")
    val globalName: String?,
    val id: String?,
    val locale: String?,
    @SerialName("mfa_enabled")
    val mfaEnabled: Boolean?,
    val phone: String?,
    @SerialName("premium_type")
    val premiumType: Int?,
    @SerialName("public_flags")
    val publicFlags: Int?,
    val username: String?,
    val verified: Boolean?
)