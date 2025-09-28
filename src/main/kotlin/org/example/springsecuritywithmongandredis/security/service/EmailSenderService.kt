package org.example.springsecuritywithmongandredis.security.service

import jakarta.mail.internet.MimeMessage
import org.example.springsecuritywithmongandredis.security.dto.AuthUserDto
import org.example.springsecuritywithmongandredis.security.entity.AuthUserEntity
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime


@Service
class EmailSenderService(
    private val javaMailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) {

    fun sendEmailVerify(authUserEntity: AuthUserEntity, verifyCode: Int){
        val message: MimeMessage = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        val context = createThymeleafContext(authUserEntity, verifyCode)



        val htmlContent: String = templateEngine.process("emails/verify-email", context)

        helper.setTo(authUserEntity.email)
        helper.setSubject("Welcome to Our Service!")
        helper.setText(htmlContent, true)

        javaMailSender.send(message)
    }


    fun createThymeleafContext(authUserEntity: AuthUserEntity, verifyCode: Int): Context {
        val verificationUrl = "http://localhost:8080/auth/email_verify?email=${authUserEntity.email}&verify_code=$verifyCode"

        return Context().apply {
            setVariable("appName", "OurService")
            setVariable("user", """
            ${authUserEntity.lastname} ${authUserEntity.firstname} ${authUserEntity.middlename} 
        """.trimIndent())
            setVariable("email", authUserEntity.email)
            setVariable("verificationLink", verificationUrl)
            setVariable("registrationDate", LocalDateTime.now())
            setVariable("verificationRequired", true)
        }

    }
}