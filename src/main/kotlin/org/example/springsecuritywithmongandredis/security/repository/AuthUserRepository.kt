package org.example.springsecuritywithmongandredis.security.repository

import org.example.springsecuritywithmongandredis.security.entity.AuthUserEntity
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AuthUserRepository: JpaRepository<AuthUserEntity, Long> {

    fun findAuthUserEntityByEmail(email: String): Optional<AuthUserEntity>
}