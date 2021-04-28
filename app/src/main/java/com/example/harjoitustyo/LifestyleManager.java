package com.example.harjoitustyo;


import android.app.AlertDialog;
import android.content.Context;
import android.content.res.AssetManager;
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

import cz.msebera.android.httpclient.Header;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class LifestyleManager {
    private static final ProfileManager profileManager = ProfileManager.getInstance();
    private ArrayList<THLData> thlDataSet = new ArrayList<>();

    //Creates nutrition manager singleton
    private static final LifestyleManager lifestyleManager = new LifestyleManager();

    private LifestyleManager() {
    }

    public static LifestyleManager getInstance() {
        return lifestyleManager;
    }


    //Method receives amount of flights, train distance and bus distance as parameters along with context.
    //Sends asynced http get request to ilmastiodieetti TransportCalculator API with parameters.
    //Receives JSON data from API and parses the required data to object.
    //Sends received data for logging and then calls createBarChart method to display the data.
    public void calculateTransportEmissions(int flightsAmount, int trainDistance, int busDistance, Context context) {
        String urlString = "https://ilmastodieetti.ymparisto.fi/ilmastodieetti/calculatorapi/v1/TransportCalculator";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        //Flight number of one way flights, limit 50
        params.put("query.europeanFlights", flightsAmount);
        //Train distance travelled in km, limit 100 000
        params.put("query.trainDistance", trainDistance);
        //Bus distance travelled in km, limit 100 000
        params.put("query.busDistance", busDistance);
        client.get(urlString, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //Parse json object response
                    String flight = response.getString("Flight");
                    String total = response.getString("Total");
                    JSONObject publicTransport = response.getJSONObject("PublicTransport");
                    String train = publicTransport.getString("Train");
                    String bus = publicTransport.getString("Bus");
                    TransportEmissions emissionsData = new TransportEmissions(flight, train, bus, total);
                    JSONObject emissionsJson = new JSONObject();
                    emissionsJson.put("Flight", emissionsData.getFlight());
                    emissionsJson.put("Train", emissionsData.getTrain());
                    emissionsJson.put("Bus", emissionsData.getBus());
                    emissionsJson.put("Total", emissionsData.getTotal());
                    logEmissions(emissionsJson, context);
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

    //Method receives group(male,female,overall) as parameter and sets corresponding group indication for finnish data.
    //Fetches users residence and age from ProfileManager and sets ageScaleId to match the users age group.
    //Compares users information to the THL data and returns feedback as String.
    public String getSmokersPercentage(String group) {
        try {
            String groupIndication;
            switch (group) {
                case "male":
                    groupIndication = "miehet";
                    break;
                case "female":
                    groupIndication = "naiset";
                    break;
                default:
                    groupIndication = "yhteensä";
            }
            String residence = profileManager.getActiveProfile().getResidence();
            int age = profileManager.getActiveProfile().getAge();
            int ageScaleId;
            if (age >= 20 && age < 65) {
                ageScaleId = 4405;
            } else if (age >= 65) {
                ageScaleId = 4406;
            } else {
                ageScaleId = 4404;
            }
            for (THLData data : thlDataSet) {
                if (data.getLocation().matches(residence) && data.getGroup().matches(groupIndication) && data.getAgeScaleID() == ageScaleId) {
                    if (ageScaleId == 4404) {
                        return "In " + data.getLocation() + " There are " + data.getPercentage() + "% " + group + " smokers";
                    } else {
                        return "In " + data.getLocation() + " There are " + data.getPercentage() + "% " + group + " smokers in your age group";
                    }
                }
            }
            return "No data found for smokers in your residence area.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong";
        }
    }

    //Reads csv file containing THL data about smokers in different areas in finland from assets folder.
    //Parses read data into THLData objects and stores them to ArrayList.
    public void queryTHLData(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStreamReader inputStreamReader = new InputStreamReader(assetManager.open("THLdata.csv"));
            BufferedReader csvReader = new BufferedReader(inputStreamReader);
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(";");
                THLData dataRow = new THLData(
                        data[0], Integer.parseInt(data[1]), data[2], Integer.parseInt(data[3]), data[4], data[5]);
                thlDataSet.add(dataRow);
            }
            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Method gets TransportEmissions object as parameter.
    //Creates BarChart AlertDialog for displaying the CO2 calculations received from TransportCalculator API
    private void createBarChart(TransportEmissions emissions, Context context) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labelNames = new ArrayList<>();
        labelNames.add("Flight");
        labelNames.add("Train");
        labelNames.add("Bus");
        labelNames.add("Total");
        barEntries.add(new BarEntry(0, Float.parseFloat(emissions.getFlight())));
        barEntries.add(new BarEntry(1, Float.parseFloat(emissions.getTrain())));
        barEntries.add(new BarEntry(2, Float.parseFloat(emissions.getBus())));
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

    //Method Reads users transportation emissions log.
    //If entries are found, creates json array from the log data and returns it.
    public JSONArray readEmissionsLog(Context context) {
        JSONArray emissions = new JSONArray();
        try {
            InputStream inputStream = context.openFileInput(profileManager.getActiveUserName() + "Transport.json");
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
            FileOutputStream fos = context.openFileOutput(profileManager.getActiveUserName() + "Transport.json", Context.MODE_PRIVATE);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Transport data", object);
            jsonArray.put(jsonObject);
            fos.write(jsonArray.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            Log.e("IOException", "Error occurred writing file ");
        }
    }

}

