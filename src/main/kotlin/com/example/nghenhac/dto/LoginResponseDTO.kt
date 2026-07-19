package com.example.nghenhac.dto

data class LoginResponseDTO(
    val token: String,
    val user: UserResponseDTO
)
