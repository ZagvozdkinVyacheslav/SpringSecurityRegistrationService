package org.example.springsecuritywithmongandredis.security.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.example.springsecuritywithmongandredis.security.DUMMY_PASSWORD
import org.example.springsecuritywithmongandredis.security.dto.UserDetails
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey
import kotlin.jvm.java

@Component
class JwtTokenProvider(
    private val objectMapper: ObjectMapper,
    private val passwordEncoder: PasswordEncoder
) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Value($$"${jwt.base64-secret}")
    private lateinit var base64Secret: String

    @Value($$"${jwt.expiration}")
    private val jwtExpirationInMs: Long = 3600000 // 1 час

    private var key: SecretKey? = null



    @PostConstruct
    fun init(){
        val keyBytes = Decoders.BASE64
            .decode(base64Secret)
            .also { log.debug("Using Base64-encoded JWT secret key") }
        key = Keys.hmacShaKeyFor(keyBytes)

    }

    // Генерация токена
    fun generateToken(authentication: Authentication, userDetails: UserDetails): String {
        val authorities = authentication.authorities.joinToString(",") { it.authority }
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)


        return Jwts.builder().apply {
            subject(authentication.name)
            claim(AUTHORITIES_KEY, authorities)
            claim(USER_DETAIL_KEY, objectMapper.writeValueAsString(userDetails))
            issuedAt(now)
            expiration(expiryDate)
            signWith(key)
        }.compact()
    }

    // Получение username из токена
    fun getAuthentication(token: String): Authentication {
        val claims =  Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
        val authorities = claims[AUTHORITIES_KEY]
            .toString()
            .split(",")
            .dropLastWhile { it.isEmpty() }
            .map { SimpleGrantedAuthority(it) }

        val principal = User(claims.subject, passwordEncoder.encode(DUMMY_PASSWORD), authorities)
        val token = UsernamePasswordAuthenticationToken(principal, token, authorities)
        token.details = if(claims[USER_DETAIL_KEY] != null){
            objectMapper.readValue(claims[USER_DETAIL_KEY].toString(), UserDetails::class.java)
        }else{
            null
        }
        return token

    }

    // Валидация токена
    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            return true
        }  catch (e: io.jsonwebtoken.security.SecurityException) {
            log.info("Invalid JWT signature.")
            log.trace("Invalid JWT signature trace: $e")
        } catch (e: MalformedJwtException) {
            log.info("Invalid JWT signature.")
            log.trace("Invalid JWT signature trace: $e")
        } catch (e: ExpiredJwtException) {
            log.info("Expired JWT token.")
            log.trace("Expired JWT token trace: $e")
        } catch (e: UnsupportedJwtException) {
            log.info("Unsupported JWT token.")
            log.trace("Unsupported JWT token trace: $e")
        } catch (e: IllegalArgumentException) {
            log.info("JWT token compact of handler are invalid.")
            log.trace("JWT token compact of handler are invalid trace: $e")
        }
        return false
    }

    companion object{
        private val AUTHORITIES_KEY = "auth"
        private val USER_DETAIL_KEY = "user_details"
    }
}