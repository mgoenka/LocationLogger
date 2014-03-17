package com.mgoenka.locationlogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class LocationLoggerActivity extends Activity {
	private static final long MEASURE_TIME = 1000 * 1;
	private static final long POLLING_FREQ = 1000 * 1;
	private static final float MIN_DISTANCE = 0.5f;
	

	// Views for display location information
	private TextView mAccuracyView;
	private TextView mTimeView;
	private TextView mLatView;
	private TextView mLngView;

	private int mTextViewColor = Color.GRAY;

	// Current best location estimate
	private Location mBestReading;

	// Reference to the LocationManager and LocationListener
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;

	private final String TAG = "Edx";
	private boolean mFirstUpdate = true;
	String wifi = "";
	String fileName = "/sdcard/edx.log";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_location_logger);

		mAccuracyView = (TextView) findViewById(R.id.accuracy_view);
		mTimeView = (TextView) findViewById(R.id.time_view);
		mLatView = (TextView) findViewById(R.id.lat_view);
		mLngView = (TextView) findViewById(R.id.lng_view);
		
		LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean gps_on = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		fileName += (gps_on ? "GPS" : "") + wifi + 1 + (int)(Math.random() * 1000)  + ".txt";


		// Acquire reference to the LocationManager
		if (null == (mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE)))
			finish();
		
		updateLocation();

		mLocationListener = new LocationListener() {
			// Called back when location changes
			public void onLocationChanged(Location location) {
				ensureColor();
				// Update best estimate
				mBestReading = location;

				// Update display
				updateDisplay(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// NA
			}

			public void onProviderEnabled(String provider) {
				// NA
			}

			public void onProviderDisabled(String provider) {
				// NA
			}
		};
	}
	
	public void onFetchLocation(View v) {
		updateLocation();
	}
	
	public void updateLocation() {
		// Get best last location measurement
		mBestReading = lastKnownLocation();

		// Display last reading information
		if (null != mBestReading) {
			updateDisplay(mBestReading);
		} else {
			mAccuracyView.setText("No Initial Reading Available");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Determine whether initial reading is
		// "good enough"
		// Register for network location updates
		mLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, POLLING_FREQ, MIN_DISTANCE,
				mLocationListener);

		// Register for GPS location updates
		mLocationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, POLLING_FREQ, MIN_DISTANCE,
				mLocationListener);

		// Schedule a runnable to unregister location listeners
		Executors.newScheduledThreadPool(1).schedule(new Runnable() {

			@Override
			public void run() {
				Log.i(TAG, "location updates cancelled");
				mLocationManager.removeUpdates(mLocationListener);
			}
		}, MEASURE_TIME, TimeUnit.MILLISECONDS);
	}

	// Unregister location listeners
	@Override
	protected void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(mLocationListener);
	}

	// Get the last known location from all providers
	// return best reading is as accurate as minAccuracy and
	// was taken no longer then minTime milliseconds ago
	private Location lastKnownLocation() {
		Location bestResult = null;

		List<String> matchingProviders = mLocationManager.getAllProviders();

		for (String provider : matchingProviders) {
			Location location = mLocationManager.getLastKnownLocation(provider);
			if (location != null) {
				bestResult = location;
			}
		}
		return bestResult;
	}

	// Update display
	private void updateDisplay(Location location) {
		Float accuracy = location.getAccuracy();
		String time = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss",
				Locale.getDefault()).format(new Date(location.getTime()));
		Double longitude = location.getLongitude();
		Double latitude = location.getLatitude();
		
		appendLog("Accuracy: " + accuracy + ", Time: " + time + ", Longitude: " + longitude + ", Latitude: " +
				latitude);
		
		mAccuracyView.setText("Accuracy:" + accuracy);
		mTimeView.setText("Time:" + time);
		mLatView.setText("Longitude:" + longitude);
		mLngView.setText("Latitude:" + latitude);
	}
	
	public void appendLog(String text) {       
	   File logFile = new File(fileName);
	   if (!logFile.exists()) {
	      try {
	         logFile.createNewFile();
	      } 
	      catch (IOException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}

	private void ensureColor() {
		if (mFirstUpdate) {
			setTextViewColor(mTextViewColor);
			mFirstUpdate = false;
		}
	}

	private void setTextViewColor(int color) {
		mAccuracyView.setTextColor(color);
		mTimeView.setTextColor(color);
		mLatView.setTextColor(color);
		mLngView.setTextColor(color);
	}
}
