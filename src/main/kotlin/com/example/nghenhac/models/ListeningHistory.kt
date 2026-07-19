package com.example.nghenhac.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "listening_history")
class ListeningHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    val song: Song,

    @Column(name = "played_at", nullable = false)
    val playedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ListeningHistory) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ListeningHistory(id=$id, playedAt=$playedAt)"
    }
}
