package com.example.nghenhac.repository

import com.example.nghenhac.models.Playlist
import com.example.nghenhac.models.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface PlaylistRepository : JpaRepository<Playlist, Long> {

    @Query("SELECT p FROM Playlist p WHERE p.owner.id = :ownerId")
    fun findPlaylistsByOwnerId(@Param("ownerId") ownerId: Long , pageable: Pageable): Page<Playlist>

    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.songs s LEFT JOIN FETCH s.artist WHERE p.id = :playlistId")
    fun findPlaylistWithSongsAndArtistsById(@Param("playlistId") playlistId: Long): Playlist?

    fun findByOwnerId(ownerId: Long): List<Playlist>
    fun findByOwnerIdAndIsPublicTrue(ownerId: Long): List<Playlist>

    fun existsByNameAndOwner(name: String, owner: User): Boolean

    @Query("""
        SELECT p FROM Playlist p 
        WHERE p.isPublic = true 
        AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    fun searchPublicPlaylists(@Param("query") query: String): List<Playlist>
}