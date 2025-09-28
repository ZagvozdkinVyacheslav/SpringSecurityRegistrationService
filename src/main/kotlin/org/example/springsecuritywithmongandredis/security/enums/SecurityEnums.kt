package org.example.springsecuritywithmongandredis.security.enums

enum class SecurityEnums {
    CLIENT,
    ADMIN
}

enum class VerifyEmailStatus{
    VERIFIED,
    WRONG_CODE,
    CODE_RE_CREATED
}