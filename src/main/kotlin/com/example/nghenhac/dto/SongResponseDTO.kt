package com.example.nghenhac.dto

import java.util.*

data class SongResponseDTO(
    val id: Long,
    val title: String,
    val durationSeconds: Int,
    val artistName: String,
    val coverArtUrl: String?
)