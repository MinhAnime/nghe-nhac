package com.example.nghenhac.repository

import com.example.nghenhac.models.Playlist
import com.example.nghenhac.models.Song
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface SongRepository :JpaRepository<Song, Long> {
    fun findTop4ByPlaylistsContains(playlist: Playlist): List<Song>

    @Query("SELECT s FROM Song s JOIN FETCH s.artist")
    fun findAllWithArtist(pageable: Pageable): Page<Song>

    @Query("SELECT s FROM Song s JOIN s.playlists p WHERE p.id = :playlistId")
    fun findByPlaylistId(@Param("playlistId") playlistId: Long, pageable: Pageable): Page<Song>

    @Query("""
        SELECT s FROM Song s JOIN FETCH s.artist a 
        WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) 
        OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    fun searchSongs(@Param("query") query: String): List<Song>
}