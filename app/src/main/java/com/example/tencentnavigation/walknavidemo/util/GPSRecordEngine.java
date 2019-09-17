package com.example.tencentnavigation.walknavidemo.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.navi.data.GpsLocation;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GPSRecordEngine {
	private static final String TAG = "integrated";

	private static GPSRecordEngine s_instance;

	private LocationManager mLocationManager;
	private LocationListener mLocationListener;

	private boolean mIsRecording = false;

	private List<String> mGpsList;
	private String mRecordFileName;

	public static GPSRecordEngine getInstance() {
		if (null == s_instance) {
			s_instance = new GPSRecordEngine();
		}
		return s_instance;
	}

	private GPSRecordEngine() {
		mGpsList = new ArrayList<>();
	}

	public synchronized boolean isRecording() {
		return mIsRecording;
	}

	public void startRecordLocation(Context context, String fileName) {
		if (mIsRecording) {
			return;
		}
		Toast.makeText(context, "start record location...", Toast.LENGTH_SHORT).show();
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mLocationListener = new MyLocationListener();
		try {
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
		} catch (SecurityException e) {
			Toast.makeText(context, "start record location error!!!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "startRecordLocation Exception", e);
			e.printStackTrace();
		}

		mRecordFileName = fileName;
		if (!mRecordFileName.endsWith(".gps")) {
			mRecordFileName += ".gps";
		}

		mIsRecording = true;
	}

	public void stopRecordLocation(Context context) {
        Toast.makeText(context, "stop record location, save to file...", Toast.LENGTH_SHORT).show();
		mLocationManager.removeUpdates(mLocationListener);
		saveGPS(mRecordFileName);
		mIsRecording = false;
	}

	public void startRecordTencentLocation(Context context, String fileName) {
		if (mIsRecording) {
			return;
		}
		Toast.makeText(context, "start record location...", Toast.LENGTH_SHORT).show();
		Log.i(TAG, "start record location...");

		mRecordFileName = fileName;
		if (!mRecordFileName.endsWith(".gps")) {
			mRecordFileName += ".gps";
		}

		mIsRecording = true;
	}

	public void recordTencentLocation(TencentLocation tencentLocation) {
		if (mIsRecording && tencentLocation != null) {
			mGpsList.add(locationToString(tencentLocation));
		}
	}

	public void recordGpsLocation(GpsLocation gpsLocation) {
		if (mIsRecording && gpsLocation != null) {
			mGpsList.add(locationToString(gpsLocation));
		}
	}

	public void stopRecordTencentLocation(Context context) {
		Toast.makeText(context, "stop record location, save to file...", Toast.LENGTH_SHORT).show();
		Log.i(TAG, "stop record location, save to file...");
		saveGPS(mRecordFileName);
		mIsRecording = false;
	}

	public void recordLocations(List<LatLng> points, String fileName) {
		mIsRecording = true;
		mRecordFileName = fileName;
		if (!mRecordFileName.endsWith(".gps")) {
			mRecordFileName += ".gps";
		}
		float results[] = new float[2];
		float heading;
		for (int i = 0; i < points.size() - 1; i++) {
			LatLng p1 = points.get(i);
			LatLng p2 = points.get(i + 1);
			Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results);
			//distance = results[0];
			heading = results[1];
			// 确保方向角大于0°
			while (heading < 0) {
				heading += 360;
			}

			Location location = new Location(LocationManager.GPS_PROVIDER);
			location.setLongitude(p1.longitude);
			location.setLatitude(p1.latitude);
			location.setAccuracy(5);
			location.setBearing(heading);
			location.setSpeed(30);
			location.setAltitude(10);

			mGpsList.add(locationToString(location));
		}
		saveGPS(mRecordFileName);
		mIsRecording = false;
	}

	// GPS使用的日期格式
	private static SimpleDateFormat gpsDataFormatter =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

	private DecimalFormat df = new DecimalFormat("0.000");

	private String locationToString(Location location) {
		StringBuilder sb = new StringBuilder();
		
		long time = System.currentTimeMillis();
		String timeStr = gpsDataFormatter.format(new Date(time));

		sb.append(location.getLatitude());
		sb.append(",");
		sb.append(location.getLongitude());
		sb.append(",");
		sb.append(location.getAccuracy());
		sb.append(",");
		sb.append(location.getBearing());
		sb.append(",");
		sb.append(location.getSpeed());
		sb.append(",");
		sb.append(timeStr);
		sb.append(",");
		sb.append(df.format((double) time / 1000.0));
		// sb.append(df.format(System.currentTimeMillis()/1000.0));
		// sb.append(df.format(location.getTime()/1000.0));
		sb.append(",");
		sb.append(location.getAltitude());
		sb.append("\n");
		return sb.toString();
	}

	private String locationToString(TencentLocation location) {
		StringBuilder sb = new StringBuilder();

		long time = System.currentTimeMillis();
		String timeStr = gpsDataFormatter.format(new Date(time));

		sb.append(location.getLatitude());
		sb.append(",");
		sb.append(location.getLongitude());
		sb.append(",");
		sb.append(location.getAccuracy());
		sb.append(",");
		sb.append(location.getBearing());
		sb.append(",");
		sb.append(location.getSpeed());
		sb.append(",");
		sb.append(timeStr);
		sb.append(",");
		sb.append(df.format((double) time / 1000.0));
		// sb.append(df.format(System.currentTimeMillis()/1000.0));
		// sb.append(df.format(location.getTime()/1000.0));
		sb.append(",");
		sb.append(location.getAltitude());
		sb.append(",");
		sb.append(location.getIndoorBuildingId());
		sb.append(",");
		sb.append(location.getIndoorBuildingFloor());
		sb.append("\n");
		return sb.toString();
	}

	private String locationToString(GpsLocation location) {
		StringBuilder sb = new StringBuilder();

		long time = System.currentTimeMillis();
		String timeStr = gpsDataFormatter.format(new Date(time));

		sb.append(location.getLatitude());
		sb.append(",");
		sb.append(location.getLongitude());
		sb.append(",");
		sb.append(location.getAccuracy());
		sb.append(",");
		sb.append(location.getDirection());
		sb.append(",");
		sb.append(location.getVelocity());
		sb.append(",");
		sb.append(timeStr);
		sb.append(",");
		sb.append(df.format((double) time / 1000.0));
		// sb.append(df.format(System.currentTimeMillis()/1000.0));
		// sb.append(df.format(location.getTime()/1000.0));
		sb.append(",");
		sb.append(location.getAltitude());
		sb.append(",");
		sb.append(location.getBuildingId());
		sb.append(",");
		sb.append(location.getFloorName());
		sb.append("\n");
		return sb.toString();
	}

	private void saveGPS(String path) {
		OutputStreamWriter writer = null;
		try {
			File outFile = new File(path);
			File parent = outFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			OutputStream out = new FileOutputStream(outFile);
			writer = new OutputStreamWriter(out);
			for (String line : mGpsList) {
				writer.write(line);
			}
		} catch (Exception e) {
			Log.e(TAG, "saveGPS Exception", e);
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to flush output stream", e);
				}
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to close output stream", e);
				}
			}
		}
	}

	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				mGpsList.add(locationToString(location));
			}
		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}
}
