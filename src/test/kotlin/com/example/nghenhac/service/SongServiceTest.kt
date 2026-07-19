package com.example.nghenhac.service

import com.example.nghenhac.dto.SongUploadDTO
import com.example.nghenhac.models.Artist
import com.example.nghenhac.models.Song
import com.example.nghenhac.models.User
import com.example.nghenhac.models.ListeningHistory
import com.example.nghenhac.repository.ArtistRepository
import com.example.nghenhac.repository.ListeningHistoryRepository
import com.example.nghenhac.repository.SongRepository
import com.example.nghenhac.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.ArgumentMatchers.any
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.web.multipart.MultipartFile
import java.util.*

class SongServiceTest {

    private lateinit var songRepository: SongRepository
    private lateinit var artistRepository: ArtistRepository
    private lateinit var fileStorageService: FileStorageService
    private lateinit var userRepository: UserRepository
    private lateinit var listeningHistoryRepository: ListeningHistoryRepository
    private lateinit var songService: SongService

    @BeforeEach
    fun setUp() {
        songRepository = Mockito.mock(SongRepository::class.java)
        artistRepository = Mockito.mock(ArtistRepository::class.java)
        fileStorageService = Mockito.mock(FileStorageService::class.java)
        userRepository = Mockito.mock(UserRepository::class.java)
        listeningHistoryRepository = Mockito.mock(ListeningHistoryRepository::class.java)
        songService = SongService(
            songRepository,
            artistRepository,
            fileStorageService,
            userRepository,
            listeningHistoryRepository
        )
    }

    @Test
    fun createSong_Successful() {
        val dto = SongUploadDTO("Song Title", 180, 1L)
        val artist = Artist(1L, "Artist Name", "cover.jpg")
        val songFile = Mockito.mock(MultipartFile::class.java)
        val coverFile = Mockito.mock(MultipartFile::class.java)

        `when`(songFile.isEmpty).thenReturn(false)
        `when`(coverFile.isEmpty).thenReturn(false)
        `when`(artistRepository.findById(1L)).thenReturn(Optional.of(artist))
        `when`(fileStorageService.uploadSong(songFile)).thenReturn("unique-song.mp3")
        `when`(fileStorageService.uploadCover(coverFile)).thenReturn("unique-cover.jpg")

        val songToSave = Song(
            id = 10L,
            title = dto.title,
            durationSeconds = dto.durationSeconds,
            artist = artist,
            songObjectName = "unique-song.mp3",
            coverArtObjectName = "unique-cover.jpg"
        )
        `when`(songRepository.save(any(Song::class.java) ?: songToSave)).thenReturn(songToSave)
        `when`(fileStorageService.getCoverUrl("unique-cover.jpg")).thenReturn("http://localhost:9000/covers/unique-cover.jpg")

        val response = songService.createSong(dto, songFile, coverFile)

        assertNotNull(response)
        assertEquals(10L, response.id)
        assertEquals("Song Title", response.title)
        assertEquals(180, response.durationSeconds)
        assertEquals("Artist Name", response.artistName)
        assertEquals("http://localhost:9000/covers/unique-cover.jpg", response.coverArtUrl)
    }

    @Test
    fun createSong_ArtistNotFound_ThrowsException() {
        val dto = SongUploadDTO("Song Title", 180, 1L)
        val songFile = Mockito.mock(MultipartFile::class.java)

        `when`(artistRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows(EntityNotFoundException::class.java) {
            songService.createSong(dto, songFile, null)
        }
        assertEquals("Artist không tồn tại với ID: 1", exception.message)
    }

    @Test
    fun getSongStreamUrl_Successful() {
        val artist = Artist(1L, "Artist Name")
        val song = Song(
            id = 10L,
            title = "Song Title",
            durationSeconds = 180,
            artist = artist,
            songObjectName = "unique-song.mp3"
        )
        `when`(songRepository.findById(10L)).thenReturn(Optional.of(song))
        `when`(fileStorageService.getSongUrl("unique-song.mp3")).thenReturn("http://localhost:9000/songs/unique-song.mp3?token=abc")

        val url = songService.getSongStreamUrl(10L)

        assertEquals("http://localhost:9000/songs/unique-song.mp3?token=abc", url)
    }

    @Test
    fun getSongStreamUrl_SongNotFound_ThrowsException() {
        `when`(songRepository.findById(10L)).thenReturn(Optional.empty())

        val exception = assertThrows(EntityNotFoundException::class.java) {
            songService.getSongStreamUrl(10L)
        }
        assertEquals("Song không tồn tại với ID: 10", exception.message)
    }

    @Test
    fun toggleLikeSong_Like_Successful() {
        val artist = Artist(1L, "Artist Name")
        val song = Song(id = 10L, title = "Song Title", durationSeconds = 180, artist = artist, songObjectName = "unique-song.mp3")
        val user = User(id = 1L, username = "testuser", email = "test@example.com", password = "password")

        `when`(userRepository.findByUsername("testuser")).thenReturn(user)
        `when`(songRepository.findById(10L)).thenReturn(Optional.of(song))

        val isLiked = songService.toggleLikeSong(10L, "testuser")

        assertTrue(isLiked)
        assertTrue(user.likedSongs.contains(song))
    }

    @Test
    fun toggleLikeSong_Unlike_Successful() {
        val artist = Artist(1L, "Artist Name")
        val song = Song(id = 10L, title = "Song Title", durationSeconds = 180, artist = artist, songObjectName = "unique-song.mp3")
        val user = User(id = 1L, username = "testuser", email = "test@example.com", password = "password")
        user.likedSongs.add(song)

        `when`(userRepository.findByUsername("testuser")).thenReturn(user)
        `when`(songRepository.findById(10L)).thenReturn(Optional.of(song))

        val isLiked = songService.toggleLikeSong(10L, "testuser")

        assertFalse(isLiked)
        assertFalse(user.likedSongs.contains(song))
    }

    @Test
    fun getLikedSongs_Successful() {
        val artist = Artist(1L, "Artist Name")
        val song = Song(id = 10L, title = "Song Title", durationSeconds = 180, artist = artist, songObjectName = "unique-song.mp3")
        val user = User(id = 1L, username = "testuser", email = "test@example.com", password = "password")

        `when`(userRepository.findByUsername("testuser")).thenReturn(user)
        `when`(songRepository.findLikedSongsByUserId(Mockito.eq(1L), any(Pageable::class.java) ?: PageRequest.of(0, 10)))
            .thenReturn(PageImpl(listOf(song)))

        val result = songService.getLikedSongs("testuser", 0, 10)

        assertEquals(1, result.size)
        assertEquals("Song Title", result[0].title)
    }

    @Test
    fun getListeningHistory_Successful() {
        val artist = Artist(1L, "Artist Name")
        val song = Song(id = 10L, title = "Song Title", durationSeconds = 180, artist = artist, songObjectName = "unique-song.mp3")
        val user = User(id = 1L, username = "testuser", email = "test@example.com", password = "password")
        val history = ListeningHistory(id = 5L, user = user, song = song)

        `when`(userRepository.findByUsername("testuser")).thenReturn(user)
        `when`(listeningHistoryRepository.findByUserIdOrderByPlayedAtDesc(Mockito.eq(1L), any(Pageable::class.java) ?: PageRequest.of(0, 10)))
            .thenReturn(PageImpl(listOf(history)))

        val result = songService.getListeningHistory("testuser", 0, 10)

        assertEquals(1, result.size)
        assertEquals("Song Title", result[0].title)
    }
}
