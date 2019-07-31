package com.android.priyanka.movieapi.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.priyanka.movieapi.BuildConfig;
import com.android.priyanka.movieapi.R;
import com.android.priyanka.movieapi.adapter.ReviewAdapter;
import com.android.priyanka.movieapi.adapter.TrailersAdapter;
import com.android.priyanka.movieapi.database.MoviesContract;
import com.android.priyanka.movieapi.database.MoviesDBHelper;
import com.android.priyanka.movieapi.model.Movie;
import com.android.priyanka.movieapi.model.Review;
import com.android.priyanka.movieapi.model.ReviewsList;
import com.android.priyanka.movieapi.model.Video;
import com.android.priyanka.movieapi.model.VideosList;
import com.android.priyanka.movieapi.rest.APIClient;
import com.android.priyanka.movieapi.rest.APIEndPoint;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.android.priyanka.movieapi.database.MoviesContract.MoviesEntry.CONTENT_URI;

public class DetailsActivity extends AppCompatActivity {

    @BindView(R.id.imgBackground)
    ImageView imgBackground;
    @BindView(R.id.favouriteButton)
    Button favouriteButton;
    @BindView(R.id.tvRating)
    TextView tvRating;
    @BindView(R.id.tvOrigTitle)
    TextView tvOrigTitle;
    @BindView(R.id.tvSynopsis)
    TextView tvSynopsis;
    @BindView(R.id.tvRelease)
    TextView tvRelease;
    @BindView(R.id.layoutTrailers)
    LinearLayout layoutTrailers;
    @BindView(R.id.rvTrailers)
    RecyclerView trailersRecyclerView;
    @BindView(R.id.rvReviews)
    RecyclerView reviewsRecyclerView;

    APIEndPoint apiEndPoint;
    String BASE_PATH = "http://image.tmdb.org/t/p/w342/";
    MoviesDBHelper moviesDBHelper;
    RecyclerView.LayoutManager trailersLayoutManager, reviewsLayoutManager;

    Movie movie;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        moviesDBHelper = new MoviesDBHelper(this);
        trailersLayoutManager = new LinearLayoutManager(this);
        trailersRecyclerView.setLayoutManager(trailersLayoutManager);
        reviewsLayoutManager = new LinearLayoutManager(this);
        reviewsRecyclerView.setLayoutManager(reviewsLayoutManager);
        intent = this.getIntent();

        if (intent != null) {
            movie = intent.getParcelableExtra("Movie");
        }
        loadMovieDetail();
    }


    public void loadMovieDetail() {
        if (checkCountCursor() >= 1) {
            favouriteButton.setBackgroundResource(R.drawable.favourite_true);
        }
        String path = movie.getBackdropPath();
        Picasso.with(getApplicationContext()).load(BASE_PATH + path).into(imgBackground);
        imgBackground.setScaleType(ImageView.ScaleType.FIT_XY);
        tvOrigTitle.setText(movie.getOriginalTitle());
        tvSynopsis.setText(movie.getOverview());
        tvRating.setVisibility(View.VISIBLE);
        tvRating.setText(String.valueOf(movie.getVoteAverage()));
        tvRelease.setText(getString(R.string.release_date) + movie.getReleaseDate());
        loadTrailers(movie.getId().longValue());
        loadReviews(movie.getId().longValue());


    }

    private void loadReviews(long id) {
        apiEndPoint = APIClient.getClient().create(APIEndPoint.class);

        Call<ReviewsList> reviewsListCall = apiEndPoint.getMovieReviews(id,BuildConfig.API_KEY);
        reviewsListCall.enqueue(new Callback<ReviewsList>() {
            @Override
            public void onResponse(Call<ReviewsList> call, Response<ReviewsList> response) {
                List<Review> reviews = response.body().getReviews();
                reviewsRecyclerView.setAdapter(new ReviewAdapter(reviews));
            }

            @Override
            public void onFailure(Call<ReviewsList> call, Throwable t) {
                Log.d("Error", t.getMessage());
                setContentView(R.layout.layout_no_network);
            }
        });
    }

    private void loadTrailers(long id) {
        apiEndPoint = APIClient.getClient().create(APIEndPoint.class);

        Call<VideosList> videosListCall = apiEndPoint.getMovieVideos(id, BuildConfig.API_KEY);
        videosListCall.enqueue(new Callback<VideosList>() {
            @Override
            public void onResponse(Call<VideosList> call, Response<VideosList> response) {
                List<Video> videos = response.body().getVideos();
                trailersRecyclerView.setAdapter(new TrailersAdapter(videos, new TrailersAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {

                    }
                }));
            }

            @Override
            public void onFailure(Call<VideosList> call, Throwable t) {
                Log.d("Error", t.getMessage());
                setContentView(R.layout.layout_no_network);
            }
        });

    }

    public int checkCountCursor() {
        final Cursor cursor;
        final SQLiteDatabase db = moviesDBHelper.getReadableDatabase();
        String sql = "SELECT movie_id FROM " + MoviesContract.MoviesEntry.TABLE_NAME + " WHERE movie_id=" + movie.getId();

        cursor = db.rawQuery(sql, null);
        return cursor.getCount();
    }


    @OnClick({R.id.favouriteButton, R.id.layoutTrailers})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.favouriteButton:
                favouriteMovie();
                break;
            case R.id.layoutTrailers:
                if(trailersRecyclerView.getVisibility()==View.VISIBLE) {
                    trailersRecyclerView.setVisibility(View.GONE);
                }
                else {
                    trailersRecyclerView.setVisibility(View.VISIBLE);
                }
                break;
        }

        }

        public void favouriteMovie() {

            if(checkCountCursor()<1){
                ContentValues contentValues = new ContentValues();
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_ID, movie.getId());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE, movie.getTitle());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_POSTERPATH, movie.getPosterPath());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_SYNOPSIS, movie.getOverview());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_USER_RATING, movie.getVoteAverage());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE, movie.getReleaseDate());

                Uri uri = getContentResolver().insert(CONTENT_URI, contentValues);
                if (uri != null) {
                    favouriteButton.setBackgroundResource(R.drawable.favourite_true);
                } else
                    Log.d("TAG", "uri null");

            }else{
                String id = movie.getId().toString();
                Uri uri = CONTENT_URI;
                uri = uri.buildUpon().appendPath(id).build();
                int returnUri = getContentResolver().delete(uri, null, null);
                Log.d("TAG", returnUri+"");
                getContentResolver().notifyChange(uri, null);
                favouriteButton.setBackgroundResource(R.drawable.favourite_false);
            }
        }
    }

