package org.example.springsecuritywithmongandredis.security.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.example.springsecuritywithmongandredis.security.dto.UserDetails
import java.time.LocalDateTime

@Entity
@Table(name = "auth_user")
data class AuthUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "lastname", length = 50, nullable = false)
    var lastname: String,

    @Column(name = "firstname", length = 50, nullable = false)
    var firstname: String,

    @Column(name = "middlename", length = 50)
    var middlename: String? = null,

    @Column(name = "email", length = 50)
    var email: String,

    @Column(name = "phone_number", length = 20, unique = true, nullable = false)
    var phoneNumber: String,

    @Column(name = "email_verify_code", length = 20)
    var emailVerifyCode: Int? = null,

    @Column(name = "email_attempt")
    var emailAttempt: Int = 0,

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "role", length = 300, nullable = false)
    var role: String

){

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun toUserDetails(): UserDetails{
        return UserDetails(
            phoneNumber = this.phoneNumber,
            email = this.email,
            role = role
        )
    }

}