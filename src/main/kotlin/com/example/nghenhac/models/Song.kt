package com.example.nghenhac.models

import jakarta.persistence.*
import java.util.*


@Entity
@Table(name = "songs")
class Song(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var title: String,

    @Column(name = "duration_seconds", nullable = false)
    var durationSeconds: Int,

    @Column(name = "song_object_name", nullable = false, unique = true)
    val songObjectName: String,


    @Column(name = "cover_art_object_name")
    val coverArtObjectName: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    val artist: Artist,


    @ManyToMany(
        mappedBy = "songs",
        fetch = FetchType.LAZY
    )
    val playlists: MutableSet<Playlist> = mutableSetOf()
)
