package com.example.nghenhac.dto

import java.util.*

data class SongUploadDTO(
    val title: String,
    val durationSeconds: Int,
    val artistId: Long
)