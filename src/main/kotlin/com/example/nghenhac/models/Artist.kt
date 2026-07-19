package com.example.nghenhac.models

import jakarta.persistence.*

@Entity
@Table(name = "artists")
class Artist(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(name = "cover_art_object_name")
    val coverArtObjectName: String? = null,

    @OneToMany(
        mappedBy = "artist",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY
    )
    val songs: List<Song> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Artist) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Artist(id=$id, name='$name', coverArtObjectName=$coverArtObjectName)"
    }
}