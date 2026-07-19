package com.example.nghenhac.service

import com.example.nghenhac.dto.*
import com.example.nghenhac.models.Playlist
import com.example.nghenhac.repository.PlaylistRepository
import com.example.nghenhac.repository.SongRepository
import com.example.nghenhac.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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


    fun createPlaylist(request: CreatePlaylistRequestDTO, username: String): PlaylistResponseDTO {

        val owner = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("user.not_found")


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
            isPublic = false
        )


        val savedPlaylist = playlistRepository.save(newPlaylist)


        return mapToPlaylistResponse(savedPlaylist)
    }

    fun deletePlaylist(playlistId: Long, username: String) {
        val playlist = playlistRepository.findById(playlistId)
            .orElseThrow { EntityNotFoundException("playlist.not_found") }

        val currentUser = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("user.not_found")

        // Kiểm tra quyền sở hữu
        if (playlist.owner.id != currentUser.id) {
            throw AccessDeniedException("playlist.delete_denied")
        }

        playlistRepository.delete(playlist)
    }
    fun removeSongFromPlaylist(playlistId: Long, songId: Long, username: String) {
        // Fetch playlist kèm songs để thao tác
        val playlist = playlistRepository.findPlaylistWithSongsAndArtistsById(playlistId)
            ?: throw EntityNotFoundException("playlist.not_found")

        val currentUser = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("user.not_found")

        if (playlist.owner.id != currentUser.id) {
            throw AccessDeniedException("playlist.edit_denied")
        }

        // Tìm bài hát trong danh sách hiện tại
        // tìm trong playlist.songs
        val songToRemove = playlist.songs.find { it.id == songId }
            ?: throw EntityNotFoundException("playlist.song_not_found")

        playlist.songs.remove(songToRemove)
        playlistRepository.save(playlist)
    }
    fun renamePlaylist(playlistId: Long, newName: String, username: String): PlaylistResponseDTO {
        val playlist = playlistRepository.findById(playlistId)
            .orElseThrow { EntityNotFoundException("playlist.not_found") }

        val currentUser = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("user.not_found")

        if (playlist.owner.id != currentUser.id) {
            throw AccessDeniedException("playlist.edit_denied")
        }

        playlist.name = newName
        val updatedPlaylist = playlistRepository.save(playlist)

        return mapToPlaylistResponse(updatedPlaylist)
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long, username: String): PlaylistResponseDTO {

        val playlist = playlistRepository.findPlaylistWithSongsAndArtistsById(playlistId)
            ?: throw EntityNotFoundException("playlist.not_found")

        val currentUser = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("user.not_found")


        if (playlist.owner.id != currentUser.id) {
            throw AccessDeniedException("playlist.edit_denied")
        }


        val song = songRepository.findById(songId)
            .orElseThrow { EntityNotFoundException("song.not_found") }

        if (playlist.songs.contains(song)) {
            throw IllegalArgumentException("song.already_in_playlist")
        }

        playlist.songs.add(song)


        val updatedPlaylist = playlistRepository.save(playlist)


        return mapToPlaylistResponse(updatedPlaylist)
    }


    fun getMyPlaylists(username: String, page: Int, size: Int): List<PlaylistSummaryDTO> {
        val owner = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("user.not_found")

        val pageable = PageRequest.of(page, size, Sort.by("id").descending())
        val playlistPage = playlistRepository.findPlaylistsByOwnerId(owner.id!!, pageable)
        return playlistPage.content.map { mapToPlaylistSummaryDTO(it) }
    }

    fun getPlaylistDetails(playlistId: Long, requesterUsername: String): PlaylistDetailDTO {

        val playlist = playlistRepository.findPlaylistWithSongsAndArtistsById(playlistId)
            ?: throw EntityNotFoundException("playlist.not_found")

        val requester = userRepository.findByUsername(requesterUsername)
            ?: throw EntityNotFoundException("user.not_found")

        val isOwner = playlist.owner.id == requester.id

        if (!isOwner && !playlist.isPublic) {
            throw AccessDeniedException("playlist.access_denied")
        }

        return mapToPlaylistDetailDTO(playlist)
    }

    fun togglePrivacy(playlistId: Long, username: String): PlaylistResponseDTO {
        val playlist = playlistRepository.findById(playlistId)
            .orElseThrow { EntityNotFoundException("playlist.not_found") }

        val currentUser = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("user.not_found")

        // Chỉ chủ sở hữu mới được đổi
        if (playlist.owner.id != currentUser.id) {
            throw AccessDeniedException("playlist.edit_denied")
        }

        // Đảo ngược trạng thái (True <-> False)
        playlist.isPublic = !playlist.isPublic

        val savedPlaylist = playlistRepository.save(playlist)
        return mapToPlaylistResponse(savedPlaylist)
    }

    private fun mapToPlaylistResponse(playlist: Playlist): PlaylistResponseDTO {

        val songDTOs = playlist.songs.map { song ->
            songService.mapToSongResponseDTO(song)
        }

        return PlaylistResponseDTO(
            id = playlist.id!!,
            name = playlist.name,
            ownerUsername = playlist.owner.username,
            isPublic = playlist.isPublic,
            songs = songDTOs
        )
    }

     fun mapToPlaylistSummaryDTO(playlist: Playlist): PlaylistSummaryDTO {

        val topSongs = songRepository.findPlaylistThumbnails(
            playlistId = playlist.id!!,
            pageable = PageRequest.of(0, 4)
        )

        val urls = topSongs.mapNotNull { song ->
            song.coverArtObjectName?.let { fileStorageService.getCoverUrl(it) }
        }

        return PlaylistSummaryDTO(
            id = playlist.id!!,
            name = playlist.name,
            ownerUsername = playlist.owner.username,
            isPublic = playlist.isPublic,
            thumbnails = urls
        )
    }

    fun getSongsInPlaylist(playlistId: Long, page: Int, size: Int): List<SongResponseDTO> {
        val pageable = PageRequest.of(page, size)
        val songPage = songRepository.findSongsByPlaylistIdOrdered(playlistId, pageable)
        return songPage.content.map { songService.mapToSongResponseDTO(it) }
    }


    private fun mapToPlaylistDetailDTO(playlist: Playlist): PlaylistDetailDTO {
        val songDTOs = playlist.songs.map { song ->
            songService.mapToSongResponseDTO(song)
        }

        return PlaylistDetailDTO(
            id = playlist.id!!,
            name = playlist.name,
            ownerUsername = playlist.owner.username,
            isPublic = playlist.isPublic,
            songs = songDTOs
        )
    }

}