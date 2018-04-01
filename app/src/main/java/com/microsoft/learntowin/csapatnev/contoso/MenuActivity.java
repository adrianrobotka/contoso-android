package com.microsoft.learntowin.csapatnev.contoso;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Menu of the app, default activity too
 */
public class MenuActivity extends Activity {
    // Prefix (rather tag) for the logging
    private static final String LOGTAG = MenuActivity.class.getSimpleName();

    // Helper class to reduce confusing code in the activity
    private final ActivityHelper helper = new ActivityHelper(this);

    private ConnectionCheckTask task = null;

    private boolean blockStart = true;

    /*
     * UI references
     */
    private Button menuRegistrationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Help debugging
        Log.d(LOGTAG, "onCreate() called");

        getActivityViews();

        menuRegistrationButton.setEnabled(false);

        setButtonListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        task = new ConnectionCheckTask();
        task.execute();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    /**
     * Get (interactive) Views of the activity
     */
    private void getActivityViews() {
        menuRegistrationButton = (Button) findViewById(R.id.menuRegistrationButton);
    }

    /**
     * Set OnClickListener for the Views
     */
    private void setButtonListeners() {

        /*
         * Because even buttons need care
         */
        menuRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blockStart && isNetworkConnected()) {
                    task = new ConnectionCheckTask();
                    task.execute();
                    return;
                }

                if (blockStart) {
                    Toast.makeText(getApplicationContext(), R.string.withoutInternet, Toast.LENGTH_LONG).show();
                    return;
                }

                helper.startActivity(RegistrationActivity.class);
            }
        });
    }

    private class ConnectionCheckTask extends AsyncTask<Void, String, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (!isNetworkConnected()) {
                    Log.e(LOGTAG, "No internet connection");
                    return false;
                }

                return true; // no more required check

            } catch (Exception e) {
                Log.e(LOGTAG, "Cannot check email -> no internet connection", e);
            }

            return false;
        }

        @Override
        protected void onPreExecute() {
            menuRegistrationButton.setEnabled(false);
        }

        @Override
        protected void onPostExecute(Boolean thereIsConnection) {
            blockStart = !thereIsConnection;

            if (!thereIsConnection) {
                Toast.makeText(getApplicationContext(), R.string.noInternet, Toast.LENGTH_LONG).show();
            } else {
                Log.d(LOGTAG, "Have internet connection.");

            }

            menuRegistrationButton.setEnabled(true);
        }
    }
}
