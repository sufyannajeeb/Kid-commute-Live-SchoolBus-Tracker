package com.example.kidcommute;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocationService extends Service {

	public static String place = "";
	private LocationManager locationManager;
	private Boolean locationChanged;

	private Handler handler = new Handler();
	public static Location curLocation;
	public static boolean isService = true;
	public static String lati = "", logi = "";

	public static String tmplocs = "";
	SharedPreferences sh;
	float my_speed=0;

	private Location latestFix;
	private Location lastSentFix;
	private long lastSentAt;
	private RequestQueue requestQueue;

	private static final String TAG = "LocationService";
	private static final String CHANNEL_ID = "kid_commute_location";
	private static final int NOTIFICATION_ID = 101;
	private static final long UPDATE_INTERVAL_MS = 15000; // send location every 15 seconds
	private static final long UPDATE_MIN_DISTANCE_M = 5;
	private static final long MAX_FIX_AGE_MS = 600000;
	private static final float MAX_FIX_ACCURACY_M = 1000f;
	private static final long FRESHNESS_TIE_MS = 30000;
	private static final float MOVE_BEFORE_RESEND_M = 15f;
	private static final long HEARTBEAT_MS = 60000;
	private static final long GEOCODE_THROTTLE_MS = 60000;
	private boolean geocoding = false;
	private long lastGeocodeAt = 0;
	private Location lastGeocodedFix;
	private boolean loopStarted = false;
	private boolean updatesRequested = false;
	private boolean realFixSeen = false;

	LocationListener locationListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			handleFix(location);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == 0)// UnAvailable
			{
			} else if (status == 1)// Trying to Connect
			{
			} else if (status == 2) {// Available
			}
		}
	};

	private void handleFix(Location location) {
		if (location == null) {
			return;
		}
		if (!isPlausibleFix(location)) {
			Log.w(TAG, "ignoring unusable fix " + location.getLatitude() + "," + location.getLongitude()
					+ " acc=" + (location.hasAccuracy() ? location.getAccuracy() : -1f));
			return;
		}
		// A fix delivered by the provider is proof the position is real. Until one
		// arrives, getLastKnownLocation can only hold a provider default.
		realFixSeen = true;
		if (latestFix == null || location.getTime() > latestFix.getTime()) {
			latestFix = location;
		}
		curLocation = latestFix;
		locationChanged = true;
		maybeUpdatePlaceName(location);
	}

	private boolean isPlausibleFix(Location location) {
		if (location == null) {
			return false;
		}
		// mock fixes (emulator GPS / test tools) are accepted so tracking works
		// while the app is being tested without a real vehicle
		if (location.getLatitude() == 0d && location.getLongitude() == 0d) {
			return false;
		}
		if (Math.abs(location.getLatitude()) > 90 || Math.abs(location.getLongitude()) > 180) {
			return false;
		}
		if (location.hasAccuracy() && location.getAccuracy() > MAX_FIX_ACCURACY_M) {
			return false;
		}
		return System.currentTimeMillis() - location.getTime() <= MAX_FIX_AGE_MS;
	}

	// The more precise fix wins, but only within FRESHNESS_TIE_MS of the other one so
	// an old precise fix cannot beat a current one.
	private Location betterFix(Location a, Location b) {
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		long ageA = System.currentTimeMillis() - a.getTime();
		long ageB = System.currentTimeMillis() - b.getTime();
		float accA = a.hasAccuracy() ? a.getAccuracy() : Float.MAX_VALUE;
		float accB = b.hasAccuracy() ? b.getAccuracy() : Float.MAX_VALUE;
		if (ageA <= ageB + FRESHNESS_TIE_MS && accA < accB) {
			return a;
		}
		if (ageB <= ageA + FRESHNESS_TIE_MS && accB < accA) {
			return b;
		}
		return ageA <= ageB ? a : b;
	}

	private float distanceM(Location a, Location b) {
		if (a == null || b == null) {
			return Float.MAX_VALUE;
		}
		float[] result = new float[1];
		Location.distanceBetween(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude(), result);
		return result[0];
	}

	// --------------------------------------------------------------- place name

	/**
	 * Best-effort reverse geocode of the current fix so the uploaded row carries
	 * a readable place name (the server stores it in the `location` column and
	 * the parent screen shows it under "BUS IS NOW AT"). Runs off the main thread
	 * and is throttled so it cannot slow the upload loop.
	 */
	private void maybeUpdatePlaceName(final Location location) {
		if (location == null || geocoding || !Geocoder.isPresent()) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now - lastGeocodeAt < GEOCODE_THROTTLE_MS) {
			return;
		}
		// no need to re-geocode while we are basically in the same spot
		if (place.length() > 0 && lastGeocodedFix != null
				&& distanceM(lastGeocodedFix, location) < 200f) {
			return;
		}
		lastGeocodeAt = now;
		geocoding = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String name = "";
				try {
					Geocoder geocoder = new Geocoder(LocationService.this, Locale.getDefault());
					List<Address> addresses = geocoder.getFromLocation(
							location.getLatitude(), location.getLongitude(), 1);
					if (addresses != null && !addresses.isEmpty()) {
						Address a = addresses.get(0);
						String road = a.getThoroughfare();
						String city = a.getLocality();
						if (road != null && road.length() > 0 && city != null && city.length() > 0) {
							name = road + ", " + city;
						} else if (city != null && city.length() > 0) {
							name = city;
						} else if (a.getSubAdminArea() != null && a.getSubAdminArea().length() > 0) {
							name = a.getSubAdminArea();
						} else if (a.getFeatureName() != null && a.getFeatureName().length() > 0) {
							name = a.getFeatureName();
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "reverse geocode failed: " + e.getMessage());
				}
				geocoding = false;
				name = name.trim();
				if (name.length() > 0) {
					place = name;
					lastGeocodedFix = new Location(location);
					Log.d(TAG, "place resolved: " + place);
				}
			}
		}, "location-geocoder").start();
	}


	@Override
	public void onCreate() {
		super.onCreate();

		requestUpdates();
		requestQueue = Volley.newRequestQueue(getApplicationContext());
		isService = true;
		sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		createNotificationChannel();
		// keep the process alive so the admin keeps receiving updates
		startForeground(NOTIFICATION_ID, buildNotification());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!loopStarted) {
			loopStarted = true;
			Log.d(TAG, "location sharing started, url=" + sh.getString("url", ""));
			handler.postDelayed(GpsFinder, 100);
		}
		return START_STICKY;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();

	}

	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
					"Live bus location", NotificationManager.IMPORTANCE_LOW);
			channel.setShowBadge(false);
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			if (nm != null) {
				nm.createNotificationChannel(channel);
			}
		}
	}

	private Notification buildNotification() {
		return new NotificationCompat.Builder(this, CHANNEL_ID)
				.setContentTitle("kid Commute")
				.setContentText("Sharing this bus's live location with parents...")
				.setSmallIcon(R.drawable.ic_location)
				.setOngoing(true)
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.build();
	}

	@Override
	public void onDestroy() {
		if (handler != null) {
			handler.removeCallbacks(GpsFinder);
		}
		if (locationManager != null) {
			try {
				locationManager.removeUpdates(locationListener);
			} catch (Exception e) {
				Log.e(TAG, "removeUpdates failed: " + e.getMessage());
			}
		}
		updatesRequested = false;
		isService = false;
		stopForeground(true);
		Log.d(TAG, "location sharing stopped");
		super.onDestroy();
	}

	public Runnable GpsFinder = new Runnable() {

		public void run() {
			Location fix = usableFix();

			if (fix == null) {
				Log.w(TAG, realFixSeen
						? "no usable fix, not sending anything to admin"
						: "waiting for a real gps fix, provider default not trusted yet");
			} else {
				curLocation = fix;
				my_speed = fix.getSpeed();
				lati = String.format(Locale.US, "%.6f", fix.getLatitude());
				logi = String.format(Locale.US, "%.6f", fix.getLongitude());

				boolean moved = distanceM(lastSentFix, fix) >= MOVE_BEFORE_RESEND_M;
				boolean heartbeat = System.currentTimeMillis() - lastSentAt >= HEARTBEAT_MS;
				if (moved || heartbeat || lastSentFix == null) {
					insert_place();
					lastSentFix = new Location(fix);
					lastSentAt = System.currentTimeMillis();
				}
			}
			handler.postDelayed(GpsFinder, UPDATE_INTERVAL_MS);
		}
	};

	private void requestUpdates() {
		if (locationManager == null) {
			locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		}
		if (locationManager == null || updatesRequested) {
			return;
		}
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e(TAG, "location permission not granted");
			return;
		}
		try {
			String[] providers = {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER};
			for (String provider : providers) {
				if (!locationManager.isProviderEnabled(provider)) {
					continue;
				}
				locationManager.requestLocationUpdates(provider, UPDATE_INTERVAL_MS, UPDATE_MIN_DISTANCE_M, locationListener);
				Location last = locationManager.getLastKnownLocation(provider);
				if (isPlausibleFix(last) && (latestFix == null || last.getTime() > latestFix.getTime())) {
					latestFix = last;
				}
			}
			updatesRequested = true;
		} catch (SecurityException e) {
			Log.e(TAG, "cannot request location updates: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "cannot request location updates: " + e.getMessage());
		}
	}

	private Location usableFix() {
		if (!realFixSeen) {
			return null;
		}
		latestFix = betterFix(latestFix, lastKnownFix());
		return latestFix;
	}

	private Location lastKnownFix() {
		if (locationManager == null) {
			locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		}
		if (locationManager == null) {
			return null;
		}
		Location best = null;
		try {
			String[] providers = {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER};
			for (String provider : providers) {
				if (!locationManager.isProviderEnabled(provider)) {
					continue;
				}
				Location candidate = locationManager.getLastKnownLocation(provider);
				if (isPlausibleFix(candidate)) {
					best = betterFix(best, candidate);
				}
			}
		} catch (SecurityException e) {
			Log.e(TAG, "cannot read last known location: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "cannot read last known location: " + e.getMessage());
		}
		return best;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void insert_place() {
		String url = sh.getString("url", "") + "vehicle_location";
		Log.d(TAG, "posting " + lati + "," + logi
				+ " acc=" + (curLocation != null && curLocation.hasAccuracy() ? curLocation.getAccuracy() : -1f)
				+ " to " + url);

		StringRequest postRequest = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						try {
							JSONObject jsonObj = new JSONObject(response);
							if (jsonObj.getString("status").equalsIgnoreCase("ok")) {
								Log.d(TAG, "location delivered to admin");
							} else {
								Log.e(TAG, "server rejected location: " + response);
							}
						} catch (Exception e) {
							Log.e(TAG, "bad response: " + e.getMessage());
						}
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "upload failed: " + error.toString());
					}
				}
		) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<String, String>();

				params.put("lati", lati);
				params.put("longi", logi);
				params.put("bid", sh.getString("bid", ""));
				params.put("location", place);

				return params;
			}
		};

		int MY_SOCKET_TIMEOUT_MS = 100000;

		postRequest.setRetryPolicy(new DefaultRetryPolicy(
				MY_SOCKET_TIMEOUT_MS,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		if (requestQueue != null) {
			requestQueue.add(postRequest);
		}

	}


}
