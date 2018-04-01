package com.microsoft.learntowin.csapatnev.contoso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraActivity extends Activity {
    // Android constants
    private static final int REQUEST_TAKE_PHOTO = 1;

    // Prefix (rather tag) for the logging
    private static final String LOGTAG = CameraActivity.class.getSimpleName();

    // Path of the taken photo
    private String photoPath;

    private ProgressDialog detectionProgressDialog;

    //A prefilled Registration is needed to this activity
    private Registration registration;

    // To provide error messages connected with face detection
    private String statusMessage;

    /*
     * UI references
     */
    private Button confirmButton;
    private Button backButton;
    private TextView confirmStatusTextView;
    private ImageView pictureImageView;

    public CameraActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        registration = Registration.getSavedInstance();
        if (registration == null) {
            Log.e(LOGTAG, "Missing preRegistration");
            //registration = new Registration("Lajos", "Kovács", "lali@gmail.com", "1968-06-07", "Manó Kft.", "Pap");
            //registration.save();
            finish();
        }

        registerUIReferences();
        registerCallbacks();

        /*
         * tryToLoadPicture();
         */
        if (registration != null && registration.getCandidatePhoto() != null) {
            updateView();
        } else {
            dispatchTakePictureIntent();
        }
    }

    /**
     * Because sometimes even UI need care...
     */
    private void registerUIReferences() {
        confirmButton = (Button) findViewById(R.id.confirmButton);
        backButton = (Button) findViewById(R.id.confirmBackButton);
        confirmStatusTextView = (TextView) findViewById(R.id.confirmStatusTextView);
        pictureImageView = (ImageView) findViewById(R.id.pictureImageView);

        detectionProgressDialog = new ProgressDialog(this);
    }

    private void registerCallbacks() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registration.setCandidatePhoto(null);
                registration.save();
                finish();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registration.finalizeCandidatePhoto();
                registration.save();
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * To save the taken photo we should create a temp file
     *
     * @return Temp file
     * @throws IOException You know...
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        photoPath = image.getAbsolutePath();
        return image;
    }


    /**
     * Start camera and write the taken image to a temp file
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            try {
                photoFile = createImageFile();

                // Continue only if the File was successfully created
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.microsoft.learntowin.csapatnev.contoso.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            } catch (Exception e) {
                // Error occurred while creating the File
                Log.e(LOGTAG, "Cannot take picture", e);
            }
        }
    }


    /**
     * After user choose the picture, we change the view to confirm that image
     */
    private void updateView() {
        // showing the photo is take too much time, do it later (after API response)
        new FaceDetectionTask().execute();

        // Show previously taken image
        pictureImageView.setImageBitmap(registration.getCandidatePhoto());
        pictureImageView.setVisibility(View.VISIBLE);
    }

    private void showConfirmButton() {
        confirmStatusTextView.setVisibility(View.GONE);
        confirmButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOGTAG, "Picture saved");

        File imageFile = new File(photoPath);
        Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        registration.setCandidatePhoto(myBitmap);
        registration.save();

        // Update the view with the picture
        updateView();
    }


    // !! If you change this class change in the GalleryActivity too
    private class FaceDetectionTask extends AsyncTask<Void, String, Face[]> {
        @SuppressLint("DefaultLocale")
        @Override
        protected Face[] doInBackground(Void... params) {
            try {
                publishProgress(getString(R.string.detecting));
                Face[] result = UserInterface.detect(registration.getCandidatePhoto());
                if (result == null) {
                    publishProgress("Detection Finished. Nothing detected");

                    return null;
                }

                publishProgress(String.format("Detection Finished. %d face(s) detected", result.length));

                return result;
            } catch (Exception e) {
                Log.e(LOGTAG, "Detection failed:", e);
                publishProgress(getString(R.string.detectionFailed));
                statusMessage = getString(R.string.faceDetectionFailed);
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            detectionProgressDialog.setMessage("...");
            detectionProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            Log.i(LOGTAG, "onProgressUpdate: " + progress[0]);
            detectionProgressDialog.setMessage(progress[0]);
        }

        @Override
        protected void onPostExecute(Face[] faces) {
            detectionProgressDialog.dismiss();

            if (faces == null || faces.length == 0) {
                Log.i(LOGTAG, "Face[] faces: null");
                statusMessage = getString(R.string.noFacesDetected);
            } else {
                Log.i(LOGTAG, "Successful face detection");

                Bitmap bitmap = registration.getCandidatePhoto().copy(Bitmap.Config.ARGB_8888, true);

                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.RED);
                final int stokeWidth = 5;
                paint.setStrokeWidth(stokeWidth);

                for (Face face : faces) {
                    FaceRectangle faceRectangle = face.faceRectangle;
                    canvas.drawRect(
                            faceRectangle.left,
                            faceRectangle.top,
                            faceRectangle.left + faceRectangle.width,
                            faceRectangle.top + faceRectangle.height,
                            paint);

                    pictureImageView.setImageBitmap(bitmap);
                }

                if (faces.length != 1) {
                    statusMessage = getString(R.string.oneFaceAllowed);
                }
            }

            if (statusMessage != null) {
                confirmStatusTextView.setText(statusMessage);
                confirmStatusTextView.setVisibility(View.VISIBLE);
                return;
            }

            showConfirmButton();
        }
    }
}