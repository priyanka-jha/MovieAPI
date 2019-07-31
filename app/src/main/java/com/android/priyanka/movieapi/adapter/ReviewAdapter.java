package com.android.priyanka.movieapi.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.priyanka.movieapi.R;
import com.android.priyanka.movieapi.model.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }


    private final List<Review> reviews;
    //   private final OnItemClickListener listener;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviews_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position);

    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvReviews;
        TextView tvAuthor;

        public ViewHolder(View itemView) {
            super(itemView);
            tvReviews = (TextView)itemView.findViewById(R.id.tv_reviews);
            tvAuthor = (TextView)itemView.findViewById(R.id.author);
        }

        public void bind(final int position){
            String reviewsContent = reviews.get(position).getContent();
            String reviewsAuthor = reviews.get(position).getAuthor();
            tvReviews.setText(reviewsContent);
            tvAuthor.setText(reviewsAuthor + " says:");

        }
    }
}
