package com.dhruvb.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by dhruv on 15/1/16.
 */
public class MoviesDBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = MoviesDBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.v(LOG_TAG, "creating database tables");
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
                MoviesContract.MoviesEntry.TABLE_NAME + "(" +
                MoviesContract.MoviesEntry._ID + " INTEGER PRIMARY KEY, " +
                MoviesContract.MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_RATING + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_BACKDROP_URL + " TEXT NOT NULL, " +
                ");";

        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " +
                MoviesContract.FavoritesEntry.TABLE_NAME + "(" +
                MoviesContract.FavoritesEntry._ID + " INTEGER PRIMARY KEY, " +
                MoviesContract.FavoritesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesContract.FavoritesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesContract.FavoritesEntry.COLUMN_RATING + " TEXT NOT NULL, " +
                MoviesContract.FavoritesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MoviesContract.FavoritesEntry.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                MoviesContract.FavoritesEntry.COLUMN_BACKDROP_URL + " TEXT NOT NULL, " +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MoviesEntry.TABLE_NAME + ";");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.FavoritesEntry.TABLE_NAME + ";");
        onCreate(sqLiteDatabase);
    }
}
