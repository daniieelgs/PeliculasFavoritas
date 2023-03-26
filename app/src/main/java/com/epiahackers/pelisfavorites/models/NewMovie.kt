package com.epiahackers.pelisfavorites.models

data class NewMovie (
    var page: Int = 0,
    var results: List<Movie> = emptyList(),
    var total_pages: Int = 0,
    var total_results: Int = 0
)