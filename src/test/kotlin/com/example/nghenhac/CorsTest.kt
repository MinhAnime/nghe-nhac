package com.example.nghenhac

import org.junit.jupiter.api.Test
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.DefaultCorsProcessor
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class CorsTest {

    @Test
    fun testCorsConfiguration() {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val request = MockHttpServletRequest()
        request.method = "OPTIONS"
        request.requestURI = "/api/v1/auth/register"
        request.addHeader("Origin", "http://localhost:63342")
        request.addHeader("Access-Control-Request-Method", "POST")
        request.addHeader("Access-Control-Request-Headers", "content-type")

        val response = MockHttpServletResponse()
        val processor = DefaultCorsProcessor()
        val isValid = processor.processRequest(configuration, request, response)

        println("Is Valid CORS: $isValid")
        println("Response Status: ${response.status}")
        println("Response Headers: ${response.headerNames.associateWith { response.getHeaders(it) }}")

        assertTrue(isValid)
    }
}
