package com.microsoft.learntowin.csapatnev.contoso;

import android.graphics.Bitmap;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Contains data of the registration process
 */
final class Registration {
    private static final String LOGTAG = Registration.class.getSimpleName();
    private static Registration savedInstance = null;
    private static ArrayList<Bitmap> photos;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String birth;
    private final String company;
    private final String jobTitle;
    private Bitmap candidatePhoto;

    Registration(String firstName, String lastName, String email, String birth, String company, String jobTitle) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birth = birth;
        this.company = company;
        this.jobTitle = jobTitle;
        photos = new ArrayList<>();
    }

    public static Registration getSavedInstance() {
        return savedInstance;
    }

    public static boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    boolean isFirstNameValid() {
        return firstName.length() >= 3 && checkExtendedAlphanumeric(firstName);
    }

    boolean isLastNameValid() {
        return lastName.length() >= 3 && checkExtendedAlphanumeric(lastName);
    }

    boolean isEmailValid() {
        return isEmailValid(email);
    }

    boolean isBirthValid() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date date = sdf.parse(birth);
            Date now = new Date();

            long nowValue = now.getTime();
            long dateValue = date.getTime();

            return nowValue >= dateValue;
        } catch (ParseException e) {
            Log.e(LOGTAG, "Cannot parse date", e);
            return false;
        }
    }

    boolean isCompanyValid() {
        return company.length() >= 3 && checkExtendedAlphanumeric(company);
    }

    boolean isJobTitleValid() {
        return jobTitle.length() >= 3 && checkExtendedAlphanumeric(jobTitle);
    }

    /**
     * Returns false if the text contains not valid characters
     *
     * @param text Text to validate
     * @return Text in valid format
     */
    private boolean checkExtendedAlphanumeric(String text) {
        return !(text.contains("*") || text.contains("\"") || text.contains(";") || text.contains("#"));
    }

    void save() {
        savedInstance = this;
    }

    public ArrayList<Bitmap> getPhotos() {
        return photos;
    }

    public void finalizeCandidatePhoto() {
        photos.add(candidatePhoto);
        candidatePhoto = null;
    }

    public Bitmap getCandidatePhoto() {
        return candidatePhoto;
    }

    public void setCandidatePhoto(Bitmap candidatePhoto) {
        this.candidatePhoto = candidatePhoto;
    }

    String getFirstName() {
        return firstName;
    }

    String getLastName() {
        return lastName;
    }

    String getEmail() {
        return email;
    }

    String getBirth() {
        return birth;
    }

    String getCompany() {
        return company;
    }

    String getJobTitle() {
        return jobTitle;
    }

    public boolean isValid() {
        return photos.size() >= Config.REQUIRED_IMAGES;
    }
}
