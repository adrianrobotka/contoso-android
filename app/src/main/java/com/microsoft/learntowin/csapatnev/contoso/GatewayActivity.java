package com.microsoft.learntowin.csapatnev.contoso;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GatewayActivity extends Activity {
    // Prefix (rather tag) for the logging
    private static final String LOGTAG = SubmitActivity.class.getSimpleName();

    //A prefilled Registration is needed to this activity
    private Registration registration;

    /*
     * UI references
     */
    private Button takePictureButton;
    private Button choosePictureButton;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway);

        registerUIReferences();
        registerCallbacks();

        registration = Registration.getSavedInstance();
        if (registration == null) {
            Log.e(LOGTAG, "Missing preRegistration");
            //registration = new Registration("Lajos", "Kovács", "lali@gmail.com", "1968-06-07", "Manó Kft.", "Pap");
            //registration.save();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (registration.getPhotos().size() >= Config.REQUIRED_IMAGES) {
            Intent intent = new Intent(getBaseContext(), SubmitActivity.class);
            startActivity(intent);
        } else {
            statusText.setText(registration.getPhotos().size() + "/" + Config.REQUIRED_IMAGES + " " + getString(R.string.photosSelected));
        }
    }

    private void registerCallbacks() {
        choosePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), GalleryActivity.class);
                startActivity(intent);
            }
        });

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), CameraActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Because sometimes even UI need care...
     */
    private void registerUIReferences() {
        statusText = (TextView) findViewById(R.id.statusText);
        takePictureButton = (Button) findViewById(R.id.takePictureButton);
        choosePictureButton = (Button) findViewById(R.id.choosePictureButton);
    }
}