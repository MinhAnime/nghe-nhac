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

    /**
     * API 1: Upload một bài hát mới.
     * Client sẽ gửi request dạng multipart/form-data.
     *
     * POST /api/v1/songs/upload
     */
    @PostMapping("/upload")
    fun uploadSong(
        // Dùng @RequestParam cho các trường metadata
        @RequestParam("title") title: String,
        @RequestParam("duration") duration: Int,
        @RequestParam("artistId") artistId: Long,

        // Dùng @RequestPart cho các file
        @RequestPart("songFile") songFile: MultipartFile,
        @RequestPart("coverFile") coverFile: MultipartFile? // Ảnh bìa có thể có hoặc không
    ): ResponseEntity<SongResponseDTO> {

        // 1. Tạo DTO từ các tham số
        val dto = SongUploadDTO(
            title = title,
            durationSeconds = duration,
            artistId = artistId
        )

        val newSongResponse = songService.createSong(dto, songFile, coverFile)

        // 3. Trả về 201 CREATED và thông tin bài hát đã tạo
        return ResponseEntity.status(HttpStatus.CREATED).body(newSongResponse)
    }

    /**
     * API 2: Lấy thông tin chi tiết của bài hát (metadata).
     * Trả về JSON chứa thông tin bài hát và URL của ảnh bìa.
     *
     * GET /api/v1/songs/{songId}
     */
    @GetMapping("/{songId}")
    fun getSongDetails(@PathVariable songId: Long): ResponseEntity<SongResponseDTO> {
        val songResponse = songService.getSongDetails(songId)
        return ResponseEntity.ok(songResponse) // Trả về 200 OK và JSON
    }

    /**
     * API 3: Lấy URL để stream nhạc (Quan trọng!).
     * API này không trả về JSON, mà trả về một HTTP Redirect (302).
     *
     * GET /api/v1/songs/stream/{songId}
     */
    @GetMapping("/stream/{songId}")
    fun streamSong(@PathVariable songId: Long): ResponseEntity<Void> {
        // 1. Lấy URL (đã ký) từ MinIO thông qua service
        val presignedUrl = songService.getSongStreamUrl(songId)

        // 2. Trả về status 302 (Found) / 307 (Temporary Redirect)
        // Header 'Location' sẽ chứa URL của MinIO.
        // Trình duyệt hoặc app (client) sẽ tự động đi đến URL này để lấy file.
        return ResponseEntity
            .status(HttpStatus.FOUND) // 302 Found
            .location(URI.create(presignedUrl))
            .build()
    }

    /**
     * Xử lý lỗi tập trung.
     * Nếu Service ném ra EntityNotFoundException (ví dụ: tìm bài hát/artist không thấy),
     * API sẽ tự động trả về lỗi 404 Not Found thay vì 500 Internal Server Error.
     */
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to (ex.message ?: "Resource not found")))
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        // Xử lý chung cho các lỗi upload file hoặc lỗi MinIO
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to (ex.message ?: "An unexpected error occurred")))
    }
}