package com.example.kidcommute;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parent side of live tracking.
 *
 * Flow:
 *   driver app  --POST lati/longi/bid-->  server/vehicle_location   [LocationService]
 *   this screen --GET bus_locations----->  server (assigned vehicle = bid from login)
 *                --GET bus_trail/<bid>-->  server (recent route points)
 *                --GET reverse_geocode-->  server (place name when DB has none)
 *
 * Polls every 10 s while the screen is open and renders: current place,
 * "updated x ago", LIVE/STALE pill (using the server's own live window),
 * the last 5 route areas as an animated line (RouteLineView), and an
 * "open in Maps" shortcut.
 */
public class track_bus extends AppCompatActivity {

    private static final String TAG = "TrackBus";
    private static final long POLL_INTERVAL_MS = 10000;   // refresh every 10 s
    private static final long REQUEST_TIMEOUT_MS = 10000;
    private static final long DEFAULT_LIVE_WINDOW_MS = 120000; // server live_seconds
    private static final int MAX_ROUTE_POINTS = 5;    // areas shown
    private static final int MAX_STORE = 60;          // raw points kept
    private static final float SAME_PLACE_METERS = 30f;   // within 30 m = same location

    private static final int COLOR_LIVE = 0xFF1B5E20;
    private static final int COLOR_STALE = 0xFFC62828;
    private static final int COLOR_WAITING = 0xFF6D5A00;

    private final Handler handler = new Handler();
    private RequestQueue requestQueue;

    private TextView btnBack, btnOpenMap, tvLive, tvCurrentName, tvUpdated, tvEmpty;
    private RouteLineView routeLine;
    private View pulse;
    private ValueAnimator pulseAnimator;

    private String baseUrl = "", bid = "", pid = "";
    private boolean polling = false;
    private boolean liveFetchFailed = false;   // cannot reach the server
    private boolean assignedMissing = false;   // server up, but no vehicle for this account

    private Point latest;                                    // newest known position
    private final List<Point> route = new ArrayList<>();     // newest first, <= MAX_STORE

    private long lastFixMs = 0;           // time of the last fix, even if the row wasn't updated

    private String busLabel = "";                            // "Bus 0001"
    private long liveWindowMs = DEFAULT_LIVE_WINDOW_MS;
    private long serverAgeMs = -1;                           // age reported by server
    private long serverAgeAt = 0;                            // local time of that report
    private int busAgeSeconds = -1;                          // bus_locations age_seconds

    private boolean placeLookupRunning = false;
    private final Map<String, String[]> placeCache = new HashMap<>();   // cell -> {areaKey, label}

    // ---------------------------------------------------------------- model

    private static class Point {
        final double lat, lng;
        final String place;     // may be null/empty
        final long timeMs;      // server timestamp, 0 = unknown
        final long receivedAt;  // local arrival time

        Point(double lat, double lng, String place, long timeMs) {
            this.lat = lat;
            this.lng = lng;
            this.place = place;
            this.timeMs = timeMs;
            this.receivedAt = System.currentTimeMillis();
        }

        long displayTime() {
            return timeMs != 0 ? timeMs : receivedAt;
        }

        String coords() {
            return String.format(Locale.US, "%.5f, %.5f", lat, lng);
        }

        String key() {
            return String.format(Locale.US, "%.3f,%.3f", lat, lng);   // ~110 m grid
        }
    }

    // ------------------------------------------------------------ lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_bus);

        btnBack = findViewById(R.id.btnBack);
        btnOpenMap = findViewById(R.id.btnOpenMap);
        tvLive = findViewById(R.id.tvLive);
        tvCurrentName = findViewById(R.id.tvCurrentName);
        tvUpdated = findViewById(R.id.tvUpdated);
        tvEmpty = findViewById(R.id.tvEmpty);
        routeLine = findViewById(R.id.routeLine);
        pulse = findViewById(R.id.pulse);

        baseUrl = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("url", "");
        bid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("bid", "");
        pid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("pid", "");

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInMaps();
            }
        });

        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPulse();
        if (baseUrl.length() == 0) {
            liveFetchFailed = true;
            render();
            return;
        }
        polling = true;
        handler.removeCallbacks(pollRunnable);
        handler.post(pollRunnable);   // first fetch right away
    }

    @Override
    protected void onPause() {
        super.onPause();
        polling = false;
        handler.removeCallbacks(pollRunnable);
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
        stopPulse();
    }

    @Override
    protected void onDestroy() {
        stopPulse();
        super.onDestroy();
    }

    // -------------------------------------------------------------- polling

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!polling) {
                return;
            }
            fetchAssignedBus();
            fetchTrail();
            handler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    private int assignedBid() {
        try {
            return Integer.parseInt(bid.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    /** Latest position + status of this parent's assigned vehicle. */
    private void fetchAssignedBus() {
        String url = baseUrl + "bus_locations?bid=" + Uri.encode(bid) + "&pid=" + Uri.encode(pid);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject root = new JSONObject(response);
                            Point p = pickAssignedBus(root);
                            int window = root.optInt("live_seconds", (int) (DEFAULT_LIVE_WINDOW_MS / 1000));
                            if (window > 0) {
                                liveWindowMs = window * 1000L;
                            }
                            if (p != null) {
                                liveFetchFailed = false;
                                assignedMissing = false;
                                applyLive(p);
                                Log.d(TAG, "live: " + p.lat + "," + p.lng
                                        + " age=" + serverAgeMs + "ms bus=" + busLabel);
                            } else {
                                assignedMissing = true;
                                Log.w(TAG, "no assigned vehicle in bus_locations (bid=" + bid + ")");
                            }
                            render();
                        } catch (Exception e) {
                            Log.e(TAG, "bad bus_locations payload: " + e);
                            liveFetchFailed = true;
                            render();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "bus_locations failed: " + error);
                        liveFetchFailed = true;
                        render();
                    }
                });
        add(request);
    }

    /** Recent trail of the assigned vehicle, newest first (server returns oldest first). */
    private void fetchTrail() {
        int id = assignedBid();
        if (id < 0) {
            return;
        }
        String url = baseUrl + "bus_trail/" + id;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject root = new JSONObject(response);
                            JSONArray trail = root.optJSONArray("trail");
                            List<Point> points = new ArrayList<>();
                            if (trail != null) {
                                for (int i = 0; i < trail.length(); i++) {
                                    Object item = trail.opt(i);
                                    if (item instanceof JSONObject) {
                                        Point p = pointFrom((JSONObject) item);
                                        if (p != null) {
                                            points.add(p);   // ascending order
                                        }
                                    }
                                }
                            }
                            if (!points.isEmpty()) {
                                List<Point> newestFirst = new ArrayList<>(points);
                                Collections.reverse(newestFirst);

                                List<Point> deduped = new ArrayList<>();
                                for (Point p : newestFirst) {
                                    addDeduped(deduped, p);       // collapses repeated locations
                                }
                                while (deduped.size() > MAX_STORE) {
                                    deduped.remove(deduped.size() - 1);
                                }

                                route.clear();
                                route.addAll(deduped);

                                if (latest == null) {
                                    latest = route.get(0);
                                } else if (latest.displayTime() >= route.get(0).displayTime()
                                        && !isSameLocation(latest, route.get(0))) {
                                    route.add(0, latest);
                                    while (route.size() > MAX_STORE) {
                                        route.remove(route.size() - 1);
                                    }
                                }
                                Log.d(TAG, "trail: " + points.size() + " point(s), showing " + route.size());
                                render();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "bad bus_trail payload: " + e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "bus_trail failed: " + error);
                    }
                });
        add(request);
    }

    private void add(StringRequest request) {
        request.setTag(TAG);
        request.setRetryPolicy(new DefaultRetryPolicy((int) REQUEST_TIMEOUT_MS, 1, 1f));
        requestQueue.add(request);
    }

    private void applyLive(Point p) {
        if (busAgeSeconds >= 0) {
            serverAgeMs = busAgeSeconds * 1000L;   // server-computed, no clock skew
        } else {
            serverAgeMs = p.timeMs != 0
                    ? Math.max(0, System.currentTimeMillis() - p.timeMs)
                    : -1;
        }
        serverAgeAt = System.currentTimeMillis();
        lastFixMs = p.displayTime();

        if (route.isEmpty() || !isSameLocation(route.get(0), p)) {
            route.add(0, p);                         // bus moved: new row
            while (route.size() > MAX_STORE) {
                route.remove(route.size() - 1);
            }
            latest = p;
        } else {
            latest = route.get(0);                   // same spot: don't add or change the row
        }
    }

    /** Same spot = within SAME_PLACE_METERS. */
    private boolean isSameLocation(Point a, Point b) {
        if (a == null || b == null) {
            return false;
        }
        float[] r = new float[1];
        Location.distanceBetween(a.lat, a.lng, b.lat, b.lng, r);
        return r[0] < SAME_PLACE_METERS;
    }

    /** Adds p unless it is the same location as the last item; then keeps the older (arrival) point. */
    private void addDeduped(List<Point> out, Point p) {
        if (!out.isEmpty() && isSameLocation(out.get(out.size() - 1), p)) {
            out.set(out.size() - 1, p);
        } else {
            out.add(p);
        }
    }

    /** Finds this parent's assigned vehicle (bid) inside the bus_locations payload. */
    private Point pickAssignedBus(JSONObject root) {
        JSONArray buses = root.optJSONArray("buses");
        if (buses == null) {
            // a backend that answers with a single fix directly
            Point direct = pointFrom(root);
            return direct;
        }
        int want = assignedBid();
        for (int i = 0; i < buses.length(); i++) {
            Object item = buses.opt(i);
            if (!(item instanceof JSONObject)) {
                continue;
            }
            JSONObject bus = (JSONObject) item;
            int id = bus.optInt("id", -1);
            if (id < 0) {
                String raw = bus.optString("id", "");
                try {
                    id = Integer.parseInt(raw.trim());
                } catch (Exception ignored) {
                }
            }
            if (want >= 0 && id != want) {
                continue;
            }
            Point p = pointFrom(bus);
            if (p == null) {
                continue;
            }
            busAgeSeconds = bus.optInt("age_seconds", -1);
            String number = firstString(bus, "bus_number", "bus_no", "number");
            String name = firstString(bus, "bus_name", "name");
            busLabel = number.length() > 0 ? "Bus " + number
                    : (name.length() > 0 ? name : "");
            return p;
        }
        return null;
    }

    // ------------------------------------------------------------- parsing

    private Point pointFrom(JSONObject o) {
        if (o == null) {
            return null;
        }
        double lat = doubleVal(o, "lati", "lat", "latitude", "lat_value");
        double lng = doubleVal(o, "longi", "lng", "lon", "longitude", "long_value");
        if (Double.isNaN(lat) || Double.isNaN(lng)) {
            return null;
        }
        if (lat == 0d && lng == 0d) {
            return null;
        }
        if (Math.abs(lat) > 90d || Math.abs(lng) > 180d) {
            return null;
        }
        String place = firstString(o, "location", "place", "address", "area", "landmark");
        long time = timeVal(o, "date_time", "updated_at", "datetime", "time",
                "timestamp", "created_at", "date");
        return new Point(lat, lng, place, time);
    }

    private double doubleVal(JSONObject o, String... keys) {
        for (String key : keys) {
            Object v = o.opt(key);
            if (v instanceof Number) {
                return ((Number) v).doubleValue();
            }
            if (v instanceof String) {
                try {
                    String s = ((String) v).trim().replace(',', '.');
                    if (s.length() > 0) {
                        return Double.parseDouble(s);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return Double.NaN;
    }

    private String firstString(JSONObject o, String... keys) {
        for (String key : keys) {
            Object v = o.opt(key);
            if (v != null && v != JSONObject.NULL) {
                String s = String.valueOf(v).trim();
                if (s.length() > 0 && !"null".equalsIgnoreCase(s)) {
                    return s;
                }
            }
        }
        return "";
    }

    private long timeVal(JSONObject o, String... keys) {
        long now = System.currentTimeMillis();
        for (String key : keys) {
            Object v = o.opt(key);
            if (v == null || v == JSONObject.NULL) {
                continue;
            }
            long t = 0;
            if (v instanceof Number) {
                t = ((Number) v).longValue();
            } else {
                String s = String.valueOf(v).trim();
                try {
                    t = Long.parseLong(s);
                } catch (NumberFormatException notNumeric) {
                    String[] formats = {
                            "yyyy-MM-dd HH:mm:ss",
                            "yyyy-MM-dd HH:mm:ss.SSS",
                            "yyyy-MM-dd'T'HH:mm:ss",
                            "dd-MM-yyyy HH:mm:ss",
                            "dd/MM/yyyy HH:mm:ss",
                    };
                    for (String f : formats) {
                        try {
                            t = new SimpleDateFormat(f, Locale.US).parse(s).getTime();
                            break;
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            if (t <= 0) {
                continue;
            }
            if (t < 100000000000L) {           // epoch seconds -> ms
                t = t * 1000L;
            }
            // reject clock-skewed / absurd values, otherwise trust the server
            if (t > now + 10 * 60 * 1000L || t < now - 365L * 24 * 3600 * 1000L) {
                continue;
            }
            return t;
        }
        return 0;
    }

    // ------------------------------------------------------------ ui state

    private long effectiveAgeMs() {
        if (latest == null) {
            return -1;
        }
        if (serverAgeMs >= 0) {
            return serverAgeMs + (System.currentTimeMillis() - serverAgeAt);
        }
        long ref = lastFixMs > 0 ? lastFixMs : latest.displayTime();
        return Math.max(0, System.currentTimeMillis() - ref);
    }

    private void render() {
        if (latest == null) {
            tvCurrentName.setText("Locating bus…");
            if (assignedMissing) {
                tvUpdated.setText("No vehicle is assigned to this account yet.");
                setPill("○ WAITING", COLOR_WAITING);
            } else if (liveFetchFailed) {
                tvUpdated.setText("Couldn't reach the tracking server. Check the server IP.");
                setPill("○ OFFLINE", COLOR_STALE);
            } else if (baseUrl.length() == 0) {
                tvUpdated.setText("Server address not configured.");
                setPill("○ OFFLINE", COLOR_STALE);
            } else {
                tvUpdated.setText("Waiting for the driver to share a location…");
                setPill("○ WAITING", COLOR_WAITING);
            }
        } else {
            long age = effectiveAgeMs();
            tvCurrentName.setText(displayNameFor(latest));
            String updated = "Updated " + relative(age);
            if (busLabel.length() > 0) {
                updated = busLabel + " · " + updated;
            }
            tvUpdated.setText(updated);
            if (age <= liveWindowMs) {
                setPill("● LIVE", COLOR_LIVE);
            } else {
                setPill("● STALE", COLOR_STALE);
            }
        }

        btnOpenMap.setAlpha(latest == null ? 0.45f : 1f);

        // build the unique list: one row per area, newest first
        List<String> shownKeys = new ArrayList<>();
        List<String> shownNames = new ArrayList<>();
        List<Long> shownTimes = new ArrayList<>();

        for (Point p : route) {
            String[] info = placeInfo(p);
            if (info == null) {
                continue;                    // not resolved yet
            }
            int idx = shownKeys.indexOf(info[0]);
            if (idx >= 0) {
                if (idx == shownKeys.size() - 1) {
                    shownTimes.set(idx, p.displayTime());  // keep the arrival time
                }
                continue;                                  // never print an area twice
            }
            if (shownKeys.size() >= MAX_ROUTE_POINTS) {
                break;
            }
            shownKeys.add(info[0]);
            shownNames.add(info[1]);
            shownTimes.add(p.displayTime());
        }

        tvEmpty.setVisibility(shownNames.isEmpty() ? View.VISIBLE : View.GONE);
        routeLine.setStops(shownNames, shownTimes);

        Log.d(TAG, "render: [" + tvLive.getText() + "] " + tvCurrentName.getText()
                + " | " + tvUpdated.getText() + " | stops=" + routeLine.getStopCount());
    }

    private void setPill(String text, int color) {
        tvLive.setText(text);
        tvLive.setTextColor(color);
    }

    private String relative(long ageMs) {
        if (ageMs < 0) {
            return "just now";
        }
        if (ageMs < 15000) {
            return "just now";
        }
        if (ageMs < 60000) {
            return (ageMs / 1000) + " s ago";
        }
        if (ageMs < 3600000L) {
            return (ageMs / 60000) + " min ago";
        }
        if (ageMs < 86400000L) {
            return (ageMs / 3600000L) + " h ago";
        }
        return (ageMs / 86400000L) + " d ago";
    }

/**
 * Place name for a point: area label once reverse_geocode has answered,
 * otherwise the server value, otherwise coordinates. The lookup is started
 * here because the server's own place name is often a whole stretch
 * ("Kollam - Theni Highway") that every point along it shares.
 */
    private String displayNameFor(Point p) {
        String[] info = placeInfo(p);
        if (info != null) {
            return info[1];
        }
        if (p.place != null && p.place.trim().length() > 0) {
            return p.place.trim();
        }
        return p.coords();
    }

    /** {areaKey, label} once the area is known, otherwise null (still looking up). */
    private String[] placeInfo(Point p) {
        String[] c = placeCache.get(p.key());
        if (c == null) {
            maybeResolvePlace(p);
            return null;
        }
        return c[1].length() > 0 ? c : null;      // lookup failed -> null
    }

    private void maybeResolvePlace(final Point p) {
        if (placeLookupRunning || baseUrl.length() == 0) {
            return;
        }
        final String key = p.key();
        if (placeCache.containsKey(key)) {
            return;
        }
        placeLookupRunning = true;
        String url = baseUrl + "reverse_geocode?lat=" + p.lat + "&lng=" + p.lng;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        placeLookupRunning = false;
                        String[] info = new String[]{"", ""};
                        try {
                            info = buildPlaceInfo(new JSONObject(response));
                            Log.d(TAG, "place for " + key + ": " + info[1]
                                    + "  raw=" + response);
                        } catch (Exception e) {
                            Log.d(TAG, "reverse_geocode payload: " + e);
                        }
                        placeCache.put(key, info);
                        render();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        placeLookupRunning = false;
                        placeCache.put(key, new String[]{"", ""});
                        Log.d(TAG, "reverse_geocode failed: " + error);
                        render();
                    }
                });
        add(request);
    }

    /** Builds {areaKey, label} such as {"fathimapuram", "Fathimapuram, Changanassery"}. */
    private String[] buildPlaceInfo(JSONObject root) {
        JSONObject addr = root.optJSONObject("address");
        JSONObject src = addr != null ? addr : root;

        String area = firstString(src, "neighbourhood", "suburb", "sublocality",
                "locality", "hamlet", "village", "quarter", "city_district", "area");
        String town = firstString(src, "town", "city", "municipality");
        String full = firstString(root, "place", "full", "name", "display_name");

        // no structured fields: use the comma parts of the full text, "Road, Area"
        if (area.length() == 0 && full.length() > 0) {
            String[] parts = full.split(",");
            area = parts.length > 1 ? parts[1].trim() : parts[0].trim();
        }
        if (area.length() == 0) {
            return new String[]{"", ""};
        }
        String label = (town.length() > 0 && !town.equalsIgnoreCase(area))
                ? area + ", " + town : area;
        return new String[]{area.toLowerCase(Locale.ROOT), label};
    }

    // ------------------------------------------------------------- actions

    private void openInMaps() {
        if (latest == null) {
            Toast.makeText(getApplicationContext(),
                    "Waiting for the bus location…", Toast.LENGTH_SHORT).show();
            return;
        }
        String geo = String.format(Locale.US, "geo:%f,%f?q=%f,%f",
                latest.lat, latest.lng, latest.lat, latest.lng);
        try {
            Intent gmm = new Intent(Intent.ACTION_VIEW, Uri.parse(geo));
            gmm.setPackage("com.google.android.apps.maps");
            startActivity(gmm);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(geo)));
            } catch (Exception e2) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "https://www.google.com/maps/search/?api=1&query="
                                    + latest.lat + "," + latest.lng)));
                } catch (Exception e3) {
                    Toast.makeText(getApplicationContext(),
                            "No maps app found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // -------------------------------------------------------------- visuals

    private void startPulse() {
        if (pulse == null || pulseAnimator != null) {
            return;
        }
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(1100);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (Float) animation.getAnimatedValue();
                float scale = 1f + 0.35f * f;
                pulse.setScaleX(scale);
                pulse.setScaleY(scale);
                pulse.setAlpha(1f - 0.7f * f);
            }
        });
        pulseAnimator.start();
    }

    private void stopPulse() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
    }
}
