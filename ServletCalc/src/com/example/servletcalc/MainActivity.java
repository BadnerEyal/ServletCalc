package com.example.servletcalc;

// This code was fixed and noted by Shachar lavi 
// shachar.la.v@gmail.com

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView resultTv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// this class handles 2 numbers from TextViews and send them to a local server 
		// the local server sum the numbers and returns the result to the activity
		// the summing can take some time , thats why the server request is in AsyncTask

		Button btn = (Button) findViewById(R.id.btn);
		final EditText num1 = (EditText) findViewById(R.id.numberView1);
		final EditText num2 = (EditText) findViewById(R.id.numberView2);
		resultTv = (TextView) findViewById(R.id.result);
		
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String n1Str = num1.getText().toString();// First number from a textview
				String n2Str = num2.getText().toString();// Second number from a textview
				
				try
				{
					int n1 = Integer.parseInt(n1Str);
					int n2 = Integer.parseInt(n2Str);
				
					SumTask task = new SumTask();
					task.execute(new Integer[]{n1,n2});// Sum the numbers and start the Asynctask
				}
				catch(NumberFormatException e)
				{
					Toast.makeText(MainActivity.this, "Input is not a number,  try again!", Toast.LENGTH_LONG).show();
				}
				
				
			
			}
		});
	}

	
	private class SumTask extends  AsyncTask<Integer, Integer, Integer>{

		private boolean isOK = false;// this is a FLAG which incharge to infrom that the server connection was succesfull
		@Override
		protected void onPreExecute() { //Start of the task - run in the main thread
			super.onPreExecute();
			resultTv.setText("Calculating....");
		}
		@Override
		protected Integer doInBackground(Integer... param) // this part run in a different thread - UI CANNOT BE UPDATED HERE !!
		{
			
			String urlString = "http://10.0.2.2:8084/WebApplication1/Calculator?n1="+param[0]+"&n2="+param[1];
			//The structure is "http://10.0.2.2:(your port -Local / server port)/Your server application name/the class which get the request in the server?
			//n1="+param[0]+"&n2="+param[1]; - those are parameters + their values /
			
			
			
			
			String resultStr = connect(urlString); //Connect is an inner function (BUTTOM OF THIS PAGE )
												  //The function is in charge of approaching the server and bring the result into 
												 //resultStr - var
			if(resultStr == null)
			{
				return 0;
			}
			
			
			
			Integer result1 = Integer.valueOf(resultStr);

			return result1;
		}
		@Override
		protected void onPostExecute(Integer result) //Get the result and update the UI here 
		{
			super.onPostExecute(result);
			if(isOK)
			{
				resultTv.setText(result.toString());
			}
			else
			{
				resultTv.setText("Due to errot cannot calculate");
			}
		}
		
		public String connect(String url) //Set the request from the server
		{

		    HttpClient httpclient = new DefaultHttpClient();

		    // Prepare a request object
		    HttpGet httpget = new HttpGet(url); 
		    //httpget.setParams(params);
		    // Execute the request
		    
		    HttpResponse response;
		    try {
		        response = httpclient.execute(httpget);
		        // Examine the response status
		        Log.i("MyActivity",response.getStatusLine().toString());

		        // Get hold of the response entity
		        HttpEntity entity = response.getEntity();
		        // If the response does not enclose an entity, there is no need
		        // to worry about connection release

		        if (entity != null) {

		            InputStream instream = entity.getContent(); // urlConnection.getInputStream()
		            String result = convertStreamToString(instream);
		            // now you have the string representation of the HTML request
		            instream.close();
		            
		            isOK = true;
		            return result;
		        }
		        else
		        {
		        	return null;
		        }


		    } 
		    catch (Exception e) 
		    {
		    	Log.e("MyActivity", e.getMessage());
		    	
		    	return null;
		    }
		}
		
		private String convertStreamToString(InputStream inputStream) throws IOException {
			 //* To convert the InputStream to String we use the BufferedReader.readLine()
		     //* method. We iterate until the BufferedReader return null which means
		     //* there's no more data to read. Each line will appended to a StringBuilder
		     //* and returned as String.
		     
			if (inputStream != null) {
				StringBuilder sb = new StringBuilder();
				String line;
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
				} finally {
					inputStream.close();
				}
				return sb.toString();
			} else {
				return "";
			}
		}


	}
	
}

