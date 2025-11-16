package com.example.nghenhac.config

import com.example.nghenhac.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

@Configuration
class ApplicationConfig(
    private val userRepository: UserRepository
) {

    @Bean
    fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username ->
            val user = userRepository.findByUsername(username)
                ?: throw UsernameNotFoundException("User not found with username: $username")

            org.springframework.security.core.userdetails.User
                .withUsername(user.username)
                .password(user.password)
                .authorities("USER")
                .build()
        }
    }

}