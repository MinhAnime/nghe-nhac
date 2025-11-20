package com.example.nghenhac.dto

data class SearchResponseDTO(
    val songs: List<SongResponseDTO> = emptyList(),
    val playlists: List<PlaylistSummaryDTO> = emptyList()
)