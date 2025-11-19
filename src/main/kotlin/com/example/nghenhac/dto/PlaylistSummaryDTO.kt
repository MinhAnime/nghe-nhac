package com.example.nghenhac.dto

data class PlaylistSummaryDTO(
    val id: Long,
    val name: String,
    val ownerUsername: String,
    val thumbnails: List<String>
)