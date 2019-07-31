package com.android.priyanka.movieapi.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.priyanka.movieapi.R;
import com.android.priyanka.movieapi.activity.DetailsActivity;
import com.android.priyanka.movieapi.database.MoviesContract;
import com.android.priyanka.movieapi.database.MoviesDBHelper;
import com.android.priyanka.movieapi.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder> {
    Context context;
    private final List<Movie> movieList;

    MoviesDBHelper moviesDbHelper;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    private final OnItemClickListener listener;

    String BASE_PATH = "http://image.tmdb.org/t/p/w185/";


    public MoviesAdapter(Context context, List<Movie> movieList, OnItemClickListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MoviesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        moviesDbHelper= new MoviesDBHelper(parent.getContext());

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_recycler_view_item, parent, false);
        return new MoviesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesViewHolder moviesViewHolder, int i) {
              moviesViewHolder.bind(i,listener);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public class MoviesViewHolder extends RecyclerView.ViewHolder{
        TextView tvMovieName;
        ImageView imgPoster;
        Button favButton;

        public MoviesViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMovieName = (TextView) itemView.findViewById(R.id.tv_moviename);
            imgPoster = (ImageView) itemView.findViewById(R.id.imgPoster);
            favButton = (Button) itemView.findViewById(R.id.favouriteButton);
        }

        public void bind(final int position, final OnItemClickListener listener){
            final Movie movie = movieList.get(position);
            tvMovieName.setText(movie.getTitle());

            String image_path = movie.getPosterPath();
           // Picasso.with(context).load(BASE_PATH + image_path).fit().centerInside().into(imgPoster);
            Picasso.with(itemView.getContext()).load(BASE_PATH + image_path).into(imgPoster);

            final Cursor cursor;
            final SQLiteDatabase db = moviesDbHelper.getReadableDatabase();
            String sql ="SELECT movie_id FROM "+ MoviesContract.MoviesEntry.TABLE_NAME+" WHERE movie_id="+movie.getId();

            cursor= db.rawQuery(sql,null);
            if(cursor.getCount()>=1) {
                favButton.setBackgroundResource(R.drawable.favourite_true);
            }

           itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   listener.onItemClick(position);
                   Intent i = new Intent(itemView.getContext(), DetailsActivity.class);
                   i.putExtra("Movie", movie);
                   itemView.getContext().startActivity(i);
               }
           });


        }
    }


}
