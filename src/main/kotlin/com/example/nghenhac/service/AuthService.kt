package com.example.nghenhac.service

import com.example.nghenhac.dto.LoginRequestDTO
import com.example.nghenhac.dto.LoginResponseDTO
import com.example.nghenhac.dto.RegisterRequestDTO
import com.example.nghenhac.dto.UserResponseDTO
import com.example.nghenhac.models.User
import com.example.nghenhac.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    fun register(request: RegisterRequestDTO): UserResponseDTO {
        if (userRepository.findByUsername(request.username) != null) {
            throw IllegalArgumentException("auth.username.exists")
        }
        if (userRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("auth.email.exists")
        }


        val hashedPassword = passwordEncoder.encode(request.pass)

        val newUser = User(
            username = request.username,
            email = request.email,
            password = hashedPassword
        )

        val savedUser = userRepository.save(newUser)

        return UserResponseDTO(savedUser.id!!, savedUser.username, savedUser.email)
    }


    fun login(request: LoginRequestDTO): LoginResponseDTO {

        val user = userRepository.findByUsername(request.username)
            ?: throw IllegalArgumentException("auth.bad_credentials")


        if (!passwordEncoder.matches(request.pass, user.password)) {
            throw IllegalArgumentException("auth.bad_credentials")
        }


        val token = jwtService.generateToken(user.username)


        return LoginResponseDTO(
            token = token,
            user = UserResponseDTO(user.id!!, user.username, user.email)
        )
    }


}