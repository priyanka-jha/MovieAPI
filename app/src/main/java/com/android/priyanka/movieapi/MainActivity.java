package com.android.priyanka.movieapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.android.priyanka.movieapi.adapter.FavMoviesAdapter;
import com.android.priyanka.movieapi.adapter.MoviesAdapter;
import com.android.priyanka.movieapi.database.MoviesContract;
import com.android.priyanka.movieapi.model.Movie;
import com.android.priyanka.movieapi.model.MoviesList;
import com.android.priyanka.movieapi.rest.APIClient;
import com.android.priyanka.movieapi.rest.APIEndPoint;
import com.google.gson.Gson;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    APIClient apiClient;
    APIEndPoint apiEndPoint;
    @BindView(R.id.spinner_toolbar)
    Spinner spinnerToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rvMovies)
    RecyclerView rvMovies;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    RecyclerView.LayoutManager layoutManager;

    int numberOfColumns = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private FavMoviesAdapter favMoviesAdapter;
    public static final int MOVIE_LOADER_ID = 0;

    int flag=-1;
    static int count;
    int currentVisiblePosition;
    int SpinnerPosition;
    // Spinner spinner;
    private final String Spinner_Position_String = "spinner_position", Scroll_Position_String = "scroll_position",
            SharedPrefPositions = "sharedPrefPositions";
    Bundle savedInstance = null;
    SharedPreferences sharedPref;

    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);  //to hide title from toolbar
        }

        layoutManager = new GridLayoutManager(this, numberOfColumns);
        rvMovies.setLayoutManager(layoutManager);

        favMoviesAdapter = new FavMoviesAdapter(this, new FavMoviesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }
        });

        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);

        Context context = getApplicationContext();
        sharedPref = context.getSharedPreferences(
                SharedPrefPositions, Context.MODE_PRIVATE);


        editor = sharedPref.edit();

    }

    @Override
    protected void onPause() {
        super.onPause();
        flag=1;
        currentVisiblePosition = ((GridLayoutManager)rvMovies.getLayoutManager()).findFirstVisibleItemPosition();

        editor.putInt(Spinner_Position_String, SpinnerPosition);
        editor.putInt(Scroll_Position_String, currentVisiblePosition);
        Log.d(TAG, "onPause :"+currentVisiblePosition + "FLAG :"  + flag);
        editor.apply();


    }

    @Override
    protected void onResume() {
        super.onResume();

        SpinnerPosition = sharedPref.getInt(Spinner_Position_String, 0);
        currentVisiblePosition = sharedPref.getInt(Scroll_Position_String, 0);
        layoutManager = new GridLayoutManager(this, numberOfColumns);
        int pScroll = sharedPref.getInt(Scroll_Position_String, 0);

        rvMovies.setLayoutManager(layoutManager);
        rvMovies.scrollToPosition(pScroll);

        favMoviesAdapter = new FavMoviesAdapter(this, new FavMoviesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }
        });

      //  setToolbarSpinner();

        ArrayAdapter<CharSequence> spinArrayAadapter = ArrayAdapter.createFromResource(this,
                R.array.toolbar_array_spinner, android.R.layout.simple_list_item_1);
        spinArrayAadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToolbar.setAdapter(spinArrayAadapter);
        count=0;

        spinnerToolbar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("selected spinner item.."+parent.getSelectedItem());
                System.out.println("selected spinner position.."+parent.getSelectedItemPosition());

                int sPosition = sharedPref.getInt(Spinner_Position_String, 0);
                int pScroll = sharedPref.getInt(Scroll_Position_String, 0);

                SpinnerPosition = parent.getSelectedItemPosition();

                if(flag==1) { //orientation change
                    SpinnerPosition = sPosition;
                    currentVisiblePosition = pScroll;
                }else{
                    currentVisiblePosition = 0;
                }

                editor.putInt(Spinner_Position_String, SpinnerPosition);
                editor.putInt(Scroll_Position_String, currentVisiblePosition);
                editor.apply();

                loadClasses();

                /*Hack to avoid the duplicate call to OnItemSelected*/
                count++;
                Log.d(TAG, "count "+count);
                if(count>1 && SpinnerPosition!=0) {
                    flag=-1;
                    count=0;
                }else if(SpinnerPosition==0)
                    flag =-1;



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);

    }

    public void loadPopularMovie() {
        apiEndPoint = APIClient.getClient().create(APIEndPoint.class);

        Call<MoviesList> moviesListCall = apiEndPoint.getPopularMovies(BuildConfig.API_KEY);
        moviesListCall.enqueue(new Callback<MoviesList>() {
            @Override
            public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {
                System.out.println("response.." + response.body().getMovies());
                System.out.println("json response.." + new Gson().toJson(response.body()));

                List<Movie> movieList = response.body().getMovies();
                progressBar.setVisibility(View.GONE);
                rvMovies.setAdapter(new MoviesAdapter(getApplicationContext(), movieList, new MoviesAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {

                    }
                }));

               /* ArrayList<Movie> movieArrayList = (ArrayList<Movie>) response.body().getMovies();
                for (int i = 0; i < movieArrayList.size(); i++) {
                    System.out.println("movie.." + i + movieArrayList.get(i).getTitle());
                }*/
            }

            @Override
            public void onFailure(Call<MoviesList> call, Throwable t) {
                Log.d("Error", t.getMessage());
                setContentView(R.layout.layout_no_network);
            }
        });

    }

    public void loadTopRatedMovies() {
        apiEndPoint = APIClient.getClient().create(APIEndPoint.class);

        Call<MoviesList> moviesListCall = apiEndPoint.getTopRatedMovies(BuildConfig.API_KEY);
        moviesListCall.enqueue(new Callback<MoviesList>() {
            @Override
            public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {
                System.out.println("response.." + response.body().getMovies());
                System.out.println("json response.." + new Gson().toJson(response.body()));

                List<Movie> movieList = response.body().getMovies();
                progressBar.setVisibility(View.GONE);

                rvMovies.setAdapter(new MoviesAdapter(getApplicationContext(), movieList, new MoviesAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {

                    }
                }));

               /* ArrayList<Movie> movieArrayList = (ArrayList<Movie>) response.body().getMovies();
                for (int i = 0; i < movieArrayList.size(); i++) {
                    System.out.println("movie.." + i + movieArrayList.get(i).getTitle());
                }*/
            }

            @Override
            public void onFailure(Call<MoviesList> call, Throwable t) {
                Log.d("Error", t.getMessage());
                setContentView(R.layout.layout_no_network);
            }
        });

    }

    public void loadFavouriteMovies() {
        progressBar.setVisibility(View.GONE);

        rvMovies.setAdapter(favMoviesAdapter);
        Log.d(TAG, "loadFavMovies " + currentVisiblePosition);
        rvMovies.scrollToPosition(currentVisiblePosition);

    }




    public void setToolbarSpinner() {

        ArrayAdapter<CharSequence> spinArrayAadapter = ArrayAdapter.createFromResource(this,
                R.array.toolbar_array_spinner, android.R.layout.simple_list_item_1);
        spinArrayAadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToolbar.setAdapter(spinArrayAadapter);
        count=0;

        spinnerToolbar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("selected spinner item.."+parent.getSelectedItem());
                System.out.println("selected spinner position.."+parent.getSelectedItemPosition());

                int sPosition = sharedPref.getInt(Spinner_Position_String, 0);
                int pScroll = sharedPref.getInt(Scroll_Position_String, 0);

                 SpinnerPosition = parent.getSelectedItemPosition();

                if(flag==1) { //orientation change
                    SpinnerPosition = sPosition;
                    currentVisiblePosition = pScroll;
                }else{
                    currentVisiblePosition = 0;
                }

                editor.putInt(Spinner_Position_String, SpinnerPosition);
                editor.putInt(Scroll_Position_String, currentVisiblePosition);
                editor.apply();

                loadClasses();

                /*Hack to avoid the duplicate call to OnItemSelected*/
                count++;
                Log.d(TAG, "count "+count);
                if(count>1 && SpinnerPosition!=0) {
                    flag=-1;
                    count=0;
                }else if(SpinnerPosition==0)
                    flag =-1;



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "OnRestore flag "+flag);
        if (savedInstanceState != null) {
            savedInstance = savedInstanceState;
            flag=1; //orientation changed
        }else{
            Log.d(TAG, "null instance");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        currentVisiblePosition = ((GridLayoutManager)rvMovies.getLayoutManager()).findFirstVisibleItemPosition();

        outState.putInt(Spinner_Position_String, SpinnerPosition);
        outState.putInt(Scroll_Position_String, currentVisiblePosition);
        Log.d(TAG, "OnSave flag: " + flag + " var-> " + currentVisiblePosition);

        editor.putInt(Spinner_Position_String, SpinnerPosition);
        editor.putInt(Scroll_Position_String, currentVisiblePosition);
        editor.commit();
    }

    private void loadClasses() {
        SpinnerPosition = sharedPref.getInt(Spinner_Position_String, 0);
        Log.d(TAG, "loadClasses for " + SpinnerPosition +"  var:" + currentVisiblePosition);
        spinnerToolbar.setSelection(SpinnerPosition);

        spinnerToolbar.setSelection(SpinnerPosition);
        if (SpinnerPosition==0){
            progressBar.setVisibility(View.VISIBLE);
            loadPopularMovie();
        }
        else if(SpinnerPosition==1){
            progressBar.setVisibility(View.VISIBLE);
            loadTopRatedMovies();
        }
        else if(SpinnerPosition==2){
            progressBar.setVisibility(View.VISIBLE);
            loadFavouriteMovies();
        }



    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {

        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mTaskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mTaskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data

                // Query and load all task data in the background; sort by priority
                // [Hint] use a try/catch block to catch any errors in loading data

                try {
                    return getContentResolver().query(MoviesContract.MoviesEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            MoviesContract.MoviesEntry._ID);

                } catch (Exception e) {
                    Log.e("TAG", "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        favMoviesAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        favMoviesAdapter.swapCursor(null);
    }
}
