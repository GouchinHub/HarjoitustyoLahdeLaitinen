package com.example.harjoitustyo;


import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.loopj.android.http.AsyncHttpClient;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class NutritionManager {
    //Beef average from: https://wwf.fi/app/uploads/h/h/l/deov6fzfmbfftdruse6a4xa/tt_selvitys_vaikuttavimmat-ilmastoteot.pdf
    //Plant and dairy average from: https://www.ptt.fi/media/julkaisut/tp195.pdf
    private static final double FINNISH_AVERAGE_BEEF_CONSUMPTION = 1.55;
    private static final double FINNISH_AVERAGE_DAIRY_CONSUMPTION = 2.3;
    private static final double FINNISH_AVERAGE_PLANT_CONSUMPTION = 1.2;
    private static final ProfileManager profileManager = ProfileManager.getInstance();

    //Creates nutrition manager singleton
    private static final NutritionManager nutritionManager = new NutritionManager();

    private NutritionManager() {
    }

    public static NutritionManager getInstance() {
        return nutritionManager;
    }

    //Method receives type of diet and amount of beef, dairy products and plant products in kg as parameters along with context.
    //Calculates percentage for consumptions compared to finnish average.
    //Sends asynced http get request to ilmastiodieetti FoodCalculator API with parameters.
    //Receives JSON data from API and parses the required data to object.
    //Sends received data for logging and then calls createBarChart method to display the data.
    public void calculateFoodEmissions(String diet, double meatAmount, double dairyAmount, double plantAmount, Context context) {
        String urlString = "https://ilmastodieetti.ymparisto.fi/ilmastodieetti/calculatorapi/v1/FoodCalculator";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        //"ilmastodieetti" food calculator API accepts values from 0 to 200.
        //The value is calculated with given amount(in kg) divided with finnish average consumption amount(in kg).
        //The calculation is multiplied by 100 to get the percentage as integer between 0 and 200
        //if the percentage exceeds 200 the amount is reduced to 200 before requesting data from the FoodCalculator.
        int beefAverage = (int) ((meatAmount / FINNISH_AVERAGE_BEEF_CONSUMPTION) * 100);
        if (beefAverage > 200)
            beefAverage = 200;
        int dairyAverage = (int) ((dairyAmount / FINNISH_AVERAGE_DAIRY_CONSUMPTION) * 100);
        if (dairyAverage > 200)
            dairyAverage = 200;
        int plantAverage = (int) ((plantAmount / FINNISH_AVERAGE_PLANT_CONSUMPTION) * 100);
        if (plantAverage > 200)
            plantAverage = 200;
        params.put("query.diet", diet);
        params.put("query.beefLevel", beefAverage);
        params.put("query.dairyLevel", dairyAverage);
        params.put("query.winterSaladLevel", plantAverage);
        client.get(urlString, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //Parse json object response
                    String dairy = response.getString("Dairy");
                    String meat = response.getString("Meat");
                    String plant = response.getString("Plant");
                    String total = response.getString("Total");
                    FoodEmissions emissionsData = new FoodEmissions(dairy, meat, plant, total);
                    JSONObject emissionJson = new JSONObject();
                    emissionJson.put("Meat", emissionsData.getMeat());
                    emissionJson.put("Dairy", emissionsData.getDairy());
                    emissionJson.put("Plant", emissionsData.getPlant());
                    emissionJson.put("Total", emissionsData.getTotal());
                    logEmissions(emissionJson, context);
                    createBarChart(emissionsData, context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                Toast.makeText(context, "Something went wrong trying to calculate emission data", Toast.LENGTH_SHORT).show();
                System.out.println("FAILURE");
            }

        });
    }

    //Method gets FoodEmissions object as parameter.
    //Creates BarChart AlertDialog for displaying the CO2 calculations received from FoodCalculator API
    private void createBarChart(FoodEmissions emissions, Context context) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labelNames = new ArrayList<>();
        labelNames.add("Meat");
        labelNames.add("Dairy");
        labelNames.add("Plant");
        labelNames.add("Total");
        barEntries.add(new BarEntry(0, Float.parseFloat(emissions.getMeat())));
        barEntries.add(new BarEntry(1, Float.parseFloat(emissions.getDairy())));
        barEntries.add(new BarEntry(2, Float.parseFloat(emissions.getPlant())));
        barEntries.add(new BarEntry(3, Float.parseFloat(emissions.getTotal())));
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout relativeLayout = (RelativeLayout) li.inflate(R.layout.emission_chart_layout, null);
        BarChart chart = relativeLayout.findViewById(R.id.bar_chart);
        //Customization to the BarCharts look
        BarDataSet set = new BarDataSet(barEntries, "CO2 Emissions (in Kg)");
        set.setColors(ColorTemplate.COLORFUL_COLORS);
        set.setValueTextSize(20f);
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        BarData data = new BarData(set);
        chart.setData(data);
        Legend legend = chart.getLegend();
        legend.setTextSize(15f);
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelNames));
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labelNames.size());
        AlertDialog builder = new AlertDialog.Builder(context)
                .setNegativeButton("Close", null)
                .setView(relativeLayout)
                .setCancelable(false)
                .create();
        builder.show();
    }

    //Method Reads users nutrition emissions log.
    //If entries are found, creates json array from the log data and returns it.
    public JSONArray readEmissionsLog(Context context) {
        JSONArray emissions = new JSONArray();
        try {
            InputStream inputStream = context.openFileInput(profileManager.getActiveUserName() + "Nutrition.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String string = "";
            if ((string = br.readLine()) != null) {
                JSONArray jsonObject = new JSONArray(string);
                emissions = jsonObject;
            }
            inputStream.close();
            return emissions;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Method gets the data from API calculations as json object.
    //logs the emission data in the json object to a json file.
    public void logEmissions(JSONObject object, Context context) {
        try {
            JSONArray jsonArray = readEmissionsLog(context);
            if (jsonArray == null) {
                jsonArray = new JSONArray();
            }
            FileOutputStream fos = context.openFileOutput(profileManager.getActiveUserName() + "Nutrition.json", Context.MODE_PRIVATE);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Nutrition data", object);
            jsonArray.put(jsonObject);
            fos.write(jsonArray.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            Log.e("IOException", "Error occurred writing file ");
        }
    }


}

