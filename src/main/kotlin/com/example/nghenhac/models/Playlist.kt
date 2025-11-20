package com.example.nghenhac.models

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "playlists")
class Playlist(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "playlist_songs",
        joinColumns = [JoinColumn(name = "playlist_id")],
        inverseJoinColumns = [JoinColumn(name = "song_id")]
    )
    @OrderColumn(name = "display_order")
    val songs: MutableList<Song> = mutableListOf()
) {

    fun addSong(song: Song) {
        this.songs.add(song)
        song.playlists.add(this)
    }

    fun removeSong(song: Song) {
        this.songs.remove(song)
        song.playlists.remove(this)
    }
}