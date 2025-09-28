package org.example.springsecuritywithmongandredis.security.dto

import org.example.springsecuritywithmongandredis.security.enums.VerifyEmailStatus


data class EmailVerifyResponseDto(
    val status: VerifyEmailStatus,
    var jwtToken: String? = null
)