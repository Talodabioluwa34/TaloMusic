package com.talodabi.talomusic.data.model

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int = 0,
    val artworkUri: String? = null,
    val year: Int = 0
)

data class Artist(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
    val albumCount: Int = 0
)

data class Genre(
    val id: Long,
    val name: String,
    val songCount: Int = 0
)

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
