package com.example.harjoitustyo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LifestyleFragment extends Fragment {
    private final ProfileManager profileManager = ProfileManager.getInstance();
    private TextView flightsEntry, busEntry, trainEntry;
    private Spinner typeSpinner, smokerGroupSpinner;
    private Context context;
    private final LifestyleManager lifestyleManager = LifestyleManager.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lifestyle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        lifestyleManager.queryTHLData(context);
        flightsEntry = view.findViewById(R.id.flights_amount);
        busEntry = view.findViewById(R.id.bus_distance);
        trainEntry = view.findViewById(R.id.train_distance);
        smokerGroupSpinner = view.findViewById(R.id.spinner_group);
        typeSpinner = view.findViewById(R.id.transport_emission_type);
        Button calculate = view.findViewById(R.id.calculate_transport_emissions);
        Button progress = view.findViewById(R.id.show_transport_co2_progress);
        Button showSmokers = view.findViewById(R.id.show_smokers);
        Button yesButton = view.findViewById(R.id.yes_button);
        Button noButton = view.findViewById(R.id.no_button);
        //Fill emission type dropdown menu from string resources
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity(),
                R.array.transport_emissions_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter1);
        //Fill smoker group dropdown menu from string resources
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(),
                R.array.smoker_group_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        smokerGroupSpinner.setAdapter(adapter2);

        //Set all onclick listeners
        flightsEntry.setOnClickListener(v -> {
            selectFlightsAmount();
        });
        busEntry.setOnClickListener(v -> {
            selectDistanceAmount(busEntry);
        });
        trainEntry.setOnClickListener(v -> {
            selectDistanceAmount(trainEntry);
        });
        calculate.setOnClickListener(v -> {
            calculateEmissions();
        });
        progress.setOnClickListener(v -> {
            showProgress();
        });
        noButton.setOnClickListener(v -> {
            Toast.makeText(context, "Keep it up! you earned 1 POINT!", Toast.LENGTH_SHORT).show();
            profileManager.addPoints(1);
            ((MainActivity) getActivity()).saveProfile();
        });
        yesButton.setOnClickListener(v -> {
            Toast.makeText(context, "Remember that smoking has many health risks!", Toast.LENGTH_SHORT).show();
        });
        showSmokers.setOnClickListener(v -> {
            showLocalSmokers();
        });
    }

    //Gets corresponding TextView field as parameter
    //Create a alert dialog for asking travelled kilometers from user.
    public void selectDistanceAmount(TextView field) {
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.km_picker, null);
        EditText picker = linearLayout.findViewById(R.id.km_number_picker);
        AlertDialog builder = new AlertDialog.Builder(context)
                .setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel", null)
                .setView(linearLayout)
                .setCancelable(false)
                .create();
        builder.show();
        builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v1 -> {
            field.setText(picker.getText().toString());
            builder.dismiss();
        });
    }

    //Create a alert dialog for asking amount of one way flights from user.
    public void selectFlightsAmount() {
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.flights_picker, null);
        NumberPicker picker1 = linearLayout.findViewById(R.id.km_number_picker);
        picker1.setMinValue(0);
        picker1.setMaxValue(50);
        picker1.setValue(0);
        picker1.setWrapSelectorWheel(false);
        AlertDialog builder = new AlertDialog.Builder(context)
                .setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel", null)
                .setView(linearLayout)
                .setCancelable(false)
                .create();
        builder.show();
        builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v1 -> {
            int amount = picker1.getValue();
            flightsEntry.setText(String.format("%d", amount));
            builder.dismiss();
        });
    }

    //Gather all user inputs and call calculateTransportEmissions.
    //Set all input fields back to zero.
    public void calculateEmissions() {
        int flights = Integer.parseInt(flightsEntry.getText().toString());
        int train = Integer.parseInt(trainEntry.getText().toString());
        int bus = Integer.parseInt(busEntry.getText().toString());
        lifestyleManager.calculateTransportEmissions(flights, train, bus, context);
        flightsEntry.setText("0");
        trainEntry.setText("0");
        busEntry.setText("0");
    }

    //Get group from dropdown menu and call getSmokerPercentage method.
    //Show feedback string received from getSmokerPercentage method.
    public void showLocalSmokers() {
        String group = smokerGroupSpinner.getSelectedItem().toString();
        String feedback = lifestyleManager.getSmokersPercentage(group);
        Toast.makeText(context, feedback, Toast.LENGTH_LONG).show();
    }

    //Method gets list of users all emission entries and label string as parameters.
    //Create LineChart alert dialog for displaying users co2 emissions progress.
    private void createChart(ArrayList<Entry> entries, String label) {
        RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.chart_layout, null);
        LineChart chart = relativeLayout.findViewById(R.id.line_chart);
        LineDataSet set = new LineDataSet(entries, label);
        set.setFillAlpha(150);
        set.setColor(Color.MAGENTA);
        set.setValueTextSize(15f);
        set.setValueTextColor(Color.BLACK);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);
        LineData data = new LineData(dataSets);
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        chart.setData(data);
        Legend legend = chart.getLegend();
        legend.setTextSize(20f);
        legend.setFormLineWidth(15f);
        AlertDialog builder = new AlertDialog.Builder(context)
                .setNegativeButton("Close", null)
                .setView(relativeLayout)
                .setCancelable(false)
                .create();
        builder.show();
    }

    //Method gets all user emissions from readEmissionsLog method.
    //If data is found, creates ArrayList of entries from log and sends the ArrayList to createChart method.
    private void showProgress() {
        String tag = typeSpinner.getSelectedItem().toString();
        ArrayList<Entry> logEntries = new ArrayList<>();
        JSONArray profileLogs = lifestyleManager.readEmissionsLog(context);
        if (profileLogs == null) {
            Toast.makeText(context, "No previous emission data", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < profileLogs.length(); i++) {
            try {
                JSONObject object = profileLogs.optJSONObject(i);
                String value = object.getJSONObject("Transport data").getString(tag);
                logEntries.add(new Entry(i, Float.parseFloat(value)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        createChart(logEntries, tag + " emission progress");
    }
}
