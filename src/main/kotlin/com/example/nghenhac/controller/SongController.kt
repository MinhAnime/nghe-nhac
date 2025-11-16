package com.example.nghenhac.controller

import com.example.nghenhac.dto.SongResponseDTO
import com.example.nghenhac.dto.SongUploadDTO
import com.example.nghenhac.service.SongService
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.util.*

@RestController // Đánh dấu đây là một API Controller (trả về JSON/HTTP response)
@RequestMapping("/api/v1/songs") // Tiền tố chung cho tất cả API trong class này
class SongController(
    // Tiêm (inject) SongService
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
    fun getAllSongs(): ResponseEntity<List<SongResponseDTO>> {
        return ResponseEntity.ok(songService.getAllSongs())
    }

    @GetMapping("/{songId}")
    fun getSongDetails(@PathVariable songId: Long): ResponseEntity<SongResponseDTO> {
        val songResponse = songService.getSongDetails(songId)
        return ResponseEntity.ok(songResponse)
    }

    @GetMapping("/stream/{songId}")
    fun streamSong(@PathVariable songId: Long): ResponseEntity<Void> {
        val presignedUrl = songService.getSongStreamUrl(songId)


        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(presignedUrl))
            .build()
    }


    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to (ex.message ?: "Resource not found")))
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<Map<String, String>> {

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to (ex.message ?: "An unexpected error occurred")))
    }
}