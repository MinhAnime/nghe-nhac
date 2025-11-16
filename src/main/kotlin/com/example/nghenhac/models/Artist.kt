package com.example.nghenhac.models

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "artists")
data class Artist(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(name = "cover_art_object_name")
    val coverArtObjectName: String? = null,

    @OneToMany(
        mappedBy = "artist",
        cascade = [CascadeType.REMOVE], // Nếu xóa Artist, các bài hát cũng bị xóa
        fetch = FetchType.LAZY
    )
    val songs: List<Song> = emptyList()
)