package org.example.springsecuritywithmongandredis.config.security

import org.example.springsecuritywithmongandredis.security.CONST_CLIENT
import org.example.springsecuritywithmongandredis.security.jwt.JwtAuthenticationFilter
import org.example.springsecuritywithmongandredis.security.jwt.JwtTokenProvider
import org.example.springsecuritywithmongandredis.security.service.DomainUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val tokenProvider: JwtTokenProvider,
    private val userDetailsService: DomainUserDetailsService
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors{corsConfig -> corsConfig.configurationSource(corsConfigurationSource())}
            .csrf { csrf ->
                csrf.disable()//csrf с jwt токеном не требуется
            }
            .headers { headers ->
                headers
                    .xssProtection { xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK) }
                    .contentSecurityPolicy { csp -> csp.policyDirectives("default-src 'self'") }
                    .frameOptions { frame -> frame.deny() }
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { authz ->
                authz
                    //swagger
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()

                    //actuator for csrf
                    .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                    .requestMatchers("/actuator/info").permitAll()
                    .requestMatchers("/actuator/health").permitAll()


                    .requestMatchers("/auth/login").permitAll()
                    .requestMatchers("/auth/email_verify").permitAll()

                    .requestMatchers("/docs/doc").hasRole(CONST_CLIENT)//hasRole add ROLE_ prefix

                    .anyRequest().authenticated()
            }
            .addFilterBefore(JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter::class.java)
        //JWT

        return http.build()
    }



    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            //тут указываем локальный адрес фронта и адрес фронта
            allowedOrigins = mutableListOf("http://localhost:8080")
            allowedMethods = mutableListOf("GET", "POST", "PUT", "DELETE", "OPTION")
            allowedHeaders = mutableListOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply{
            registerCorsConfiguration("/**", configuration)
        }
    }

    @Bean
    fun authenticationManager(httpSecurity: HttpSecurity, passwordEncoder: BCryptPasswordEncoder): AuthenticationManager{
        return httpSecurity.getSharedObject(AuthenticationManagerBuilder::class.java)
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder)
            .and()
            .build()
    }
}