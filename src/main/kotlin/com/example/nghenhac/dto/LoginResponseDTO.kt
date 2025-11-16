package com.example.nghenhac.dto

data class LoginResponseDTO(
    val token: String, // Chuỗi JWT
    val user: UserResponseDTO
)
