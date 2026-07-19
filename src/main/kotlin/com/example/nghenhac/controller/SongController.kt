package com.example.nghenhac.controller

import com.example.nghenhac.dto.SongResponseDTO
import com.example.nghenhac.dto.SongUploadDTO
import com.example.nghenhac.service.SongService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/v1/songs")
class SongController(
    private val songService: SongService
) {

    @PostMapping("/upload")
    fun uploadSong(
        @RequestParam("title") title: String,
        @RequestParam("duration") duration: Int,
        @RequestParam("artistId") artistId: Long,
        @RequestPart("songFile") songFile: MultipartFile,
        @RequestPart("coverFile") coverFile: MultipartFile?
    ): ResponseEntity<SongResponseDTO> {

        val dto = SongUploadDTO(
            title = title,
            durationSeconds = duration,
            artistId = artistId
        )

        val newSongResponse = songService.createSong(dto, songFile, coverFile)

        return ResponseEntity.status(HttpStatus.CREATED).body(newSongResponse)
    }

    @GetMapping
    fun getAllSongs(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<SongResponseDTO>> {
        return ResponseEntity.ok(songService.getAllSongs(page, size))
    }

    @GetMapping("/{songId}")
    fun getSongDetails(@PathVariable songId: Long): ResponseEntity<SongResponseDTO> {
        val songResponse = songService.getSongDetails(songId)
        return ResponseEntity.ok(songResponse)
    }

    @GetMapping("/stream/{songId}")
    fun streamSong(
        @PathVariable songId: Long,
        authentication: Authentication?
    ): ResponseEntity<Void> {
        val username = authentication?.name
        val presignedUrl = songService.getSongStreamUrl(songId, username)

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(presignedUrl))
            .build()
    }

    @PostMapping("/{songId}/like")
    fun toggleLikeSong(
        @PathVariable songId: Long,
        authentication: Authentication
    ): ResponseEntity<Map<String, Any>> {
        val username = authentication.name
        val isLiked = songService.toggleLikeSong(songId, username)
        return ResponseEntity.ok(
            mapOf(
                "liked" to isLiked,
                "message" to if (isLiked) "Đã thích bài hát" else "Đã bỏ thích bài hát"
            )
        )
    }

    @GetMapping("/liked")
    fun getLikedSongs(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<SongResponseDTO>> {
        val username = authentication.name
        return ResponseEntity.ok(songService.getLikedSongs(username, page, size))
    }

    @GetMapping("/history")
    fun getListeningHistory(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<SongResponseDTO>> {
        val username = authentication.name
        return ResponseEntity.ok(songService.getListeningHistory(username, page, size))
    }
}