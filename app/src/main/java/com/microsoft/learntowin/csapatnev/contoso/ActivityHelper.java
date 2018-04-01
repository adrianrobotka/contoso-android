package com.microsoft.learntowin.csapatnev.contoso;

import android.app.Activity;
import android.content.Intent;

/**
 * Helper methods for activities
 */
public final class ActivityHelper {
    private Activity activity;

    /**
     * Create a helper for a given activity
     *
     * @param activity Use helper for this activity
     */
    public ActivityHelper(Activity activity) {
        this.activity = activity;
    }

    /**
     * Start a new activity
     *
     * @param activityClass Activity class to start
     */
    public void startActivity(Class activityClass) {
        Intent intent = new Intent(activity, activityClass);
        activity.startActivity(intent);
    }
}
