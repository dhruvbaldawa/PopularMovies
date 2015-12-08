package com.dhruvb.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by dhruv on 12/9/15.
 */
public class MovieAdapter extends ArrayAdapter<String> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Activity context) {
        super(context, 0);
    }

    private static String getCompletePosterUrl(String posterUrl) {
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String WIDTH = "w185";
        return BASE_URL + WIDTH + posterUrl;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String imageUrl = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie, parent, false);
        }

        ImageView posterImageView = (ImageView)convertView.findViewById(R.id.list_item_movie_imageview);
        Picasso.with(getContext()).load(getCompletePosterUrl(imageUrl)).into(posterImageView);
        return convertView;
    }
}
