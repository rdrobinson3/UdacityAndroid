package mindmine.com.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Trey Robinson on 8/20/14.
 * Copyright 2014 MindMine LLC.
 */
public class ForecastFragment extends Fragment {
    public ArrayAdapter<String> mAdapter;

    public ForecastFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("Today - Sunny - 88/63");
        arrayList.add("Tomorrow - Sunny - 88/63");
        arrayList.add("Weds - Sunny - 88/63");
        arrayList.add("Thurs - Sunny - 88/63");
        arrayList.add("Friday - Sunny - 88/63");
        arrayList.add("Saturday - Sunny - 88/63");
        arrayList.add("Sunday - Sunny - 88/63");

        mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, arrayList);
        ListView list = (ListView) rootView.findViewById(R.id.listview_forecast);
        list.setAdapter(mAdapter);




        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {

            FetchWeatherTask task = new FetchWeatherTask();
            task.execute("94043");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


     class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... strings) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.



            String forecastJsonStr;

            String format = "json";
            String units = "metric";
            String numDays = "7";

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "format";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri.Builder builder = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, strings[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, numDays);


                URL url = new URL(builder.build().toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            String[] weather = null;
            if(forecastJsonStr != null){
               try{
                   weather = WeatherParser.getWeatherDataFromJson(forecastJsonStr,7);
               } catch (JSONException e){
                   Log.e(LOG_TAG, e.getMessage(), e);
                   e.printStackTrace();
               }

            }

            return weather;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if(strings != null){
                mAdapter.clear();
                for(String day : strings){
                    mAdapter.add(day);
                }
            }
        }
    }
}