package com.example.nghenhac.exception

import jakarta.persistence.EntityNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val messageSource: MessageSource
) {

    private fun resolveMessage(messageKey: String?, defaultKey: String): String {
        if (messageKey == null) {
            return translate(defaultKey)
        }
        val parts = messageKey.split("|")
        val key = parts[0]
        val args = if (parts.size > 1) parts.drop(1).toTypedArray() else null
        return try {
            messageSource.getMessage(key, args, LocaleContextHolder.getLocale())
        } catch (e: NoSuchMessageException) {
            // Nếu không tìm thấy key trong properties, trả về chính messageKey hoặc dịch defaultKey
            if (key.contains(".")) {
                key
            } else {
                translate(defaultKey)
            }
        }
    }

    private fun translate(key: String): String {
        return try {
            messageSource.getMessage(key, null, LocaleContextHolder.getLocale())
        } catch (e: NoSuchMessageException) {
            key
        }
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFoundException(ex: EntityNotFoundException): ResponseEntity<Map<String, String>> {
        val translated = resolveMessage(ex.message, "error.not_found")
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to translated))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        val translated = resolveMessage(ex.message, "error.bad_request")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to translated))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<Map<String, String>> {
        val translated = resolveMessage(ex.message, "error.forbidden")
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf("error" to translated))
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        val translated = resolveMessage(ex.message, "error.internal")
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to translated))
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ResponseEntity<Map<String, String>> {
        val translated = resolveMessage(ex.message, "error.unknown")
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to translated))
    }
}
