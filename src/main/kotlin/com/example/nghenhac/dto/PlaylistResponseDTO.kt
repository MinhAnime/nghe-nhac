package com.example.nghenhac.dto

import java.util.*

data class PlaylistResponseDTO (
    val id: Long,
    val name: String,
    val ownerUsername: String,
    val songs: List<SongResponseDTO>
)