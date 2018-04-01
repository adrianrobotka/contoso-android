package com.microsoft.learntowin.csapatnev.contoso;

import android.graphics.Bitmap;
import android.util.Log;

import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.rest.ClientException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Interface to send login/register request
 */
public class UserInterface {
    private static final String LOGTAG = UserInterface.class.getSimpleName();

    /**
     * JSON HTTP MIME type
     */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    static boolean isEmailRegistered(String email) throws Exception {
        OkHttpClient client = new OkHttpClient();
        String url = Config.API_URL + "/participant/checkEmail/" + email + "?apikey=" + Config.API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();

        String body = response.body().string();
        Log.d(LOGTAG, "Got response: " + body);

        JSONObject json = new JSONObject(body);

        return json.getBoolean("isRegistered");
    }

    static boolean register(Registration registration) {
        if (!registration.isValid()) {
            Log.e(LOGTAG, "Not valid registration");
            return false;
        }

        try {
            OkHttpClient client = new OkHttpClient();
            String url = Config.API_URL + "/participant";
            String personId;

            JSONObject requestJson = new JSONObject()
                    .put("apikey", Config.API_KEY)
                    .put("firstName", registration.getFirstName())
                    .put("lastName", registration.getLastName())
                    .put("email", registration.getEmail())
                    .put("birth", registration.getBirth())
                    .put("company", registration.getCompany())
                    .put("workTitle", registration.getJobTitle());

            RequestBody body = RequestBody.create(JSON, requestJson.toString());
            Request dataRegisterRequest = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response dataResponse = client.newCall(dataRegisterRequest).execute();
            String dataResponseBody = dataResponse.body().string();
            Log.d(LOGTAG, "Get API response: "
                    + dataResponse.toString()
                    + "\n"
                    + dataResponseBody);
            if (dataResponse.code() != 200) {
                Log.e(LOGTAG, "Cannot register user (data-request). Response code: " + dataResponse.code());
                return false;
            }

            JSONObject responseJson = new JSONObject(dataResponseBody);

            personId = responseJson.getString("personId");

            if (personId.length() < 20)
                return false;

            for (Bitmap bitmap : registration.getPhotos()) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

                try {
                    final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");

                    MultipartBody.Builder builder = new MultipartBody.Builder();
                    builder.setType(MultipartBody.FORM);
                    builder.addFormDataPart("apikey", Config.API_KEY);
                    builder.addFormDataPart("image", "face.png", RequestBody.create(MEDIA_TYPE_JPG, outputStream.toByteArray()));
                    MultipartBody req = builder.build();

                    Request faceRegisterRequest = new Request.Builder()
                            .url(Config.API_URL + "/participant/" + personId + "/image")
                            .post(req)
                            .build();

                    Response faceResponse = client.newCall(faceRegisterRequest).execute();

                    if (faceResponse.code() != 200) {
                        Log.e(LOGTAG, "Cannot register face. Response code: " + faceResponse.code());
                        return false;
                    }
                } catch (Exception e) {
                    Log.e(LOGTAG, "Exception during request", e);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            Log.d(LOGTAG, "Error while get user API access token", e);
        }

        return false;
    }

    static Face[] detect(Bitmap bitmap) throws IOException, ClientException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

        try {
            final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");

            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            builder.addFormDataPart("apikey", Config.API_KEY);
            builder.addFormDataPart("image", "face.png", RequestBody.create(MEDIA_TYPE_JPG, outputStream.toByteArray()));
            MultipartBody req = builder.build();

            Request request = new Request.Builder()
                    .url(Config.API_URL + "/detect")
                    .post(req)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();

            String body = response.body().string();

            if (response.code() != 200) {
                Log.e(LOGTAG, "Skip face detect response processing. Response code: " + response.code());

                return new Face[]{};
            }
            Log.d(LOGTAG, "Got response:" + body);

            JSONArray json = new JSONObject(body).getJSONArray("results");

            if (json.length() == 0)
                return new Face[]{};

            Face[] faces = new Face[json.length()];

            for (int i = 0; i < json.length(); i++) {
                JSONObject faceJson = json.getJSONObject(i);
                faces[i] = new Face();
                faces[i].faceId = UUID.fromString(faceJson.getString("faceId"));
                JSONObject rectJson = faceJson.getJSONObject("faceRectangle");
                FaceRectangle rect = new FaceRectangle();
                rect.top = rectJson.getInt("top");
                rect.left = rectJson.getInt("left");
                rect.width = rectJson.getInt("width");
                rect.height = rectJson.getInt("height");
                faces[i].faceRectangle = rect;
            }

            return faces;

        } catch (Exception e) {
            Log.e(LOGTAG, "Exception during request", e);
        }

        return new Face[]{};
    }
}