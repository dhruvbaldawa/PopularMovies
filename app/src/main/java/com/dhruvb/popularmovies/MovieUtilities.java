package com.dhruvb.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by dhruv on 1/24/16.
 */
public class MovieUtilities {
    public static String getSortOrder(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = settings.getString(context.getString(R.string.pref_sort_by_key),
                context.getString(R.string.pref_sort_by_value_most_popular));
        return sortOrder;
    }

    public static boolean isFavoritesEnabled(Context context) {
        String sortOrder = getSortOrder(context);
        if (sortOrder.contentEquals(context.getString(R.string.pref_sort_by_value_favorites))) {
            return true;
        }
        return false;
    }
}
