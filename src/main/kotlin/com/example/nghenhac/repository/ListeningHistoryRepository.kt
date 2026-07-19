package com.example.nghenhac.repository

import com.example.nghenhac.models.ListeningHistory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ListeningHistoryRepository : JpaRepository<ListeningHistory, Long> {

    @Query("""
        SELECT lh FROM ListeningHistory lh 
        JOIN FETCH lh.song s 
        JOIN FETCH s.artist 
        WHERE lh.user.id = :userId 
        ORDER BY lh.playedAt DESC
    """)
    fun findByUserIdOrderByPlayedAtDesc(@Param("userId") userId: Long, pageable: Pageable): Page<ListeningHistory>
}
