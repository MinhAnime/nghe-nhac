package com.example.nghenhac.config

import com.example.nghenhac.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component // Đánh dấu đây là một Bean
class JwtAuthFilter(
    private val jwtService: JwtService,
    // Chúng ta cần một cách để load thông tin User từ DB bằng username
    // Spring Security cung cấp interface UserDetailsService cho việc này
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() { // Đảm bảo filter chỉ chạy 1 lần/request

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        // Nếu header rỗng hoặc không bắt đầu bằng "Bearer ", bỏ qua
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        // Lấy token (bỏ "Bearer " đi)
        val token = authHeader.substring(7)
        val username: String

        try {
            username = jwtService.getUsernameFromToken(token)
        } catch (e: Exception) {
            // Token không hợp lệ (hết hạn, sai chữ ký, etc)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token")
            return
        }

        // Nếu user chưa được xác thực trong context
        if (SecurityContextHolder.getContext().authentication == null) {
            // Load thông tin user từ DB
            val userDetails = this.userDetailsService.loadUserByUsername(username)

            // Xác thực token
            if (jwtService.validateToken(token, userDetails.username)) {
                // Tạo một "vé" xác thực
                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails, // Principal (đối tượng user)
                    null,        // Credentials (không cần vì dùng JWT)
                    userDetails.authorities // Quyền (roles)
                )

                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)

                // Đặt "vé" vào SecurityContext.
                // Spring sẽ biết user này đã được xác thực
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        // Chuyển request đi tiếp
        filterChain.doFilter(request, response)
    }
}