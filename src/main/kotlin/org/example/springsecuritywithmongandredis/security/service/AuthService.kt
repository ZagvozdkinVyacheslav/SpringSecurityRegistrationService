package org.example.springsecuritywithmongandredis.security.service

import org.example.springsecuritywithmongandredis.exception.UserNotFoundException
import org.example.springsecuritywithmongandredis.security.CLIENT
import org.example.springsecuritywithmongandredis.security.DUMMY_PASSWORD
import org.example.springsecuritywithmongandredis.security.dto.AuthUserDto
import org.example.springsecuritywithmongandredis.security.dto.EmailVerifyResponseDto
import org.example.springsecuritywithmongandredis.security.entity.AuthUserEntity
import org.example.springsecuritywithmongandredis.security.enums.VerifyEmailStatus
import org.example.springsecuritywithmongandredis.security.jwt.JwtAuthenticationFilter.Companion.AUTHORIZATION_HEADER
import org.example.springsecuritywithmongandredis.security.jwt.JwtTokenProvider
import org.example.springsecuritywithmongandredis.security.mapper.toDto
import org.example.springsecuritywithmongandredis.security.mapper.toEntity
import org.example.springsecuritywithmongandredis.security.repository.AuthUserRepository
import org.example.springsecuritywithmongandredis.utils.generateEmailVerifyCode
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull


@Service
class AuthService(
    private val authUserRepository: AuthUserRepository,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val emailSenderService: EmailSenderService
) {

    fun createUser(authUserDto: AuthUserDto): AuthUserDto{
        val userEntity = authUserDto.toEntity(listOf(CLIENT))
        sendVerifyEmail(userEntity)
        return authUserRepository.save(userEntity).toDto()
    }

    fun enterInSystem(authUserDto: AuthUserDto): ResponseEntity<AuthUserDto>{
        val userEntity = authUserRepository.findAuthUserEntityByEmail(authUserDto.email)
            .getOrNull()

        return if (userEntity == null){
            val createdUser = createUser(authUserDto)
            ResponseEntity.status(201).body(createdUser)
        }else {
            sendVerifyEmail(userEntity)
            val dto = authUserRepository.save(userEntity).toDto()
            ResponseEntity.status(200).body(dto)
        }
    }

    fun verifyEmail(email: String, verifyCode: Int): ResponseEntity<EmailVerifyResponseDto>{
        val user = authUserRepository.findAuthUserEntityByEmail(email)
            .orElseThrow { UserNotFoundException() }

        val emailVerifyResponseDto = when{
            user.emailAttempt == 3 -> {
                user.emailVerifyCode = generateEmailVerifyCode()
                user.emailAttempt = 0
                authUserRepository.save(user)

                EmailVerifyResponseDto(status = VerifyEmailStatus.CODE_RE_CREATED)
            }

            user.emailVerifyCode == verifyCode -> {
                user.emailVerifyCode = null
                user.emailAttempt = 0
                authUserRepository.save(user)

                EmailVerifyResponseDto(status = VerifyEmailStatus.VERIFIED)
            }

            user.emailVerifyCode != verifyCode ->{
                user.emailAttempt++
                authUserRepository.save(user)

                EmailVerifyResponseDto(status = VerifyEmailStatus.WRONG_CODE)
            }
            else -> TODO()
        }
        val httpHeaders = HttpHeaders()
        if (emailVerifyResponseDto.status == VerifyEmailStatus.VERIFIED){
            val jwtToken = createJwtToken(user)
            emailVerifyResponseDto.jwtToken = jwtToken
            httpHeaders.add(AUTHORIZATION_HEADER, "Bearer $jwtToken")

        }

        return ResponseEntity(emailVerifyResponseDto, httpHeaders, HttpStatus.OK)




    }

    private fun createJwtToken(user: AuthUserEntity): String {
        val authentication = UsernamePasswordAuthenticationToken(
            user.email,
            DUMMY_PASSWORD,
            user.role.split(",").toSet().map { SimpleGrantedAuthority(it) }
            )
        val auth = authenticationManager.authenticate(authentication)

        SecurityContextHolder.getContext().authentication = auth

        return jwtTokenProvider.generateToken(auth, user.toUserDetails())
    }

    fun getUser(email: String): AuthUserDto {
        return authUserRepository.findAuthUserEntityByEmail(email)
            .orElseThrow { UserNotFoundException() }
            .toDto()
    }

    private fun sendVerifyEmail(userEntity: AuthUserEntity){
        val verifyCode = generateEmailVerifyCode()
        userEntity.emailVerifyCode = verifyCode
        emailSenderService.sendEmailVerify(userEntity, verifyCode)
    }
}