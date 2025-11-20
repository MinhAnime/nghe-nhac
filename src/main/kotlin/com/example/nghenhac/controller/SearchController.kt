package com.example.nghenhac.controller

import com.example.nghenhac.dto.SearchResponseDTO
import com.example.nghenhac.service.SearchService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/search")
class SearchController(
    private val searchService: SearchService
) {


    @GetMapping
    fun search(
        @RequestParam("q") query: String
    ): ResponseEntity<SearchResponseDTO> {
        // Gọi service để tìm cả bài hát và playlist
        val results = searchService.search(query)

        return ResponseEntity.ok(results)
    }
}