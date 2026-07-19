package com.example.nghenhac.service

import com.example.nghenhac.dto.SongResponseDTO
import com.example.nghenhac.dto.SongUploadDTO
import com.example.nghenhac.models.ListeningHistory
import com.example.nghenhac.models.Song
import com.example.nghenhac.repository.ArtistRepository
import com.example.nghenhac.repository.ListeningHistoryRepository
import com.example.nghenhac.repository.SongRepository
import com.example.nghenhac.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class SongService(
    private val songRepository: SongRepository,
    private val artistRepository: ArtistRepository,
    private val fileStorageService: FileStorageService,
    private val userRepository: UserRepository,
    private val listeningHistoryRepository: ListeningHistoryRepository
) {


    fun createSong(dto: SongUploadDTO, songFile: MultipartFile, coverFile: MultipartFile?): SongResponseDTO {


        val artist = artistRepository.findById(dto.artistId)
            .orElseThrow { EntityNotFoundException("artist.not_found|${dto.artistId}") }

        val songObjectName = fileStorageService.uploadSong(songFile)

        val coverObjectName = coverFile?.takeIf { !it.isEmpty }?.let {
            fileStorageService.uploadCover(it)
        }


        val newSong = Song(
            title = dto.title,
            durationSeconds = dto.durationSeconds,
            artist = artist,
            songObjectName = songObjectName,
            coverArtObjectName = coverObjectName
        )


        val savedSong = songRepository.save(newSong)


        return mapToSongResponseDTO(savedSong)
    }

    fun getSongStreamUrl(songId: Long, username: String? = null): String {
        val song = songRepository.findById(songId)
            .orElseThrow { EntityNotFoundException("song.not_found|$songId") }

        // Tăng lượt nghe
        song.playCount++
        songRepository.save(song)

        // Lưu lịch sử nghe nhạc nếu người dùng đã đăng nhập
        if (username != null) {
            val user = userRepository.findByUsername(username)
            if (user != null) {
                val history = ListeningHistory(user = user, song = song)
                listeningHistoryRepository.save(history)
            }
        }

        val objectName = song.songObjectName

        return fileStorageService.getSongUrl(objectName)
    }


    fun getSongDetails(songId: Long): SongResponseDTO {
        val song = songRepository.findById(songId)
            .orElseThrow { EntityNotFoundException("song.not_found|$songId") }

        return mapToSongResponseDTO(song)
    }


    internal fun mapToSongResponseDTO(song: Song): SongResponseDTO {
        // Nếu có ảnh bìa, lấy URL của nó
        val coverUrl = song.coverArtObjectName?.let {
            fileStorageService.getCoverUrl(it)
        }

        return SongResponseDTO(
            id = song.id!!,
            title = song.title,
            durationSeconds = song.durationSeconds,
            artistName = song.artist.name,
            coverArtUrl = coverUrl
        )
    }

    fun getAllSongs(page: Int, size: Int): List<SongResponseDTO> {
        val pageable = PageRequest.of(page, size, Sort.by("id").descending())
        val songPage = songRepository.findAllWithArtist(pageable)
        return songPage.content.map { mapToSongResponseDTO(it) }
    }

    fun toggleLikeSong(songId: Long, username: String): Boolean {
        val user = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("user.not_found")
        val song = songRepository.findById(songId)
            .orElseThrow { EntityNotFoundException("song.not_found|$songId") }

        val isLiked = if (user.likedSongs.contains(song)) {
            user.likedSongs.remove(song)
            false
        } else {
            user.likedSongs.add(song)
            true
        }
        userRepository.save(user)
        return isLiked
    }

    fun getLikedSongs(username: String, page: Int, size: Int): List<SongResponseDTO> {
        val user = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("user.not_found")
        val pageable = PageRequest.of(page, size, Sort.by("id").descending())
        val likedSongsPage = songRepository.findLikedSongsByUserId(user.id!!, pageable)
        return likedSongsPage.content.map { mapToSongResponseDTO(it) }
    }

    fun getListeningHistory(username: String, page: Int, size: Int): List<SongResponseDTO> {
        val user = userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("user.not_found")
        val pageable = PageRequest.of(page, size)
        val historyPage = listeningHistoryRepository.findByUserIdOrderByPlayedAtDesc(user.id!!, pageable)
        return historyPage.content.map { mapToSongResponseDTO(it.song) }
    }

}