package com.bookshare.app.data.remote.api

import com.bookshare.app.data.remote.dto.BookDetailResponse
import com.bookshare.app.data.remote.dto.OpenLibrarySearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApi {

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "spa"
    ): Response<OpenLibrarySearchResponse>

    @GET("search.json")
    suspend fun searchBooksByTitle(
        @Query("title") title: String,
        @Query("limit") limit: Int = 10
    ): Response<OpenLibrarySearchResponse>

    @GET("search.json")
    suspend fun searchBooksByAuthor(
        @Query("author") author: String,
        @Query("limit") limit: Int = 10
    ): Response<OpenLibrarySearchResponse>

    @GET("works/{workId}.json")
    suspend fun getBookDetail(
        @Path("workId") workId: String
    ): Response<BookDetailResponse>
}
