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

import java.text.DecimalFormat;
import java.util.ArrayList;

public class NutritionFragment extends Fragment {
    //Daily water recommendation amount rounded from data found at
    //https://www.mayoclinic.org/healthy-lifestyle/nutrition-and-healthy-eating/in-depth/water/art-20044256
    private static final int DAILY_WATER_RECOMMENDATION = 3;
    private TextView meatEntry, dairyEntry, plantEntry;
    private Spinner dietSpinner, typeSpinner;
    private Context context;
    private final NutritionManager nutritionManager = NutritionManager.getInstance();
    private final ProfileManager profileManager = ProfileManager.getInstance();
    private final DecimalFormat df = new DecimalFormat("#,###,##0.0");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nutrition, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        dietSpinner = view.findViewById(R.id.spinner_diet);
        typeSpinner = view.findViewById(R.id.spinner_emission_type);
        meatEntry = view.findViewById(R.id.meat_amount);
        dairyEntry = view.findViewById(R.id.dairy_amount);
        plantEntry = view.findViewById(R.id.plant_amount);
        Button calculate = view.findViewById(R.id.calculate_food_emissions);
        Button progress = view.findViewById(R.id.show_food_co2_progress);
        Button waterButton = view.findViewById(R.id.water_button);
        //Fill diet dropdown menu
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.diet_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(adapter);
        //Fill emission type dropdown menu
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(),
                R.array.food_emissions_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter2);
        //Set onclick listeners
        meatEntry.setOnClickListener(v -> {
            selectEntryAmount(meatEntry);
        });
        dairyEntry.setOnClickListener(v -> {
            selectEntryAmount(dairyEntry);
        });
        plantEntry.setOnClickListener(v -> {
            selectEntryAmount(plantEntry);
        });
        calculate.setOnClickListener(v -> {
            calculateEmissions();
        });
        progress.setOnClickListener(v -> {
            showProgress();
        });
        waterButton.setOnClickListener(v -> {
            waterEntry();
        });
    }

    //Method sets up AlertDialog for asking users daily water consumptions
    //If user input exceeds daily recommendation, 10 points are added to user profile.
    public void waterEntry() {
        RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.water_picker, null);
        NumberPicker picker1 = relativeLayout.findViewById(R.id.water_picker1);
        picker1.setMinValue(0);
        picker1.setMaxValue(9);
        picker1.setValue(0);
        picker1.setWrapSelectorWheel(false);
        NumberPicker picker2 = relativeLayout.findViewById(R.id.water_picker2);
        picker2.setMinValue(0);
        picker2.setMaxValue(9);
        picker2.setValue(0);
        AlertDialog builder = new AlertDialog.Builder(context)
                .setPositiveButton("Add Entry", null)
                .setNegativeButton("Cancel", null)
                .setView(relativeLayout)
                .setCancelable(false)
                .create();
        builder.show();
        builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v1 -> {
            double waterAmount = Double.parseDouble(picker1.getValue() + "." + picker2.getValue());
            if (waterAmount >= DAILY_WATER_RECOMMENDATION) {
                Toast.makeText(context, "Congratulations! you have exceeded the " + DAILY_WATER_RECOMMENDATION +
                        " liter recommendation, you earned 10 POINTS!", Toast.LENGTH_LONG).show();
                profileManager.addPoints(10);
                ((MainActivity) getActivity()).saveProfile();
            } else {
                Toast.makeText(context, "You still have " + df.format(DAILY_WATER_RECOMMENDATION - waterAmount) +
                        " liters left for daily " + DAILY_WATER_RECOMMENDATION + " liter recommendation", Toast.LENGTH_LONG).show();
            }
            builder.dismiss();
        });
    }

    //Method creates alert dialog for asking user for amount in kilograms using number pickers
    public void selectEntryAmount(TextView field) {
        RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.kg_picker, null);
        NumberPicker picker1 = relativeLayout.findViewById(R.id.kg_number_picker1);
        picker1.setMinValue(0);
        picker1.setMaxValue(4);
        picker1.setValue(0);
        picker1.setWrapSelectorWheel(false);
        NumberPicker picker2 = relativeLayout.findViewById(R.id.kg_number_picker2);
        picker2.setMinValue(0);
        picker2.setMaxValue(9);
        picker2.setValue(0);
        AlertDialog builder = new AlertDialog.Builder(context)
                .setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel", null)
                .setView(relativeLayout)
                .setCancelable(false)
                .create();
        builder.show();
        builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v1 -> {
            double kgAmount = Double.parseDouble(picker1.getValue() + "." + picker2.getValue());
            field.setText(df.format(kgAmount));
            builder.dismiss();
        });
    }

    //Method gathers data from user input fields and calls calculateFoodEmissions method with the data as parameters
    public void calculateEmissions() {
        String diet = dietSpinner.getSelectedItem().toString();
        double meat = Double.parseDouble(meatEntry.getText().toString());
        double dairy = Double.parseDouble(dairyEntry.getText().toString());
        double plant = Double.parseDouble(plantEntry.getText().toString());
        nutritionManager.calculateFoodEmissions(diet, meat, dairy, plant, context);
        meatEntry.setText("0");
        dairyEntry.setText("0");
        plantEntry.setText("0");
    }

    //Method gets list of users emission entries and label string as parameters.
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
        //erase description tag
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
        JSONArray profileLogs = nutritionManager.readEmissionsLog(context);
        if (profileLogs == null) {
            Toast.makeText(context, "No previous emission data", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < profileLogs.length(); i++) {
            try {
                JSONObject object = profileLogs.optJSONObject(i);
                String value = object.getJSONObject("Nutrition data").getString(tag);
                logEntries.add(new Entry(i, Float.parseFloat(value)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        createChart(logEntries, tag + " emission progress");
    }
}
