package com.example.tencentnavigation.walknavidemo.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentMotion;
import com.tencent.map.geolocation.TencentPoi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * GPS回放引擎
 */
public class GPSReplayEngine implements Runnable {
	private static final String TAG = "navisdk";
	private static GPSReplayEngine s_instance = null;
	private Thread mMockGpsProviderTask = null;

	// Android系统的Mock
	private LocationManager mLocationManager = null;
	private ArrayList<LocationListener> mLocationListeners = new ArrayList<>();
	private boolean mIsAllowMock = false;
	private int mMockGpsStatus = LocationProvider.AVAILABLE;

	// Tencent轨迹Mock
	private ArrayList<TencentLocationListener> mTencentLocationListeners = new ArrayList<>();

	private List<String> mDatas = new ArrayList<String>();
	private boolean mIsReplaying = false;

	private boolean mIsMockTencentLocation = false;

	/**
	 * 是否已经暂停
	 */
	private boolean mPause = false;

	public static GPSReplayEngine getInstance() {
		if (null == s_instance) {
			s_instance = new GPSReplayEngine();
		}
		return s_instance;
	}

	private GPSReplayEngine() {
	}

	private synchronized boolean isReplaying() {
		return mIsReplaying;
	}

	private boolean isAllowMock() {
		if (mLocationManager != null && !mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			try {
                mLocationManager.addTestProvider(
                        LocationManager.GPS_PROVIDER,
                        true,
                        true,
                        false,
                        false,
                        true,
                        true,
                        true,
                        android.location.Criteria.POWER_LOW,
                        android.location.Criteria.ACCURACY_FINE);
				mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
			} catch (SecurityException e) {
				// 如果之前未在开发者选项中手动打开“允许模拟GPS”，这里可能会抛安全异常
				Log.e(TAG, "isAllowMock Exception", e);
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public void addLocationListener(LocationListener locationListener) {
		if (locationListener != null) {
			mLocationListeners.add(locationListener);
		}
	}

	public void removeLocationListener(LocationListener locationListener) {
		if (locationListener != null) {
			mLocationListeners.remove(locationListener);
		}
	}

	public void addTencentLocationListener(TencentLocationListener listener) {
		if (listener != null) {
			mTencentLocationListeners.add(listener);
		}
	}

	public void removeTencentLocationListener(TencentLocationListener listener) {
		if (listener != null) {
			mTencentLocationListeners.remove(listener);
		}
	}

	/*
	 * 模拟轨迹
	 * @param context
	 * @param fileName 轨迹文件绝对路径
	 */
	public void startMockLocation(Context context, String fileName, boolean allowMock) {
		mIsMockTencentLocation = false;
		if (mLocationManager == null) {
			mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		}
		mIsAllowMock = allowMock && isAllowMock();
		if (!mIsAllowMock) {
			Log.e(TAG, "startMockLocation is not allow mock location, use custom replay, don't forget add listener!!!");
		}
		BufferedReader reader = null;
		try {
			File file = new File(fileName);
			InputStream is = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(is));

			String line;
			while ((line = reader.readLine()) != null) {
				mDatas.add(line);
			}
			if (mDatas.size() > 0) {
				mIsReplaying = true;
				synchronized (this) {
					mPause = false;
				}
				mMockGpsProviderTask = new Thread(this);
				mMockGpsProviderTask.start();
			}
		} catch (Exception e) {
			Log.e(TAG, "startMockLocation Exception", e);
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
				Log.e(TAG, "startMockLocation Exception", e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 退出应用前也需要调用停止模拟位置，否则手机的正常GPS定位不会恢复
	 */
	public void stopMockLocation() {
		try {
			mIsReplaying = false;
			mMockGpsProviderTask.join();
			mMockGpsProviderTask = null;
		} catch (Exception e) {
            Log.e(TAG, "stopMockLocation Exception", e);
			e.printStackTrace();
		}

		try {
			if (mIsAllowMock) {
				mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
				mLocationManager = null;
			}
		} catch (Exception e) {
            Log.e(TAG, "stopMockLocation Exception", e);
			e.printStackTrace();
		}
	}

	/*
	 * 模拟轨迹
	 * @param context
	 * @param fileName 轨迹文件绝对路径
	 */
	public void startMockTencentLocation(String fileName) {
		mIsMockTencentLocation = true;
		BufferedReader reader = null;
		try {
			File file = new File(fileName);
			InputStream is = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(is));

			String line;
			while ((line = reader.readLine()) != null) {
				mDatas.add(line);
			}
			if (mDatas.size() > 0) {
				mIsReplaying = true;
				synchronized (this) {
					mPause = false;
				}
				mMockGpsProviderTask = new Thread(this);
				mMockGpsProviderTask.start();
			}
		} catch (Exception e) {
			Log.e(TAG, "startMockTencentLocation Exception", e);
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
				Log.e(TAG, "startMockTencentLocation Exception", e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 退出应用前也需要调用停止模拟位置，否则手机的正常GPS定位不会恢复
	 */
	public void stopMockTencentLocation() {
		try {
			mIsReplaying = false;
			mMockGpsProviderTask.join();
			mMockGpsProviderTask = null;
		} catch (Exception e) {
			Log.e(TAG, "stopMockTencentLocation Exception", e);
			e.printStackTrace();
		}
	}

	public void resumeMockLocation() {
		synchronized (this) {
			mPause = false;
			notify();
		}
	}

	public void pauseMockLocation() {
		synchronized (this) {
			mPause = true;
		}
	}

	public void setMockGpsStatus(int gpsStatus) {
		mMockGpsStatus = gpsStatus;
	}

	@Override
	public void run() {
		for (String line : mDatas) {
			if (!mIsReplaying) {
				Log.e(TAG, "stop gps replay");
				break;
			}
			if (TextUtils.isEmpty(line)) {
				continue;
			}

			boolean mockResult;
			if (mIsMockTencentLocation) {
				mockResult = mockTencentLocation(line);
			} else {
				mockResult = mockAndroidLocation(line);
			}
			if (!mockResult) {
				break;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				checkToPauseThread();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void checkToPauseThread() throws InterruptedException {
		synchronized (this) {
			while (mPause) {
				wait();
			}
		}
	}

	private boolean mockTencentLocation(String line) {
		try {
			String[] parts = line.split(",");

			double latitude = Double.valueOf(parts[0]);
			double longitude = Double.valueOf(parts[1]);
			float accuracy = Float.valueOf(parts[2]);
			float bearing = Float.valueOf(parts[3]);
			float speed = Float.valueOf(parts[4]);
			double altitude = Double.valueOf(parts[7]);
			String buildingId;
			String floorName;
			if (parts.length >= 10) {
				buildingId = parts[8];
				floorName = parts[9];
			} else {
				buildingId = "";
				floorName = "";
			}

			MyTencentLocation location = new MyTencentLocation();
			location.setProvider("gps");
			location.setLongitude(longitude);
			location.setLatitude(latitude);
			location.setAccuracy(accuracy);
			location.setDirection(bearing);
			location.setVelocity(speed);
			location.setAltitude(altitude);
			location.setBuildingId(buildingId);
			location.setFloorName(floorName);
			location.setRssi(4);
			location.setTime(System.currentTimeMillis());

			for (TencentLocationListener listener : mTencentLocationListeners) {
				if (listener != null) {
					listener.onLocationChanged(location, 0, "");
					listener.onStatusUpdate(LocationManager.GPS_PROVIDER, mMockGpsStatus, "");
				}
			}
		} catch(Exception e) {
			Log.e(TAG, "Mock Location Exception", e);
			// 如果未开位置模拟，这里可能出异常
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean mockAndroidLocation(String line) {
		try {
			String[] parts = line.split(",");

			double latitude = Double.valueOf(parts[0]);
			double longitude = Double.valueOf(parts[1]);
			double[] result = CoordinateConverter.wgs84togcj02(longitude, latitude);
			longitude = result[0];
			latitude = result[1];
			float accuracy = Float.valueOf(parts[2]);
			float bearing = Float.valueOf(parts[3]);
			float speed = Float.valueOf(parts[4]);
			double altitude = Double.valueOf(parts[7]);

			Location location = new Location(LocationManager.GPS_PROVIDER);
			location.setLongitude(longitude);
			location.setLatitude(latitude);
			location.setAccuracy(accuracy);
			location.setBearing(bearing);
			location.setSpeed(speed);
			location.setAltitude(altitude);
			location.setTime(System.currentTimeMillis());
			location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

			if (mIsAllowMock) {
				mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER,
						mMockGpsStatus, null, System.currentTimeMillis());
				mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
			} else {
				for (LocationListener listener : mLocationListeners) {
					if (listener != null) {
						listener.onLocationChanged(location);
						listener.onStatusChanged(LocationManager.GPS_PROVIDER, mMockGpsStatus, null);
					}
				}
			}
		} catch(Exception e) {
			Log.e(TAG, "Mock Location Exception", e);
			// 如果未开位置模拟，这里可能出异常
			e.printStackTrace();
			return false;
		}
		return true;
	}


	class MyTencentLocation implements TencentLocation {
		/**
		 * 纬度
		 */
		private double latitude = 0;
		/**
		 * 经度
		 */
		private double longitude = 0;
		/**
		 * 精度
		 */
		private float accuracy = 0;
		/**
		 * gps方向
		 */
		private float direction = -1;
		/**
		 * 速度
		 */
		private float velocity = 0;
		/**
		 * 时间
		 */
		private long time = 0;
		/**
		 * 海拔高度
		 */
		private double altitude = 0;
		/**
		 * 定位来源
		 */
		private String provider = "";
		/**
		 * GPS信号等级
		 */
		private int rssi = 0;

		/**
		 * 手机的机头方向
		 */
		private float phoneDirection = -1;

		private String buildingId = "";

		private String floorName = "";

		@Override
		public String getProvider() {
			return provider;
		}

		@Override
		public double getLatitude() {
			return latitude;
		}

		@Override
		public double getLongitude() {
			return longitude;
		}

		@Override
		public double getAltitude() {
			return latitude;
		}

		@Override
		public float getAccuracy() {
			return accuracy;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String getAddress() {
			return null;
		}

		@Override
		public String getNation() {
			return null;
		}

		@Override
		public String getProvince() {
			return null;
		}

		@Override
		public String getCity() {
			return null;
		}

		@Override
		public String getDistrict() {
			return null;
		}

		@Override
		public String getTown() {
			return null;
		}

		@Override
		public String getVillage() {
			return null;
		}

		@Override
		public String getStreet() {
			return null;
		}

		@Override
		public String getStreetNo() {
			return null;
		}

		@Override
		public Integer getAreaStat() {
			return null;
		}

		@Override
		public List<TencentPoi> getPoiList() {
			return null;
		}

		@Override
		public float getBearing() {
			return direction;
		}

		@Override
		public float getSpeed() {
			return velocity;
		}

		@Override
		public long getTime() {
			return time;
		}

		@Override
		public long getElapsedRealtime() {
			return time;
		}

		@Override
		public int getGPSRssi() {
			return rssi;
		}

		@Override
		public String getIndoorBuildingId() {
			return buildingId;
		}

		@Override
		public String getIndoorBuildingFloor() {
			return floorName;
		}

		@Override
		public int getIndoorLocationType() {
			return 0;
		}

		@Override
		public double getDirection() {
			return phoneDirection;
		}

		@Override
		public String getCityCode() {
			return null;
		}

		@Override
		public TencentMotion getMotion() {
			return null;
		}

		@Override
		public int getGpsQuality() {
			return 0;
		}

		@Override
		public float getDeltaAngle() {
			return 0;
		}

		@Override
		public float getDeltaSpeed() {
			return 0;
		}

		@Override
		public int getCoordinateType() {
			return 0;
		}

		@Override
		public int isMockGps() {
			return 0;
		}

		@Override
		public Bundle getExtra() {
			return null;
		}

		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}

		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}

		public void setAccuracy(float accuracy) {
			this.accuracy = accuracy;
		}

		public void setDirection(float direction) {
			this.direction = direction;
		}

		public void setVelocity(float velocity) {
			this.velocity = velocity;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public void setAltitude(double altitude) {
			this.altitude = altitude;
		}

		public void setProvider(String provider) {
			this.provider = provider;
		}

		public void setRssi(int rssi) {
			this.rssi = rssi;
		}

		public void setPhoneDirection(float phoneDirection) {
			this.phoneDirection = phoneDirection;
		}

		public void setBuildingId(String buildingId) {
			this.buildingId = buildingId;
		}

		public void setFloorName(String floorName) {
			this.floorName = floorName;
		}
	}
}
