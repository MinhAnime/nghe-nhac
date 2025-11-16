package com.example.nghenhac.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthFilter: JwtAuthFilter) {

    /**
     * Bean này định nghĩa cách chúng ta mã hóa mật khẩu.
     * Chúng ta sử dụng BCrypt, tiêu chuẩn an toàn hiện nay.
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * Cấu hình "bức tường lửa" cho ứng dụng.
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Tắt CSRF vì chúng ta dùng JWT (stateless)
            .csrf { it.disable() }

            // Cấu hình quy tắc cho các request
            .authorizeHttpRequests { auth ->
                auth
                    // Cho phép tất cả mọi người truy cập các API auth
                    .requestMatchers("/api/v1/auth/**").permitAll()

                    // Tạm thời cho phép truy cập API songs,
                    // sau này chúng ta sẽ bảo vệ chúng
                    .requestMatchers("/api/v1/songs/**").permitAll()
                    .requestMatchers("/api/v1/playlists/**").hasAuthority("USER")

                    // Bất kỳ request nào khác đều yêu cầu xác thực
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}