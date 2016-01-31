package com.dhruvb.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dhruv on 1/24/16.
 */
public class MovieUtilities {
    public static String getSortOrder(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(context.getString(R.string.pref_sort_by_key),
                context.getString(R.string.pref_sort_by_value_most_popular));
    }

    public static boolean isFavoritesEnabled(Context context) {
        String sortOrder = getSortOrder(context);
        return sortOrder.contentEquals(context.getString(R.string.pref_sort_by_value_favorites));
    }

    public static String formatMovieDetailDate(String date) {
        Date dateObj = null;
        try {
            dateObj = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (dateObj != null) {
            return new SimpleDateFormat("yyyy").format(dateObj);
        } else {
            return null;
        }
    }
}
