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

    // Tên của object file nhạc (.mp3) trên MinIO, phải là duy nhất
    @Column(name = "song_object_name", nullable = false, unique = true)
    val songObjectName: String,

    // Tên của object ảnh bìa (cover) trên MinIO
    @Column(name = "cover_art_object_name")
    val coverArtObjectName: String? = null,

    // Mối quan hệ: Nhiều Song thuộc về một Artist
    @ManyToOne(fetch = FetchType.LAZY) // Chỉ tải Artist khi thực sự cần
    @JoinColumn(name = "artist_id", nullable = false) // Tên cột khóa ngoại
    val artist: Artist,

    // Mối quan hệ: Nhiều Song có thể nằm trong nhiều Playlist
    @ManyToMany(
        mappedBy = "songs", // "songs" là tên field trong class Playlist
        fetch = FetchType.LAZY
    )
    val playlists: MutableSet<Playlist> = mutableSetOf()
)
