package org.example.springsecuritywithmongandredis.security.service

import jakarta.transaction.Transactional
import org.example.springsecuritywithmongandredis.exception.UserNotFoundException
import org.example.springsecuritywithmongandredis.security.DUMMY_PASSWORD
import org.example.springsecuritywithmongandredis.security.entity.AuthUserEntity
import org.example.springsecuritywithmongandredis.security.repository.AuthUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component


@Component("userDetailsService")
class DomainUserDetailsService : UserDetailsService {
    @Autowired
    private lateinit var userRepository: AuthUserRepository

    @Autowired
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    @Transactional
    override fun loadUserByUsername(email: String): UserDetails {
        //todo matcher на номер телефона
        return userRepository.findAuthUserEntityByEmail(email)
                .map { createSpringSecurityUser(it) }
                .orElseThrow { UserNotFoundException() }
    }

    private fun createSpringSecurityUser(user: AuthUserEntity): User{
        val grantedAuthorities = user.role.split(",").toSet()
            .map { authority -> SimpleGrantedAuthority(authority) }
            .toList()

        return User(
            user.email,
            passwordEncoder.encode(DUMMY_PASSWORD),
            grantedAuthorities
        )
    }



}