package com.example.harjoitustyo;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ProfileManager {

    private final LoginManager loginManager = LoginManager.getInstance();
    private Profile activeProfile;

    private static final ProfileManager profileManager = new ProfileManager();

    private ProfileManager() {
        getActiveProfile();
    }

    public static ProfileManager getInstance() {
        return profileManager;
    }

    public Profile getActiveProfile() {
        activeProfile = loginManager.getActiveUser().getProfile();
        return activeProfile;
    }

    public String getActiveUserName() {
        return loginManager.getActiveUser().getName();
    }

    public void saveProfilePic(String newPictureUri) {
        activeProfile.setPictureUri(newPictureUri);
        loginManager.saveUserProfileChanges(activeProfile);
    }

    //Method updates users profile points with new values
    public void addPoints(int newPoints){
        int points = activeProfile.getPoints();
        points += newPoints;
        activeProfile.setPoints(points);
        loginManager.saveUserProfileChanges(activeProfile);
    }

    //Delete temporary files used to store guest profile data.
    public void deleteGuestData(Context context){
        context.deleteFile(activeProfile.getName()+"Profile.json");
        context.deleteFile(activeProfile.getName()+"Transport.json");
        context.deleteFile(activeProfile.getName()+"Nutrition.json");
    }

    //Method receives all profile changes as parameter.
    //Updates all profile changes to active profile
    public void saveProfileInformation(String email, String residence, LocalDate date, int height, double weight, double BMI) {
        activeProfile = getActiveProfile();
        activeProfile.setEmail(email);
        activeProfile.setResidence(residence);
        activeProfile.setBirthday(date);
        activeProfile.setHeight(height);
        activeProfile.setWeight(weight);
        activeProfile.setBMI(BMI);
        loginManager.saveUserProfileChanges(activeProfile);
    }

    //Reads profile information from json file in application files.
    //Creates Json object from the received data and forms and return json array.
    public JSONArray readProfileChangesLog(Context context){
        JSONArray profileChanges = new JSONArray();
        try {
            InputStream inputStream = context.openFileInput(profileManager.getActiveUserName()+"Profile.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String string = "";
            if((string = br.readLine()) != null) {
                JSONArray jsonObject = new JSONArray(string);
                profileChanges = jsonObject;
            }
            inputStream.close();
            return profileChanges;
        } catch (JSONException | IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Method gets all profile changes as parameters and creates a json object to be logged.
    public void createLogJson(String email, String residence, String birthday, String height, String weight, String bmi ,Context context){
        JSONObject profileChanges = new JSONObject();
        try {
            profileChanges.put("Email",email );
            profileChanges.put("Residence",residence );
            profileChanges.put("Birthday",birthday );
            profileChanges.put("Height",height);
            profileChanges.put("Weight",weight);
            profileChanges.put("BMI", bmi);
            logProfileChanges(profileChanges,context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Method gets json object containing profile changes as parameter and creates a json file in application files.
    //Gets old profile data as json array from readProfileChangesLog method and adds new data into json array
    //Writes new json array into the created json file.
    private void logProfileChanges(JSONObject object, Context context){
        try {
            JSONArray jsonArray = readProfileChangesLog(context);
            if(jsonArray == null){
                jsonArray = new JSONArray();
            }
            FileOutputStream fos = context.openFileOutput(profileManager.getActiveUserName()+"Profile.json", Context.MODE_PRIVATE);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Profile changes",object);
            jsonArray.put(jsonObject);
            fos.write(jsonArray.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            Log.e("IOExpection", "Error occurred writing file ");
        }
    }

}

