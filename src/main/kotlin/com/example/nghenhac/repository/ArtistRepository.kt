package com.example.nghenhac.repository

import com.example.nghenhac.models.Artist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ArtistRepository : JpaRepository<Artist, Long> {

    fun findByNameContainingIgnoreCase(name: String): List<Artist>
}