package com.example.nghenhac.service

import com.example.nghenhac.dto.SongResponseDTO
import com.example.nghenhac.dto.SongUploadDTO
import com.example.nghenhac.models.Song
import com.example.nghenhac.repository.ArtistRepository
import com.example.nghenhac.repository.SongRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class SongService(
    // Tiêm (inject) các dependencies cần thiết
    private val songRepository: SongRepository,
    private val artistRepository: ArtistRepository,
    private val fileStorageService: FileStorageService
) {

    /**
     * Xử lý nghiệp vụ upload một bài hát mới.
     */
    fun createSong(dto: SongUploadDTO, songFile: MultipartFile, coverFile: MultipartFile?): SongResponseDTO {

        // 1. Kiểm tra xem Artist có tồn tại không
        val artist = artistRepository.findById(dto.artistId)
            .orElseThrow { EntityNotFoundException("Artist không tồn tại với ID: ${dto.artistId}") }

        // 2. Upload file nhạc lên MinIO
        val songObjectName = fileStorageService.uploadSong(songFile)

        // 3. Upload file ảnh bìa (nếu có)
        val coverObjectName = coverFile?.takeIf { !it.isEmpty }?.let {
            fileStorageService.uploadCover(it)
        }

        // 4. Tạo đối tượng Song (Entity)
        val newSong = Song(
            title = dto.title,
            durationSeconds = dto.durationSeconds,
            artist = artist,
            songObjectName = songObjectName,
            coverArtObjectName = coverObjectName
        )

        // 5. Lưu metadata của Song vào PostgreSQL
        val savedSong = songRepository.save(newSong)

        // 6. Chuyển đổi sang DTO để trả về cho client
        return mapToSongResponseDTO(savedSong)
    }

    /**
     * Lấy URL (presigned) để client stream nhạc.
     */
    fun getSongStreamUrl(songId: Long): String {
        // 1. Tìm thông tin bài hát trong DB
        val song = songRepository.findById(songId)
            .orElseThrow { EntityNotFoundException("Song không tồn tại với ID: $songId") }

        // 2. Lấy tên object từ DB
        val objectName = song.songObjectName

        // 3. Tạo presigned URL từ MinIO
        return fileStorageService.getSongUrl(objectName)
    }

    /**
     * Lấy thông tin chi tiết của bài hát (để hiển thị).
     */
    fun getSongDetails(songId: Long): SongResponseDTO {
        val song = songRepository.findById(songId)
            .orElseThrow { EntityNotFoundException("Song không tồn tại với ID: $songId") }

        return mapToSongResponseDTO(song)
    }

    /**
     * Hàm helper để chuyển từ Entity (database) sang DTO (response).
     * Việc này giúp chúng ta tạo ra 'coverArtUrl'.
     */
    internal fun mapToSongResponseDTO(song: Song): SongResponseDTO {
        // Nếu có ảnh bìa, lấy URL của nó
        val coverUrl = song.coverArtObjectName?.let {
            fileStorageService.getCoverUrl(it)
        }

        return SongResponseDTO(
            id = song.id!!, // !! vì khi đã lưu thì chắc chắn có id
            title = song.title,
            durationSeconds = song.durationSeconds,
            artistName = song.artist.name, // Lấy tên nghệ sĩ
            coverArtUrl = coverUrl
        )
    }
}