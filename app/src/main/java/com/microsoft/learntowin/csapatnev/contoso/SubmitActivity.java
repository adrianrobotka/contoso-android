package com.microsoft.learntowin.csapatnev.contoso;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SubmitActivity extends Activity {
    // Prefix (rather tag) for the logging
    private static final String LOGTAG = SubmitActivity.class.getSimpleName();

    private ProgressDialog detectionProgressDialog;

    /*
     * UI references
     */
    private Button menuButton;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        registerUIReferences();
        registerCallbacks();

        Registration registration = Registration.getSavedInstance();

        if (registration == null) {
            Log.e(LOGTAG, "No registration");
            finish();
        }

        RegisterTask task = new RegisterTask(registration);
        task.execute();
    }

    private void registerCallbacks() {
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Because sometimes even UI need care...
     */
    private void registerUIReferences() {
        menuButton = (Button) findViewById(R.id.menuButton);
        statusText = (TextView) findViewById(R.id.statusText);
        detectionProgressDialog = new ProgressDialog(this);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate the user.
     */
    private class RegisterTask extends AsyncTask<Void, Void, Boolean> {

        private Registration registration;

        public RegisterTask(Registration registration) {
            this.registration = registration;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.d(LOGTAG, "Start registration");

            return UserInterface.register(registration);
        }

        @Override
        protected void onPreExecute() {
            detectionProgressDialog.setMessage(getString(R.string.registrationWithDots));
            detectionProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean successfulRegistration) {
            detectionProgressDialog.dismiss();

            if (successfulRegistration) {
                statusText.setText(getString(R.string.registrationSuccessful));
                statusText.setVisibility(View.VISIBLE);
            } else {
                statusText.setText(getString(R.string.registrationFailed));
                statusText.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onCancelled() {
            detectionProgressDialog.dismiss();
            Log.d(LOGTAG, "Registration cancelled");
        }
    }
}
