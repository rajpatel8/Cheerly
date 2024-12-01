package com.rajkumar.cheerly.Podcast

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TeddyService {
    @POST("graphql")
    suspend fun searchPodcasts(
        @Header("x-api-key") apiKey: String,
        @Body query: GraphQLQuery
    ): Response<GraphQLResponse>
}

data class GraphQLQuery(
    val query: String,
    val variables: QueryVariables
)

data class QueryVariables(
    val userId: String,
    val searchQuery: String,
    val category: String? = null,
    val maxDuration: Int? = null,
    val limit: Int = 10
)

data class GraphQLResponse(
    val data: Data?
)

data class Data(
    val searchPodcasts: SearchPodcastsResponse?
)

data class SearchPodcastsResponse(
    val episodes: List<TeddyEpisode>?
)
