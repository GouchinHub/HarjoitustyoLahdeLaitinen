package com.example.harjoitustyo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE = 1;
    private TextView dobEdit;
    private TextView profileAge;
    private TextView heightEdit;
    private TextView weightEdit;
    private TextView profileBMI;
    private TextView points;
    private EditText emailEdit, residenceEdit;
    private ImageView profilePicture;
    private ToggleButton toggleButton;
    private LinearLayout itemsLayout;
    private Context context;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Button save;
    private Button cancel;
    private final ProfileManager profileManager = ProfileManager.getInstance();
    private final LoginManager loginManager = LoginManager.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        itemsLayout = view.findViewById(R.id.editable_items);
        emailEdit = view.findViewById(R.id.edit_email);
        residenceEdit = view.findViewById(R.id.edit_residence);
        dobEdit = view.findViewById(R.id.edit_date);
        heightEdit = view.findViewById(R.id.edit_height);
        weightEdit = view.findViewById(R.id.edit_weight);
        profileAge = view.findViewById(R.id.user_age_view);
        profilePicture = view.findViewById(R.id.profile_picture);
        profileBMI = view.findViewById(R.id.User_bmi_view);
        points = view.findViewById(R.id.user_points);
        toggleButton = view.findViewById(R.id.profileEditButton);
        save = view.findViewById(R.id.save_changes_button);
        cancel = view.findViewById(R.id.cancel_changes_button);
        Button changePic = view.findViewById(R.id.edit_profile_pic_button);
        //Display username in profile
        TextView profileNameView = view.findViewById(R.id.profile_name);
        profileNameView.setText(profileManager.getActiveUserName());
        //Load the active profile data to the profile page and set profile picture if found in user data
        loadProfile();
        if (isAdded() && context != null){
            if (profileManager.getActiveProfile().getPictureUri() != null) {
                Uri imageUri = Uri.parse(profileManager.getActiveProfile().getPictureUri());
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                    profilePicture.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editProfile();
            } else {
                //cancels current changes in profile
                disableEditingMode();
                loadProfile();
            }
        });

        //listener for date selected by user.
        dateSetListener = (view1, year, month, dayOfMonth) -> {
            month++;
            LocalDate now = LocalDate.now();
            if (now.isBefore(LocalDate.of(year, month, dayOfMonth))) {
                Toast.makeText(context, "Choose a valid date!", Toast.LENGTH_SHORT).show();
            } else {
                LocalDate newDoB = LocalDate.of(year, month, dayOfMonth);
                dobEdit.setText(formatter.format(newDoB));
            }
        };
        save.setOnClickListener(v -> {
            saveProfileChanges();
        });
        cancel.setOnClickListener(v -> {
            setEditCheckedOFF();
        });
        changePic.setOnClickListener(v -> {
            changeProfilePic();
        });
    }

    //lets user choose a picture from phones gallery
    public void changeProfilePic() {
        try {
            Intent gallery = new Intent();
            gallery.setType("image/*");
            gallery.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
        } catch (IllegalStateException | NullPointerException e){
            e.printStackTrace();
        }
    }

    public void deleteGuest() {
        profileManager.deleteGuestData(context);
    }

    //Get new profile picture from phones files and save it to device using scoped storage.
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            FileOutputStream fos;
            try {
                //Save the user chosen picture into scoped storage under the name "HappyHealthPictures"
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "image_" + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "HappyHealthPictures");
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = (FileOutputStream) resolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Objects.requireNonNull(fos);
                //set new bitmap as profile picture and save image Uri to profile data.
                profilePicture.setImageBitmap(bitmap);
                profileManager.saveProfilePic(imageUri.toString());
                ((MainActivity) getActivity()).updateNavPicture();
                ((MainActivity) getActivity()).saveProfile();
                loadProfile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setEditCheckedOFF() {
        toggleButton.setChecked(false);
    }

    //Method set fields in profile view to editable and adds onclick listeners.
    public void editProfile() {
        itemsLayout.setBackground(ContextCompat.getDrawable(context, R.color.secondary_background));
        cancel.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);

        if (!loginManager.getActiveUser().isGuestFlag()) {
            emailEdit.setInputType(InputType.TYPE_CLASS_TEXT);
            emailEdit.setTextColor(getResources().getColor(R.color.secondary_pink));
            emailEdit.setEnabled(true);
        }

        residenceEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        residenceEdit.setTextColor(getResources().getColor(R.color.secondary_pink));
        residenceEdit.setEnabled(true);

        //oped DatePickerDialog to choose date of birth
        dobEdit.setTextColor(getResources().getColor(R.color.secondary_pink));
        dobEdit.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(
                    context,
                    R.style.DialogTheme,
                    dateSetListener,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            dialog.show();
        });

        heightEdit.setTextColor(getResources().getColor(R.color.secondary_pink));
        heightEdit.setOnClickListener(v -> {
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.cm_picker, null);
            NumberPicker picker = linearLayout.findViewById(R.id.numberPicker);
            picker.setMinValue(0);
            picker.setMaxValue(300);
            picker.setValue(180);
            AlertDialog builder = new AlertDialog.Builder(context)
                    .setPositiveButton("Confirm", null)
                    .setNegativeButton("Cancel", null)
                    .setView(linearLayout)
                    .setCancelable(false)
                    .create();
            builder.show();
            builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                int newHeight = picker.getValue();
                heightEdit.setText(Integer.toString(newHeight));
                builder.dismiss();
            });
        });

        weightEdit.setTextColor(getResources().getColor(R.color.secondary_pink));
        weightEdit.setOnClickListener(v -> {
            RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.kg_picker, null);
            NumberPicker picker1 = relativeLayout.findViewById(R.id.kg_number_picker1);
            picker1.setMinValue(0);
            picker1.setMaxValue(300);
            picker1.setValue(70);
            NumberPicker picker2 = relativeLayout.findViewById(R.id.kg_number_picker2);
            picker2.setMinValue(0);
            picker2.setMaxValue(9);
            picker2.setValue(0);
            picker2.setWrapSelectorWheel(true);
            AlertDialog builder = new AlertDialog.Builder(context)
                    .setPositiveButton("Confirm", null)
                    .setNegativeButton("Cancel", null)
                    .setView(relativeLayout)
                    .setCancelable(false)
                    .create();
            builder.show();
            builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                Double newWeight = Double.parseDouble(picker1.getValue() + "." + picker2.getValue());
                weightEdit.setText(Double.toString(newWeight));
                builder.dismiss();
            });
        });
    }

    private double calculateBMI() {
        double height = Double.parseDouble(heightEdit.getText().toString());
        double weight = Double.parseDouble(weightEdit.getText().toString());
        double BMI = weight / (height / 100) / (height / 100);
        return BMI;
    }

    //Disables all editable fields in profile and changes their color and background back to default.
    private void disableEditingMode() {
        itemsLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.grad4));

        emailEdit.setInputType(InputType.TYPE_NULL);
        emailEdit.setTextColor(getResources().getColor(R.color.black));
        emailEdit.setEnabled(false);

        residenceEdit.setInputType(InputType.TYPE_NULL);
        residenceEdit.setTextColor(getResources().getColor(R.color.black));
        residenceEdit.setEnabled(false);

        dobEdit.setTextColor(getResources().getColor(R.color.black));
        dobEdit.setOnClickListener(null);

        heightEdit.setTextColor(getResources().getColor(R.color.black));
        heightEdit.setOnClickListener(null);

        weightEdit.setTextColor(getResources().getColor(R.color.black));
        weightEdit.setOnClickListener(null);
        setEditCheckedOFF();
    }


    //Method calls profile manager for saving profile changes to log and user data.
    public void saveProfileChanges() {
        double newBMI = calculateBMI();
        DecimalFormat df = new DecimalFormat("#,###,##0.00");
        profileBMI.setText(df.format(newBMI));
        cancel.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);
        profileManager.createLogJson(emailEdit.getText().toString(),
                residenceEdit.getText().toString(),
                dobEdit.getText().toString(),
                heightEdit.getText().toString(),
                weightEdit.getText().toString(),
                profileBMI.getText().toString(),
                context
        );
        profileManager.saveProfileInformation(
                emailEdit.getText().toString(),
                residenceEdit.getText().toString(),
                LocalDate.parse(dobEdit.getText().toString(), formatter),
                Integer.parseInt(heightEdit.getText().toString()),
                Double.parseDouble(weightEdit.getText().toString()),
                newBMI
        );
        ((MainActivity) requireActivity()).saveProfile();
        disableEditingMode();
    }

    //Load all profile information in the profile view
    private void loadProfile() {
        Profile profile = profileManager.getActiveProfile();
        profileAge.setText(String.format("%d", profile.getAge()));
        DecimalFormat df = new DecimalFormat("#,###,##0.00");
        profileBMI.setText(df.format(profile.getBMI()));
        emailEdit.setText(profile.getEmail());
        residenceEdit.setText(profile.getResidence());
        dobEdit.setText(profile.getBirthday());
        heightEdit.setText(String.format("%d", profile.getHeight()));
        df = new DecimalFormat("#,###,##0.0");
        weightEdit.setText(df.format(profile.getWeight()));
        points.setText(String.format("%d", profile.getPoints()));
    }

}
