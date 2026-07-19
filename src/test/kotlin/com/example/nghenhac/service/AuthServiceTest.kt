package com.example.nghenhac.service

import com.example.nghenhac.dto.LoginRequestDTO
import com.example.nghenhac.dto.RegisterRequestDTO
import com.example.nghenhac.models.User
import com.example.nghenhac.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.ArgumentMatchers.any
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtService: JwtService
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = Mockito.mock(UserRepository::class.java)
        passwordEncoder = Mockito.mock(PasswordEncoder::class.java)
        jwtService = Mockito.mock(JwtService::class.java)
        authService = AuthService(userRepository, passwordEncoder, jwtService)
    }

    @Test
    fun register_Successful() {
        val request = RegisterRequestDTO("testuser", "test@example.com", "password")
        `when`(userRepository.findByUsername(request.username)).thenReturn(null)
        `when`(userRepository.findByEmail(request.email)).thenReturn(null)
        `when`(passwordEncoder.encode(request.pass)).thenReturn("hashed_password")

        val userToSave = User(1L, "testuser", "test@example.com", "hashed_password")
        `when`(userRepository.save(any(User::class.java))).thenReturn(userToSave)

        val response = authService.register(request)

        assertNotNull(response)
        assertEquals(1L, response.id)
        assertEquals("testuser", response.username)
        assertEquals("test@example.com", response.email)
    }

    @Test
    fun register_ExistingUsername_ThrowsException() {
        val request = RegisterRequestDTO("testuser", "test@example.com", "password")
        val existingUser = User(1L, "testuser", "test@example.com", "hashed_password")
        `when`(userRepository.findByUsername(request.username)).thenReturn(existingUser)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.register(request)
        }
        assertEquals("Username đã tồn tại", exception.message)
    }

    @Test
    fun register_ExistingEmail_ThrowsException() {
        val request = RegisterRequestDTO("testuser", "test@example.com", "password")
        val existingUser = User(1L, "testuser", "test@example.com", "hashed_password")
        `when`(userRepository.findByUsername(request.username)).thenReturn(null)
        `when`(userRepository.findByEmail(request.email)).thenReturn(existingUser)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.register(request)
        }
        assertEquals("Email đã tồn tại", exception.message)
    }

    @Test
    fun login_Successful() {
        val request = LoginRequestDTO("testuser", "password")
        val existingUser = User(1L, "testuser", "test@example.com", "hashed_password")
        `when`(userRepository.findByUsername(request.username)).thenReturn(existingUser)
        `when`(passwordEncoder.matches(request.pass, existingUser.password)).thenReturn(true)
        `when`(jwtService.generateToken(existingUser.username)).thenReturn("mocked_jwt_token")

        val response = authService.login(request)

        assertNotNull(response)
        assertEquals("mocked_jwt_token", response.token)
        assertEquals(1L, response.user.id)
        assertEquals("testuser", response.user.username)
    }

    @Test
    fun login_InvalidUsername_ThrowsException() {
        val request = LoginRequestDTO("testuser", "password")
        `when`(userRepository.findByUsername(request.username)).thenReturn(null)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.login(request)
        }
        assertEquals("Username hoặc mật khẩu không đúng", exception.message)
    }

    @Test
    fun login_InvalidPassword_ThrowsException() {
        val request = LoginRequestDTO("testuser", "password")
        val existingUser = User(1L, "testuser", "test@example.com", "hashed_password")
        `when`(userRepository.findByUsername(request.username)).thenReturn(existingUser)
        `when`(passwordEncoder.matches(request.pass, existingUser.password)).thenReturn(false)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.login(request)
        }
        assertEquals("Username hoặc mật khẩu không đúng", exception.message)
    }
}
