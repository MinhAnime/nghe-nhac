package com.example.nghenhac.dto

data class PlaylistDetailDTO(
    val id: Long,
    val name: String,
    val ownerUsername: String,
    val songs: List<SongResponseDTO>
)
