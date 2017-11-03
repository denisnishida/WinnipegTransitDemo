package com.example.winnipegtransitdemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity
{
  final String API_KEY = "?api-key=rQ8lXW4lpLR9CwiYqK";
  final String BEGIN_URL = "https://api.winnipegtransit.com/v2/";
  final String JSON_APPEND = ".json";
  final String STATUS_SCHEDULE_REQUEST = "statuses/schedule";
  final String STOP_SCHEDULE_REQUEST_BEGIN = "stops/";
  final String STOP_SCHEDULE_REQUEST_END = "/schedule";

  // Stop Schedule example:
  // http://api.winnipegtransit.com/v2/stops/10064/schedule.json?api-key=rQ8lXW4lpLR9CwiYqK

  String requestUrl;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // URL to request the schedules status
    // https://api.winnipegtransit.com/v2/statuses/schedule.json?api-key=rQ8lXW4lpLR9CwiYqK
    requestUrl = BEGIN_URL + STATUS_SCHEDULE_REQUEST + JSON_APPEND + API_KEY;
    
    processRequest();
  }

  private boolean isNetworkAvailable()
  {
    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  public void processRequest()
  {
    if (isNetworkAvailable())
    {
      // create and execute AsyncTask
      ProcessingTask task = new ProcessingTask();
      task.execute();
    }
    else
    {
      Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
    }
  }

  class ProcessingTask extends AsyncTask
  {
    // On the start of the thread
    @Override
    protected void onPreExecute()
    {
      super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects)
    {
      // Create URL object to RSS file
      URL url = null;      

      try
      {
        url = new URL(requestUrl);
      }
      catch (MalformedURLException e)
      {
        e.printStackTrace();
      }

      // Create and open HTTP connection
      HttpURLConnection connection = null;
      try
      {
        connection = (HttpURLConnection) url.openConnection();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }

      try
      {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null)
        {
          stringBuilder.append(line);
        }

        bufferedReader.close();

        return stringBuilder.toString();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      finally
      {
        connection.disconnect();
      }

      return null;
    }

    // On the end of the thread
    @Override
    protected void onPostExecute(Object o)
    {
      String response = o.toString();

      Log.i("INFO", o.toString());

      parseJSON(response);
    }

    // Verify the request and get the values
    private void parseJSON(String response)
    {
      try
      {
        // Using orj.json, get the file string and convert it to an object
        JSONObject object = (JSONObject) new JSONTokener(response).nextValue();

        // The Winnipeg Transit JSON results usually have nested values
        // We can identify the request by the first key of the first level

        // The method names() will retrieve an JSONArray with the key names
        JSONArray objectNames = object.names();

        // Retrieve the first key of the first level
        String firstKey = objectNames.getString(0);

        if (firstKey.equals("status"))
        {
          parseStatus(object.getJSONObject(firstKey));
        }
        else if (firstKey.equals("stop-schedule"))
        {

        }
      }
      catch (JSONException e)
      {
        e.printStackTrace();
      }
    }

    // Get the information from the status request
    private void parseStatus(JSONObject statusObject) throws JSONException
    {
      String message = statusObject.getString("message");

      // Other example not related to Winnipeg Transit
      //int likelihood = object.getInt("likelihood");

      TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
      tvStatus.setText(message);
    }
  }

  public void onClickShowBusesButton(View view)
  {

  }
}
