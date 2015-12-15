package com.dhruvb.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ImageView moviePosterImageView = (ImageView)findViewById(R.id.movie_detail_poster_imageview);
        TextView originalTitleTextView = (TextView)findViewById(R.id.movie_detail_title_textview);
        TextView releaseDateTextView = (TextView)findViewById(R.id.movie_detail_release_date_textview);
        TextView userRatingTextView = (TextView)findViewById(R.id.movie_detail_user_rating_textview);
        TextView overviewTextView = (TextView)findViewById(R.id.movie_detail_overview_textview);

//        Picasso.with(this).load(getCompletePosterUrl(imageUrl)).into(moviePosterImageView);

    }
}
