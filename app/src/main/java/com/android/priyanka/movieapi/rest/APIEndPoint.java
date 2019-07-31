package com.android.priyanka.movieapi.rest;

import com.android.priyanka.movieapi.model.MoviesList;
import com.android.priyanka.movieapi.model.ReviewsList;
import com.android.priyanka.movieapi.model.VideosList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIEndPoint {
    @GET("movie/popular")
    Call<MoviesList> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/top_rated")
    Call<MoviesList> getTopRatedMovies(@Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<VideosList> getMovieVideos(@Path("id") long movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ReviewsList> getMovieReviews(@Path("id") long movieId, @Query("api_key") String apiKey);


}
