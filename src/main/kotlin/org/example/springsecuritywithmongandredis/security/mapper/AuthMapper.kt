package org.example.springsecuritywithmongandredis.security.mapper

import org.example.springsecuritywithmongandredis.security.dto.AuthUserDto
import org.example.springsecuritywithmongandredis.security.entity.AuthUserEntity
import kotlin.random.Random


fun AuthUserDto.toEntity(roles: List<String>): AuthUserEntity{
    return AuthUserEntity(
        lastname = this.lastname,
        firstname = this.firstname,
        middlename = this.middlename,
        email = this.email,
        phoneNumber = this.phoneNumber,
        emailVerifyCode = Random.nextInt(999999),
        role = roles.joinToString(",")
    )
}

fun AuthUserEntity.toDto(): AuthUserDto{
    return AuthUserDto(
        lastname = this.lastname,
        firstname = this.firstname,
        middlename = this.middlename,
        email = this.email,
        phoneNumber = phoneNumber
    )
}