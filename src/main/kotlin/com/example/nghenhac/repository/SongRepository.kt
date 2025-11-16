package com.example.nghenhac.repository

import com.example.nghenhac.models.Playlist
import com.example.nghenhac.models.Song
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface SongRepository :JpaRepository<Song, Long> {
    fun findByTitleContainingIgnoreCase(title: String): List<Song>
    fun findByArtistId(artistId: Long): List<Song>
    fun findFirstByPlaylistsContains(playlist: Playlist): Song?

    @Query("SELECT s FROM Song s JOIN FETCH s.artist")
    fun findAllWithArtist(): List<Song>
}