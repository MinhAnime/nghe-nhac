package com.example.nghenhac.models

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @OneToMany(
        mappedBy = "owner",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY
    )
    val playlists: List<Playlist> = emptyList()
) {

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_liked_songs",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "song_id")]
    )
    val likedSongs: MutableSet<Song> = mutableSetOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "User(id=$id, username='$username', email='$email')"
    }
}