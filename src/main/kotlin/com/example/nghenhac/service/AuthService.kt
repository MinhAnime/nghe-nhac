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
            throw IllegalArgumentException("Username đã tồn tại")
        }
        if (userRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("Email đã tồn tại")
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



    /**
     * Xử lý logic đăng nhập
     */
    fun login(request: LoginRequestDTO): LoginResponseDTO {
        // 1. Tìm user bằng username
        val user = userRepository.findByUsername(request.username)
            ?: throw IllegalArgumentException("Username hoặc mật khẩu không đúng")

        // 2. KIỂM TRA MẬT KHẨU
        // Dùng `matches` để so sánh mật khẩu gốc (request.pass)
        // với mật khẩu đã băm (user.passwordHash) trong database
        if (!passwordEncoder.matches(request.pass, user.password)) {
            throw IllegalArgumentException("Username hoặc mật khẩu không đúng")
        }

        // 3. Nếu mật khẩu khớp, tạo JWT
        val token = jwtService.generateToken(user.username)

        // 4. Trả về token và thông tin user
        return LoginResponseDTO(
            token = token,
            user = UserResponseDTO(user.id!!, user.username, user.email)
        )
    }


}