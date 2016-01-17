package com.dhruvb.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dhruv on 15/1/16.
 */
public class MoviesContract {
    public static final String CONTENT_AUTHORITY = "com.dhruvb.popularmovies.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class MoviesEntry implements BaseColumns {
        public static final String TABLE_MOVIES = "movies";

        public static final String _ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_URL = "poster_path";
        public static final String COLUMN_BACKDROP_URL = "backdrop_path";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_HAS_VIDEO = "has_video";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_MOVIES).build();
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + TABLE_MOVIES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + TABLE_MOVIES;

        public static Uri buildMoviesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getCompleteImageUrl(String imageUrl, String size) {
            final String BASE_URL = "http://image.tmdb.org/t/p/";
            switch (size) {
                case "w92":
                case "w154":
                case "w185":
                case "w342":
                case "w500":
                case "w780":
                case "original":
                    return BASE_URL + size + imageUrl;
            }
            return null;  // @TODO maybe raise an exception instead
        }

    }
}
