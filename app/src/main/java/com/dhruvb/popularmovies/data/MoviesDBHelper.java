package com.dhruvb.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
                MoviesContract.MoviesEntry.TABLE_MOVIES + "(" +
                MoviesContract.MoviesEntry._ID + " INTEGER PRIMARY KEY, " +
                MoviesContract.MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL" +
                MoviesContract.MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL" +
                MoviesContract.MoviesEntry.COLUMN_RATING + " TEXT NOT NULL" +
                MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL" +
                MoviesContract.MoviesEntry.COLUMN_POSTER_URL + " TEXT NOT NULL" +
                MoviesContract.MoviesEntry.COLUMN_HAS_VIDEO + " INTEGER DEFAULT 0" +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MoviesEntry.TABLE_MOVIES + ";");
        sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                MoviesContract.MoviesEntry.TABLE_MOVIES + "';");
        onCreate(sqLiteDatabase);
    }
}
