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
        val playlistDetails = playlistService.getPlaylistDetails(playlistId, authentication.name)
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

    @DeleteMapping("/{playlistId}")
    fun deletePlaylist(
        @PathVariable playlistId: Long,
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        playlistService.deletePlaylist(playlistId, authentication.name)
        return ResponseEntity.ok(mapOf("message" to "Đã xóa playlist thành công"))
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    fun removeSongFromPlaylist(
        @PathVariable playlistId: Long,
        @PathVariable songId: Long,
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        playlistService.removeSongFromPlaylist(playlistId, songId, authentication.name)
        return ResponseEntity.ok(mapOf("message" to "Đã xóa bài hát khỏi playlist"))
    }

    @PutMapping("/{playlistId}")
    fun renamePlaylist(
        @PathVariable playlistId: Long,
        @RequestBody request: RenamePlaylistRequestDTO,
        authentication: Authentication
    ): ResponseEntity<PlaylistResponseDTO> {
        val result = playlistService.renamePlaylist(playlistId, request.name, authentication.name)
        return ResponseEntity.ok(result)
    }

    @PutMapping("/{playlistId}/privacy")
    fun togglePrivacy(
        @PathVariable playlistId: Long,
        authentication: Authentication
    ): ResponseEntity<PlaylistResponseDTO> {
        val result = playlistService.togglePrivacy(playlistId, authentication.name)
        return ResponseEntity.ok(result)
    }
}