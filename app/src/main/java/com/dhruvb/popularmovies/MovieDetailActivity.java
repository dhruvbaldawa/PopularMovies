package com.dhruvb.popularmovies;

import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.dhruvb.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "detail activity started");
        setContentView(R.layout.activity_movie_detail);

        ImageView moviePosterImageView = (ImageView)findViewById(R.id.movie_detail_poster_imageview);
        TextView originalTitleTextView = (TextView)findViewById(R.id.movie_detail_title_textview);
        TextView releaseDateTextView = (TextView)findViewById(R.id.movie_detail_release_date_textview);
        TextView userRatingTextView = (TextView)findViewById(R.id.movie_detail_user_rating_textview);
        TextView overviewTextView = (TextView)findViewById(R.id.movie_detail_overview_textview);

        Uri movieURI = getIntent().getData();
        Cursor cursor = getContentResolver().query(movieURI, null, null, null, null);

        setTitle(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_TITLE)));
        Resources res = getResources();

        Picasso.with(this)
                .load(MoviesContract.MoviesEntry.getCompleteImageUrl(
                        cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_URL)), "w500"))
                .into(moviePosterImageView);
        originalTitleTextView.setText(
                cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_TITLE)));
        releaseDateTextView.setText(String.format(
                res.getString(R.string.movie_detail_label_release_date),
                cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE))));
        userRatingTextView.setText(String.format(
                res.getString(R.string.movie_detail_label_rating),
                cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_RATING))));
        overviewTextView.setText(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_OVERVIEW)));
        cursor.close();
    }
}
