package com.example.nghenhac.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey
import kotlin.time.Duration

@Service
class JwtService {


    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String


    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    private val expirationTimeMs: Long = 2_592_000_000


    fun generateToken(username: String): String {
        val now = Date()
        val expirationDate = Date(now.time + expirationTimeMs)

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(secretKey)
            .compact()
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun getUsernameFromToken(token: String): String {
        return extractAllClaims(token).subject
    }


    fun validateToken(token: String, username: String): Boolean {
        val tokenUsername = getUsernameFromToken(token)
        val isTokenExpired = extractAllClaims(token).expiration.before(Date())

        return (tokenUsername == username && !isTokenExpired)
    }
}