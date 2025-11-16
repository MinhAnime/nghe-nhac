package com.example.nghenhac.controller

import com.example.nghenhac.dto.LoginRequestDTO
import com.example.nghenhac.dto.LoginResponseDTO
import com.example.nghenhac.dto.RegisterRequestDTO
import com.example.nghenhac.dto.UserResponseDTO
import com.example.nghenhac.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun registerUser(@RequestBody request: RegisterRequestDTO): ResponseEntity<UserResponseDTO> {
        val userResponse = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse)
    }

    @PostMapping("/login")
    fun loginUser(@RequestBody request: LoginRequestDTO): ResponseEntity<LoginResponseDTO> {
        val loginResponse = authService.login(request)
        return ResponseEntity.ok(loginResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to (ex.message ?: "Invalid request")))
    }
}