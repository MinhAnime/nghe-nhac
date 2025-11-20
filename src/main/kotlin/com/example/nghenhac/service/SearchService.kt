package com.example.nghenhac.service

import com.example.nghenhac.dto.SearchResponseDTO
import com.example.nghenhac.repository.PlaylistRepository
import com.example.nghenhac.repository.SongRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SearchService(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val songService: SongService,
    private val playlistService: PlaylistService
) {

    @Transactional(readOnly = true)
    fun search(query: String): SearchResponseDTO {
        // 1. Tìm bài hát
        val songs = songRepository.searchSongs(query)
            .map { songService.mapToSongResponseDTO(it) }

        // 2. Tìm playlist công khai
        val playlists = playlistRepository.searchPublicPlaylists(query)
            .map { playlistService.mapToPlaylistSummaryDTO(it) }

        // 3. Gộp lại
        return SearchResponseDTO(
            songs = songs,
            playlists = playlists
        )
    }
}