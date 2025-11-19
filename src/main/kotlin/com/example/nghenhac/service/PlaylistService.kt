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
            isPublic = false
        )


        val savedPlaylist = playlistRepository.save(newPlaylist)


        return mapToPlaylistResponse(savedPlaylist)
    }


    fun addSongToPlaylist(playlistId: Long, songId: Long, username: String): PlaylistResponseDTO {

        val playlist = playlistRepository.findPlaylistWithSongsAndArtistsById(playlistId)
            ?: throw EntityNotFoundException("Playlist không tồn tại.")

        val currentUser = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("User không tồn tại.")


        if (playlist.owner.id != currentUser.id) {
            throw AccessDeniedException("Bạn không có quyền thêm bài hát vào playlist này.")
        }


        val song = songRepository.findById(songId)
            .orElseThrow { EntityNotFoundException("Bài hát không tồn tại.") }

        if (playlist.songs.contains(song)) {
            throw IllegalArgumentException("Bài hát đã có trong playlist.")
        }

        playlist.songs.add(song)


        val updatedPlaylist = playlistRepository.save(playlist)


        return mapToPlaylistResponse(updatedPlaylist)
    }


    fun getMyPlaylists(username: String, page: Int, size: Int): List<PlaylistSummaryDTO> {
        val owner = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("User không tồn tại.")

        val pageable = PageRequest.of(page, size, Sort.by("id").descending())
        val playlistPage = playlistRepository.findPlaylistsByOwnerId(owner.id!!, pageable)
        return playlistPage.content.map { mapToPlaylistSummaryDTO(it) }
    }

    fun getPlaylistDetails(playlistId: Long): PlaylistDetailDTO {

        val playlist = playlistRepository.findPlaylistWithSongsAndArtistsById(playlistId)
            ?: throw EntityNotFoundException("Playlist không tồn tại.")

        return mapToPlaylistDetailDTO(playlist)
    }

    private fun mapToPlaylistResponse(playlist: Playlist): PlaylistResponseDTO {

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

    fun getSongsInPlaylist(playlistId: Long, page: Int, size: Int): List<SongResponseDTO> {
        val pageable = PageRequest.of(page, size, Sort.by("id").descending())
        val songPage = songRepository.findByPlaylistId(playlistId, pageable)
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
            songs = songDTOs
        )
    }

}