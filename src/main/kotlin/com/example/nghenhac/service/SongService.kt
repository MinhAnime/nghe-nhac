package com.example.nghenhac.service

import com.example.nghenhac.dto.SongResponseDTO
import com.example.nghenhac.dto.SongUploadDTO
import com.example.nghenhac.models.Song
import com.example.nghenhac.repository.ArtistRepository
import com.example.nghenhac.repository.SongRepository
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
    private val fileStorageService: FileStorageService
) {


    fun createSong(dto: SongUploadDTO, songFile: MultipartFile, coverFile: MultipartFile?): SongResponseDTO {


        val artist = artistRepository.findById(dto.artistId)
            .orElseThrow { EntityNotFoundException("Artist không tồn tại với ID: ${dto.artistId}") }

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

    fun getSongStreamUrl(songId: Long): String {

        val song = songRepository.findById(songId)
            .orElseThrow { EntityNotFoundException("Song không tồn tại với ID: $songId") }

        val objectName = song.songObjectName

        return fileStorageService.getSongUrl(objectName)
    }


    fun getSongDetails(songId: Long): SongResponseDTO {
        val song = songRepository.findById(songId)
            .orElseThrow { EntityNotFoundException("Song không tồn tại với ID: $songId") }

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
    fun searchSongs(query: String): List<SongResponseDTO> {
        val songs = songRepository.searchSongs(query)
        return songs.map { mapToSongResponseDTO(it) }
    }
}