package com.example.nghenhac.service

import com.example.nghenhac.dto.CreatePlaylistRequestDTO
import com.example.nghenhac.dto.PlaylistDetailDTO
import com.example.nghenhac.dto.PlaylistResponseDTO
import com.example.nghenhac.dto.PlaylistSummaryDTO
import com.example.nghenhac.models.Playlist
import com.example.nghenhac.repository.PlaylistRepository
import com.example.nghenhac.repository.SongRepository
import com.example.nghenhac.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service

@Service
class PlaylistService(
    private val playlistRepository: PlaylistRepository,
    private val userRepository: UserRepository,
    private val songRepository: SongRepository,
    private val songService: SongService,
    private val fileStorageService: FileStorageService
) {

    /**
     * Tạo một playlist mới.
     * @param request DTO chứa tên playlist.
     * @param username Tên của user đang đăng nhập (lấy từ JWT).
     */
    fun createPlaylist(request: CreatePlaylistRequestDTO, username: String): PlaylistResponseDTO {
        // 1. Tìm user đang đăng nhập
        val owner = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("User không tồn tại.")


        val baseName = request.name
        var finalName = baseName


        if (playlistRepository.existsByNameAndOwner(finalName, owner)) {
            var counter = 1
            finalName = "$baseName ($counter)"

            while (playlistRepository.existsByNameAndOwner(finalName, owner)) {
                counter++
                finalName = "$baseName ($counter)"
            }
        }


        val newPlaylist = Playlist(
            name = finalName,
            owner = owner,
            isPublic = false // Mặc định là private
        )

        // 3. Lưu vào database
        val savedPlaylist = playlistRepository.save(newPlaylist)

        // 4. Trả về DTO (chưa có bài hát nào)
        return mapToPlaylistResponse(savedPlaylist)
    }

    /**
     * Thêm một bài hát vào playlist.
     * @param playlistId ID của playlist.
     * @param songId ID của bài hát.
     * @param username Tên của user đang đăng nhập.
     */
    fun addSongToPlaylist(playlistId: Long, songId: Long, username: String): PlaylistResponseDTO {
        // 1. Tìm playlist
        val playlist = playlistRepository.findPlaylistWithSongsAndArtistsById(playlistId)
            ?: throw EntityNotFoundException("Playlist không tồn tại.")
        // 2. Tìm user đang đăng nhập
        val currentUser = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("User không tồn tại.")

        // 3. KIỂM TRA QUYỀN: User có phải là chủ playlist không?
        if (playlist.owner.id != currentUser.id) {
            throw AccessDeniedException("Bạn không có quyền thêm bài hát vào playlist này.")
        }

        // 4. Tìm bài hát
        val song = songRepository.findById(songId)
            .orElseThrow { EntityNotFoundException("Bài hát không tồn tại.") }

        if (playlist.songs.contains(song)) {
            throw IllegalArgumentException("Bài hát đã có trong playlist.")
        }

        // 5. Thêm bài hát vào set (MutableSet sẽ tự động lo việc trùng lặp)
        playlist.songs.add(song)

        // 6. Lưu lại playlist (với bài hát mới)
        val updatedPlaylist = playlistRepository.save(playlist)

        // 7. Trả về DTO
        return mapToPlaylistResponse(updatedPlaylist)
    }

    /**
     * Lấy danh sách các playlist của user đang đăng nhập.
     */
    fun getMyPlaylists(username: String): List<PlaylistSummaryDTO> {
        val owner = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("User không tồn tại.")

        // Gọi hàm KHÔNG fetch songs
        val playlists = playlistRepository.findPlaylistsByOwnerId(owner.id!!)

        // Map sang DTO tóm tắt
        return playlists.map { mapToPlaylistSummaryDTO(it) }
    }

    fun getPlaylistDetails(playlistId: Long): PlaylistDetailDTO {
        // Gọi hàm CÓ fetch songs
        val playlist = playlistRepository.findPlaylistWithSongsAndArtistsById(playlistId)
            ?: throw EntityNotFoundException("Playlist không tồn tại.")

        // (Bạn có thể thêm 1 bước kiểm tra bảo mật ở đây,
        // ví dụ: nếu playlist là private, chỉ chủ sở hữu mới được xem)

        // Map sang DTO chi tiết
        return mapToPlaylistDetailDTO(playlist)
    }

    // (Hàm getPlaylistDetails(playlistId) có thể thêm ở đây)

    /**
     * Helper: Chuyển từ Entity sang DTO
     */
    private fun mapToPlaylistResponse(playlist: Playlist): PlaylistResponseDTO {
        // Chuyển đổi danh sách Song Entity sang SongResponseDTO (có URL)
        val songDTOs = playlist.songs.map { song ->
            songService.mapToSongResponseDTO(song)
        }

        return PlaylistResponseDTO(
            id = playlist.id!!,
            name = playlist.name,
            ownerUsername = playlist.owner.username,
            songs = songDTOs
        )
    }

    private fun mapToPlaylistSummaryDTO(playlist: Playlist): PlaylistSummaryDTO {
        // Lấy ảnh bìa của bài hát đầu tiên (nếu có)
        // Đây là 1 query nhỏ, nhưng chấp nhận được (tốt hơn là tải 1000 bài hát)
        val firstSong = songRepository.findFirstByPlaylistsContains(playlist)

        val coverUrl = firstSong?.coverArtObjectName?.let {
            fileStorageService.getCoverUrl(it)
        }

        return PlaylistSummaryDTO(
            id = playlist.id!!,
            name = playlist.name,
            ownerUsername = playlist.owner.username,
            coverArtUrl = coverUrl
        )
    }
    private fun mapToPlaylistDetailDTO(playlist: Playlist): PlaylistDetailDTO {
        val songDTOs = playlist.songs.map { song ->
            songService.mapToSongResponseDTO(song)
        }

        return PlaylistDetailDTO(
            id = playlist.id!!,
            name = playlist.name,
            ownerUsername = playlist.owner.username,
            songs = songDTOs
        )
    }

}