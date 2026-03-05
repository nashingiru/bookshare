package com.bookshare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Open Library API response DTOs
data class OpenLibrarySearchResponse(
    @SerializedName("numFound") val numFound: Int = 0,
    @SerializedName("docs") val docs: List<BookSearchDoc> = emptyList()
)

data class BookSearchDoc(
    @SerializedName("key") val key: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("author_name") val authorName: List<String>? = null,
    @SerializedName("cover_i") val coverId: Int? = null,
    @SerializedName("isbn") val isbn: List<String>? = null,
    @SerializedName("number_of_pages_median") val pageCount: Int? = null,
    @SerializedName("subject") val subjects: List<String>? = null,
    @SerializedName("language") val languages: List<String>? = null,
    @SerializedName("first_publish_year") val firstPublishYear: Int? = null
) {
    fun getCoverUrl(): String {
        return coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" } ?: ""
    }

    fun getAuthor(): String = authorName?.firstOrNull() ?: "Autor desconocido"

    fun getIsbn(): String = isbn?.firstOrNull() ?: ""
}

data class BookDetailResponse(
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: Any? = null,
    @SerializedName("subjects") val subjects: List<String>? = null
) {
    fun getDescriptionText(): String {
        return when (description) {
            is String -> description
            is Map<*, *> -> (description["value"] as? String) ?: "Sin descripción disponible."
            else -> "Sin descripción disponible."
        }
    }
}
