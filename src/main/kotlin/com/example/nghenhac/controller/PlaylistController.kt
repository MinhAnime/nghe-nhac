package com.example.nghenhac.controller

import com.example.nghenhac.dto.*
import com.example.nghenhac.service.PlaylistService
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/playlists")
class PlaylistController(
    private val playlistService: PlaylistService
) {

    @PostMapping
    fun createPlaylist(
        @RequestBody request: CreatePlaylistRequestDTO,
        authentication: Authentication
    ): ResponseEntity<PlaylistResponseDTO> {


        val username = authentication.name

        val playlist = playlistService.createPlaylist(request, username)
        return ResponseEntity.status(HttpStatus.CREATED).body(playlist)
    }


    @PostMapping("/{playlistId}/songs")
    fun addSongToPlaylist(
        @PathVariable playlistId: Long,
        @RequestBody request: AddSongToPlaylistRequestDTO,
        authentication: Authentication
    ): ResponseEntity<PlaylistResponseDTO> {

        val username = authentication.name
        val updatedPlaylist = playlistService.addSongToPlaylist(
            playlistId,
            request.songId,
            username
        )
        return ResponseEntity.ok(updatedPlaylist)
    }

    @GetMapping("/my-playlists")
    fun getMyPlaylists(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<PlaylistSummaryDTO>> {
        val username = authentication.name
        val playlists = playlistService.getMyPlaylists(username, page, size)
        return ResponseEntity.ok(playlists)
    }

    @GetMapping("/{playlistId}")
    fun getPlaylistById(
        @PathVariable playlistId: Long,
        authentication: Authentication
    ): ResponseEntity<PlaylistDetailDTO> {
        // Service sẽ lo việc kiểm tra (nếu playlist là private)
        val playlistDetails = playlistService.getPlaylistDetails(playlistId)
        return ResponseEntity.ok(playlistDetails)
    }
    @GetMapping("/{playlistId}/songs")
    fun getSongsInPlaylist(
        @PathVariable playlistId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<SongResponseDTO>> {
        return ResponseEntity.ok(playlistService.getSongsInPlaylist(playlistId, page, size))
    }


    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf("error" to (ex.message ?: "Không có quyền truy cập")))
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to (ex.message ?: "Không tìm thấy tài nguyên")))
    }
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf("message" to (ex.message ?: "Lỗi không xác định")))
    }
}