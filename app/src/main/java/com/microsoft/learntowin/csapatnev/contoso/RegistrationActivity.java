package com.microsoft.learntowin.csapatnev.contoso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public final class RegistrationActivity extends Activity {
    // Prefix (rather tag) for the logging
    private static final String LOGTAG = RegistrationActivity.class.getSimpleName();

    // Helper class to reduce confusing code in the activity
    private final ActivityHelper helper = new ActivityHelper(this);

    /*
     * UI references
     */
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText birthEditText;
    private EditText companyEditText;
    private EditText jobTitleEditText;
    private TextView eulaTextView;
    private Button registerButton;

    private boolean isEmailValidated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        registerUIReferences();

        registerCallbacks();

        isEmailValidated = false;
    }

    private void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        newFragment.show(ft, "datePicker");
    }


    private void registerCallbacks() {
        birthEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkConnected()) {
                    attemptRegister();
                } else {
                    Toast.makeText(getBaseContext(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                }
            }
        });

        eulaTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.EULA_URL));
                startActivity(browser);
            }
        });

        emailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean getFocus) {
                if (getFocus) {
                    return;
                }

                validateEmail();
            }
        });
    }

    private void validateEmail() {
        String email = emailEditText.getText().toString();

        isEmailValidated = Registration.isEmailValid(email);

        if (!isEmailValidated) {
            emailEditText.setError(getString(R.string.invalidEmail));
            return;
        }

        new EmailCheckTask(email).execute();
    }

    /**
     * Because sometimes even UI need care...
     */
    private void registerUIReferences() {
        firstNameEditText = (EditText) findViewById(R.id.registerFirstNameEditText);
        lastNameEditText = (EditText) findViewById(R.id.registerLastNameEditText);
        emailEditText = (EditText) findViewById(R.id.registerEmailEditText);
        birthEditText = (EditText) findViewById(R.id.registerBirthEditText);
        companyEditText = (EditText) findViewById(R.id.registerCompanyEditText);
        jobTitleEditText = (EditText) findViewById(R.id.registerJobTitleEditText);
        eulaTextView = (TextView) findViewById(R.id.eulaTextView);
        registerButton = (Button) findViewById(R.id.registerButton);
    }

    /**
     * Attempts to sign in or register the account specified by the register form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    private void attemptRegister() {
        // Reset errors.
        firstNameEditText.setError(null);
        lastNameEditText.setError(null);
        emailEditText.setError(null);
        birthEditText.setError(null);
        companyEditText.setError(null);
        jobTitleEditText.setError(null);

        // Store values at the time of the login attempt.
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String birth = birthEditText.getText().toString();
        String company = companyEditText.getText().toString();
        String jobTitle = jobTitleEditText.getText().toString();

        View focusView = null;

        Registration registration = new Registration(firstName, lastName, email, birth, company, jobTitle);

        if (!registration.isJobTitleValid()) {
            jobTitleEditText.setError(getString(R.string.invalidJobTitle));
            focusView = jobTitleEditText;
        }

        if (!registration.isCompanyValid()) {
            companyEditText.setError(getString(R.string.invalidCompany));
            focusView = companyEditText;
        }

        if (!registration.isBirthValid()) {
            birthEditText.setError(getString(R.string.invalidBirth));
            focusView = birthEditText;
        }

        if (!registration.isEmailValid()) {
            emailEditText.setError(getString(R.string.invalidEmail));
            focusView = emailEditText;
        }

        if (!registration.isLastNameValid()) {
            lastNameEditText.setError(getString(R.string.invalidName));
            focusView = lastNameEditText;
        }

        if (!registration.isFirstNameValid()) {
            firstNameEditText.setError(getString(R.string.invalidName));
            focusView = firstNameEditText;
        }

        if (focusView != null) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        }

        if (!isEmailValidated) {
            validateEmail();
            return;
        }

        // Save user data and continue the registration
        registration.save();
        helper.startActivity(GatewayActivity.class);

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @SuppressLint("DefaultLocale")
        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            birthEditText.setText(year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day));
        }
    }

    private class EmailCheckTask extends AsyncTask<Void, String, Boolean> {
        private String email;
        private boolean error = false;

        public EmailCheckTask(String email) {
            this.email = email;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return UserInterface.isEmailRegistered(email);
            } catch (Exception e) {
                Log.e(LOGTAG, "Cannot check email", e);
                error = true;
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Boolean isRegistered) {
            if (error)
                return;

            if (isRegistered) {
                emailEditText.setError(getString(R.string.registeredEmail));
                emailEditText.requestFocus();
            }
        }
    }
}
