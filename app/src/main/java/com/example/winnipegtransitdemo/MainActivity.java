package com.example.winnipegtransitdemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    processRss();
  }

  private boolean isNetworkAvailable()
  {
    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  public void processRss()
  {
    if (isNetworkAvailable())
    {
      // create and execute AsyncTask
      RssProcessingTask task = new RssProcessingTask();
      task.execute();
    }
    else
    {
      Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
    }
  }

  class RssProcessingTask extends AsyncTask
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
      String feedUrl = "https://api.winnipegtransit.com/v2/statuses/schedule.json?api-key=rQ8lXW4lpLR9CwiYqK";

      try
      {
        url = new URL(feedUrl);
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

      String message = "No information available";

      // Using orj.json
      try {
          JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
          JSONObject statusObject = object.getJSONObject("status");
          message = statusObject.getString("message");
//          int likelihood = object.getInt("likelihood");
//          JSONArray photos = object.getJSONArray("photos");
      }
      catch (JSONException e) {
          e.printStackTrace();
      }

      TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
      tvStatus.setText(message);
    }
  }
}
