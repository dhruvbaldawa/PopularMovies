package com.dhruvb.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by dhruv on 12/9/15.
 */
public class MovieAdapter extends ArrayAdapter<MovieInfo> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Activity context, List<MovieInfo> movieInfo) {
        super(context, 0, movieInfo);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieInfo movieInfo = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie, parent, false);
        }

        ImageView posterImageView = (ImageView)convertView.findViewById(R.id.list_item_movie_imageview);
        Picasso.with(getContext()).load(MovieInfo.getCompleteImageUrl(movieInfo.originalPosterUrl, "w342"))
                .into(posterImageView);
        return convertView;
    }
}
