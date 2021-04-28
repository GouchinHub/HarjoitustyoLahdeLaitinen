package com.example.harjoitustyo;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class FitnessFragment extends Fragment {
    private Context context;
    private static final ProfileManager profileManager = ProfileManager.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fitness, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        LineChart weightChart = view.findViewById(R.id.weight_chart);
        LineChart heightChart = view.findViewById(R.id.height_chart);
        LineChart bmiChart = view.findViewById(R.id.bmi_chart);
        //Fill all charts with corresponding data
        showProgress("BMI", "BMI progress", bmiChart);
        showProgress("Weight", "Weight progress (in kilograms)", weightChart);
        showProgress("Height", "Height progress (in centimeters)", heightChart);
        super.onViewCreated(view, savedInstanceState);
    }

    //method gets list of data entries, label, and LineChart. Fills given chart with given entries and given label.
    private void fillChart(ArrayList<Entry> entries, String label, LineChart chart) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setFillAlpha(150);
        set.setColor(Color.MAGENTA);
        set.setValueTextSize(15f);
        set.setValueTextColor(Color.BLACK);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);
        LineData data = new LineData(dataSets);
        //remove description tag from chart
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        chart.setData(data);
        Legend legend = chart.getLegend();
        legend.setTextSize(20f);
        legend.setFormLineWidth(15f);
    }

    //Method gets tag string and LineChart with label.
    //Searches profile changes from log. If found, creates array of entries form log with values on given tag. Then calls method fillChart.
    public void showProgress(String tag, String label, LineChart chart) {
        ArrayList<Entry> logEntries = new ArrayList<>();
        JSONArray profileChanges = profileManager.readProfileChangesLog(context);
        if (profileChanges == null) {
            return;
        }
        for (int i = 0; i < profileChanges.length(); i++) {
            try {
                JSONObject object = profileChanges.optJSONObject(i);
                String value = object.getJSONObject("Profile changes").getString(tag);
                logEntries.add(new Entry(i, Float.parseFloat(value)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        fillChart(logEntries, label, chart);
    }

}
