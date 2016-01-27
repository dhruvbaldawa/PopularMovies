package com.dhruvb.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.dhruvb.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

/**
 * Created by dhruv on 12/9/15.
 */
public class MovieAdapter extends CursorAdapter {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView posterImageView = (ImageView)view.findViewById(R.id.list_item_movie_imageview);
        String posterPath = cursor.getString(
                cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_URL));
        Picasso.with(context)
                .load(MoviesContract.MoviesEntry.getCompleteImageUrl(posterPath, "w500"))
                .placeholder(R.drawable.image)
                .error(R.drawable.image_broken)
                .into(posterImageView);
    }
}
