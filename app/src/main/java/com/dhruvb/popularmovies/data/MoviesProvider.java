package com.dhruvb.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by dhruv on 15/1/16.
 */
public class MoviesProvider extends ContentProvider {
    private static final String LOG_TAG = MoviesProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDBHelper mOpenHelper;

    private static final int URI_TYPE_MOVIES = 100;
    private static final int URI_TYPE_MOVIE_ITEM = 101;
    private static final int URI_TYPE_FAVORITES = 200;
    private static final int URI_TYPE_FAVORITES_ITEM = 201;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.MoviesEntry.TABLE_NAME, URI_TYPE_MOVIES);
        matcher.addURI(authority, MoviesContract.MoviesEntry.TABLE_NAME + "/#", URI_TYPE_MOVIE_ITEM);
        matcher.addURI(authority, MoviesContract.FavoritesEntry.TABLE_NAME, URI_TYPE_FAVORITES);
        matcher.addURI(authority, MoviesContract.FavoritesEntry.TABLE_NAME + "/#", URI_TYPE_FAVORITES_ITEM);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_TYPE_MOVIES:
                return MoviesContract.MoviesEntry.CONTENT_DIR_TYPE;
            case URI_TYPE_MOVIE_ITEM:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            case URI_TYPE_FAVORITES:
                return MoviesContract.FavoritesEntry.CONTENT_DIR_TYPE;
            case URI_TYPE_FAVORITES_ITEM:
                return MoviesContract.FavoritesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case URI_TYPE_MOVIES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                return retCursor;
            case URI_TYPE_MOVIE_ITEM:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        MoviesContract.MoviesEntry._ID + " = ?",
                        new String[] { String.valueOf(ContentUris.parseId(uri)) },
                        null,
                        null,
                        sortOrder
                );
                return retCursor;
            case URI_TYPE_FAVORITES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                return retCursor;
            case URI_TYPE_FAVORITES_ITEM:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        MoviesContract.FavoritesEntry._ID + " = ?",
                        new String[] { String.valueOf(ContentUris.parseId(uri)) },
                        null,
                        null,
                        sortOrder
                );
                return retCursor;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri retUri = null;
        switch (sUriMatcher.match(uri)) {
            case URI_TYPE_MOVIES:
                long _id = db.replaceOrThrow(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    retUri = MoviesContract.MoviesEntry.buildMoviesUri(_id);
                }
                break;
            case URI_TYPE_FAVORITES_ITEM:
                _id = db.replaceOrThrow(MoviesContract.FavoritesEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    retUri = MoviesContract.FavoritesEntry.buildMoviesUri(_id);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numDeleted;
        switch (sUriMatcher.match(uri)){
            case URI_TYPE_MOVIES:
                numDeleted = db.delete(
                        MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case URI_TYPE_MOVIE_ITEM:
                numDeleted = db.delete(MoviesContract.MoviesEntry.TABLE_NAME,
                        MoviesContract.MoviesEntry._ID + " = ?",
                        new String[]{ String.valueOf(ContentUris.parseId(uri)) });
                break;
            case URI_TYPE_FAVORITES_ITEM:
                numDeleted = db.delete(MoviesContract.FavoritesEntry.TABLE_NAME,
                        MoviesContract.FavoritesEntry._ID + " = ?",
                        new String[]{ String.valueOf(ContentUris.parseId(uri)) });
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return numDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numUpdated = 0;

        if (values == null){
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch(sUriMatcher.match(uri)){
            case URI_TYPE_MOVIES:
                numUpdated = db.update(MoviesContract.MoviesEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case URI_TYPE_MOVIE_ITEM:
                numUpdated = db.update(MoviesContract.MoviesEntry.TABLE_NAME,
                        values,
                        MoviesContract.MoviesEntry._ID + " = ?",
                        new String[] { String.valueOf(ContentUris.parseId(uri)) });
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch(sUriMatcher.match(uri)){
            case URI_TYPE_MOVIES:
                db.beginTransaction();

                // keep track of successful inserts
                int numInserted = 0;
                try{
                    for(ContentValues value : values){
                        if (value == null){
                            throw new IllegalArgumentException("Cannot have null content values");
                        }
                        long _id = -1;
                        try{
                            _id = db.replaceOrThrow(MoviesContract.MoviesEntry.TABLE_NAME,
                                    null, value);
                        }catch(SQLiteConstraintException e) {
                            Log.w(LOG_TAG, "Attempting to insert " +
                                    value.getAsString(
                                            MoviesContract.MoviesEntry._ID)
                                    + " but value is already in database.");
                        }
                        if (_id != -1){
                            numInserted++;
                        }
                    }
                    if(numInserted > 0){
                        db.setTransactionSuccessful();
                    }
                } finally {
                    db.endTransaction();
                }
                if (numInserted > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return numInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
