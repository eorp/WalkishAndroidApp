package dv606.eo222fw.walkish;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

//walking session activity
public class WalkingMapActivity extends AppCompatActivity implements OnMapReadyCallback {

   private GoogleMap mMap; // Might be null if Google Play services APK is not available.
   //UI elements
   private TextView txtDuration;
   private TextView txtDistance;
   private TextView txtCalories;
   private TextView txtSpeed;
   private Button btnStart;
   private Button btnStop;
   private Button btnPauseResume;
   private ImageButton btnLock;
   //keep track if activity is paused
   private boolean isPaused = false;
   //keep track if buttons are locked
   private boolean isLocked = true;
   //kep track if the activity started
   private boolean isStarted = false;
   //location manager
   private LocationManager locManager;
   //location listener
   private LocationListener myLocationListener;
   //route object
   private Route route = null;
   //list of route coordinates
   private List<Coordinate> coordinates = null;
   //coordinate object
   private Coordinate coordinate = null;
   //starting point location
   private Location startLocation;
   //location service
   private LocationService locationService;
   //intent to start location service
   private Intent serviceIntent;
   //keep track if we are bound to the service
   private boolean serviceBound = false;
   private final String TAG = "walk";
   //duration timer variables
   //duration start time
   private long startTime = 0L;
   private long timeInMilliseconds = 0L;
   private long timeSwapBuff = 0L;
   private long updatedtime = 0L;
   private int secs = 0;
   private int mins = 0;
   private int hrs = 0;
   private Handler handler = new Handler();
   //minimum time between updates in milliseconds
   private static long MIN_TIME = 1000 * 1 * 3; // 3 seconds
   //user's weight (in kg)
   private static int WEIGHT = 60;
   //thread
   private Thread myThread = null;
   //polyline options
   private PolylineOptions polylineOptions;
   //polyline
   private Polyline polyline;
   //list of LatLng objects
   private List<LatLng> path = new ArrayList<LatLng>();
   //broadcast receiver
   private BroadcastReceiver receiver;
   //average speed
   private double avSpeed = 0.0;
   //flag for gps enabled status
   private boolean isGPSEnabled = false;
   //map type
   private int MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL;
   //route colour
   private int ROUTE_COLOUR = Color.BLUE;




   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_walking_map);
      //get reference to UI elements
      btnStart = (Button) findViewById(R.id.btn_start_activity);
      btnStop = (Button) findViewById(R.id.btn_stop);
      btnPauseResume = (Button) findViewById(R.id.btn_pause_resume);
      btnLock = (ImageButton) findViewById(R.id.imgBtn_lock);

      txtDuration = (TextView) findViewById(R.id.txt_duration);
      txtDistance = (TextView) findViewById(R.id.txt_distance);
      txtCalories = (TextView) findViewById(R.id.txt_calories);
      txtSpeed = (TextView) findViewById(R.id.txt_speed);
      //if walking session is not running set duration text to default value
      if(!isStarted)
      txtDuration.setText("00:00:00");
      locManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
      isGPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

      //read map type value from shared preferences
      if(Utils.getSettingsValues("type",Utils.readMapType(this))!=0)
         MAP_TYPE = Utils.getSettingsValues("type",Utils.readMapType(this));
      //read route colour from shared preferences
      if(Utils.getSettingsValues("colour",Utils.readRouteColour(this))!=0)
         ROUTE_COLOUR = Utils.getSettingsValues("colour",Utils.readRouteColour(this));
      //read weight value from shared preferences
      WEIGHT = Utils.readWeight(this);
      //bind to location service
      bindToService();
      //set UI elements visibility
      setVisibility();

   }
   /**
    * connect to the service
    */
   private ServiceConnection serviceConnection = new ServiceConnection(){

      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
         LocationService.LocationBinder binder = (LocationService.LocationBinder)service;
         //get service
         locationService = binder.getService();

         Log.e(TAG, "on service connected, bound is: " + serviceBound + " service: " + locationService + " intent: " + serviceIntent);
      }
      //the following gets called when connection with service gets unexpectidly disconnected
      @Override
      public void onServiceDisconnected(ComponentName name) {
         locationService = null;
         serviceBound = false;
        
      }
   };
   /**
    * bind to service to access its public methods
    */
   private void bindToService()
   {
      Log.e(TAG, "binding to service, bound is: " + serviceBound + " service: " + locationService + " intent: " + serviceIntent);
      //start the music service once the activity starts
      //if intent is not created
      if(serviceIntent==null){
         //create intent
         serviceIntent = new Intent(this, LocationService.class);

      }
      //start service
      startService(serviceIntent);
      //bind to service
      bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
      serviceBound = true;
      Log.e(TAG, "after, bound is: " + serviceBound + " service: " + locationService + " intent: " + serviceIntent);

   }

   /**
    * set click listeners for UI elements
    */
   private void setListeners() {
      btnStart.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {

            //activity started
            isStarted = true;
            //set UI elements visibility
            setVisibility();
            //start tracking
            startTracking();
            //}
         }
      });

      btnLock.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            //if lock is on
            if (isLocked) {
               //set lock off
               isLocked = false;
            } else//otherwise - switch back
               isLocked = true;
            Log.e(TAG, "lock clicked, started: " + isStarted + " locked: " + isLocked);
            //set UI elements visibility
            setVisibility();
         }
      });

      btnPauseResume.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            //check activity paused status and change it
            if (!isPaused) {
               isPaused = true;
               pauseTracking();
            } else {
               isPaused = false;
               resumeTracking();
            }
            Log.e(TAG, "pause clicked, started: " + isStarted + " locked: " + isLocked + " paused: " + isPaused);
            //set UI elements visibility
            setVisibility();
         }
      });

      btnStop.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            //activity stopped - reset flags
            isStarted = false;
            isLocked = true;
            isPaused = false;
            Log.e(TAG, "stop clicked, started: " + isStarted + " locked: " + isLocked + " paused: " + isPaused);
            //set UI elements visibility
            setVisibility();
            //stop tracking
            stopTracking();
            //show history
            Intent intent = new Intent(WalkingMapActivity.this, RoutesHistoryActivity.class);
            startActivity(intent);
         }
      });
   }

   /**
    * set UI elements visibility
    */
   private void setVisibility() {
      Log.e(TAG, "set visible, started: " + isStarted + " locked: " + isLocked + " paused: " + isPaused);
      //if activity is started
      if (isStarted) {//show activity controls
         findViewById(R.id.activity_controls).setVisibility(View.VISIBLE);
         //hide start activty button
         findViewById(R.id.btn_start_activity).setVisibility(View.INVISIBLE);
         //toggle lock button appearance depending on its status
         Utils.toggleLockBtn(btnLock, this, isLocked);
         //toggle pause/resume button appearance depending on paused status
         Utils.togglePauseResumeBtn(this, isPaused, btnPauseResume);
         //change buttons appearance depending on lock and pause status
         Utils.setBtnBgColour(this, isLocked, isPaused, btnStop, btnPauseResume);
      } else {//if activity is stopped or the app is just loaded
         //hide activity controls
         findViewById(R.id.activity_controls).setVisibility(View.INVISIBLE);
         //show start activity button
         findViewById(R.id.btn_start_activity).setVisibility(View.VISIBLE);
      }
      setActionBarVisibility();
   }

   @Override
   protected void onResume() {
      super.onResume();
      if(!serviceBound && isStarted)
         bindToService();
      setActionBarVisibility();
      //get location updates from receiver
      receiver = new BroadcastReceiver(){
         @Override
         public void onReceive(Context context, Intent intent) {
            //Log.e(TAG,"on recieve");
            Bundle b = intent.getExtras();
            final Location loc = (Location)b.get(android.location.LocationManager.KEY_LOCATION_CHANGED);
            //Location loc2 = (Location)b.getParcelable("location");
            //Log.e(TAG, "on recieve, loc:" + loc + " loc2(extra)" + loc2);
               //get corresponding values from service (distance, speed, location)
               final String distance = String.format("%.2f", locationService.getDistance());
               avSpeed = locationService.getAverageSpeed();
               Log.e(TAG,"av speed: "+avSpeed);
               final String speed = String.format("%.2f",locationService.getSpeed());
            final double distanceKm = Double.parseDouble(distance);
            //if location is not null - add new value to list of LatLng objects
               if (loc != null) {
                  path.add(getLatLng(loc));
               }
               //calculate calories based on weight, duration and speed
               final String calories = calculateCalories(distanceKm, txtDuration.getText().toString());
               runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     //update route info on display
                     txtDistance.setText(distance);
                     txtSpeed.setText(speed);
                     txtCalories.setText(calories);

                        updateCamera(loc);
                        updatePolyline(loc);

                  }
               });
         }
      };

      setUpMapIfNeeded();
      setListeners();

      Log.e(TAG, "on resume, bound: " + serviceBound + " started: " + isStarted + " locked: " + isLocked + " paused: " + isPaused);
   }

   @Override
   protected void onStart() {
      super.onStart();
      if(!serviceBound && isStarted)
      bindToService();
      //LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
        //      new IntentFilter(LocationService.LOCATION_UPDATE));
      Log.e(TAG,"on start, bound: "+serviceBound);
   }

   @Override
   protected void onStop() {
      Log.e(TAG, "on stop, bound: " + serviceBound);
      if(serviceBound)
         unbindFromService();
      super.onStop();
      Log.e(TAG, "on stop, bound: " + serviceBound);
   }
   @Override
   public void onBackPressed() {
      moveTaskToBack(true);
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      Log.e(TAG, "on destroy, bound: " + serviceBound);
   }

   @Override
   protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);
      Log.e(TAG, "on new intent, bound: " + serviceBound + " intent: " + intent);
      if(!serviceBound)
         bindToService();
      setActionBarVisibility();
   }
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_session, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();

      //noinspection SimplifiableIfStatement
      if (id == R.id.choose_settings) {
         startActivity(new Intent(this, PreferencesActivity.class));
      } else if (id == R.id.choose_history) {
         startActivity(new Intent(this,RoutesHistoryActivity.class));
      } else if (id == R.id.choose_home) {
         startActivity(new Intent(this,MainActivity.class));
      }

      return super.onOptionsItemSelected(item);

   }

   /**
    * unbind from service
    */
   private void unbindFromService()
   {
      if(serviceBound) {
         unbindService(serviceConnection);
         serviceBound = false;
      }
   }
   /**
    * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
    * installed) and the map has not already been instantiated.. This will ensure that we only ever
    * call {@link #setUpMap()} once when {@link #mMap} is not null.
    * <p/>
    * If it isn't installed {@link SupportMapFragment} (and
    * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
    * install/update the Google Play services APK on their device.
    * <p/>
    * A user can return to this FragmentActivity after following the prompt and correctly
    * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
    * have been completely destroyed during this process (it is likely that it would only be
    * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
    * method in {@link #onResume()} to guarantee that it will be called.
    */
   private void setUpMapIfNeeded() {
      Log.e(TAG, "in setup map if needed, map: " + mMap);

      // Do a null check to confirm that we have not already instantiated the map.
      if (mMap == null) {
         // Try to obtain the map from the SupportMapFragment.
         mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view))
                 .getMap();
         //Map Settings
         mMap.getUiSettings().setMyLocationButtonEnabled(true);
         mMap.getUiSettings().setCompassEnabled(true);
         mMap.getUiSettings().setZoomControlsEnabled(true);
         //map type
         mMap.setMapType(MAP_TYPE);
         // Check if we were successful in obtaining the map.
         if (mMap != null) {
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
               @Override
               public void onMapLoaded() {
                  Log.e(TAG, "map loaded");
                  setUpMap();
               }
            });
            //setUpMap();

         }
      }
   }

   /**
    * initialise map to reflect current position
    */
   private void setUpMap() {
//      Log.e(TAG, "in setup map, bound: " + serviceBound + " gps: " + locationService.isGPSEnabled());
      //initialise polyline options
      polylineOptions = new PolylineOptions();
      //path colour from shared preferences
      polylineOptions.color(ROUTE_COLOUR).width(4);

         //check if GPS is enabled on the device
         if (!isGPSEnabled) {
            //if GPS is off - show warning
            gpsOffAlert();
         }
         else {//if GPS is on
            //get current location
            Location location = getLastKnownLocation();
            Log.e(TAG, "in setup map, location: " + location);
            //if such location exists/registered - update camera position
            if (location != null) {
               mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getLatLng(location), 16));

               Log.e(TAG, "in setup map, found last location, location: " + location);
               mMap.setMyLocationEnabled(true);

               //set route start location
               setStartLocation(location);
               //draw initial polyline
               polyline = mMap.addPolyline(polylineOptions.add(getLatLng(location)));
               //add first element to list of LatLng objects
               if(path.size()==0)
                  path.add(getLatLng(location));
            }
         }
      //}

   }

   /**
    * show alert dialog if gps is off
    */
   public void gpsOffAlert(){
      AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      // Setting Dialog Title
      alertDialog.setTitle("GPS is off");

      // alert dialog message
      alertDialog.setMessage("GPS is off. This application requires enabled GPS to function. Do you want to go to settings menu and activate GPS?");

      // open settings menu on button press
      alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog,int which) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            WalkingMapActivity.this.startActivity(intent);
         }
      });

      // on pressing cancel button
      alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            finish();
            //context.startActivity(new Intent(context,MainActivity.class));
         }
      });

      //show alert
      alertDialog.show();
   }
   /**
    * get last known location quering all available providers
    * @return location
    */
   private Location getLastKnownLocation() {
      //locManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
      List<String> providers = locManager.getProviders(true);
      Location bestLocation = null;
      for (String provider : providers) {

         Location loc = locManager.getLastKnownLocation(provider);
         Log.e(TAG,"provider found: "+provider+" location: "+loc);
         if (loc == null) {
            continue;
         }
         if (bestLocation == null || loc.getAccuracy() < bestLocation.getAccuracy()) {
            // Found best last known location: %s", l);
            bestLocation = loc;
         }
      }
      return bestLocation;
   }
   @Override
   public void onMapReady(GoogleMap googleMap) {
      Log.e(TAG, "on map ready ");

      mMap = googleMap;

      setUpMap();
   }


   /**
    * start walking activity tracking
    */
   private void startTracking(){
      //ensure we are bound to service
      if(!serviceBound)
         bindToService();
      //get time now
      String routeStartTime = Utils.getCurrentTimeStringShort();
         //instantiate route object
         route = new Route();
         //get today's date in different formats (long, short)
         String routeName = Utils.getFullDateString();
         String routeDate = Utils.getShortDateString();
         //set corresponding route values
         route.setName(routeName);
         route.setDate(routeDate);
         route.setStartTime(routeStartTime);
         //instantiate coordinates list
         coordinates = new ArrayList<Coordinate>();
      //if there is a starting point for the route
         if (getStartLocation() != null) {
            //add that location to list of coordinates
            addPoint(getStartLocation());
         }
         if (coordinates.size() > 0) {
            route.setStartPoint(coordinates.get(0));
         }
      //double check that we are bound to the service
         if (serviceBound) {
            //start tracking
            locationService.startTracking(isStarted);
         }
      //start timer displaying session duration
         startTimer();
      //register receiever for location updates
         LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                 new IntentFilter(LocationService.LOCATION_UPDATE));

   }

   /**
    * stop tracking
    */
   private void stopTracking(){
      //stop timer
      stopTimer();
      //get session duration
      String routeDuration = txtDuration.getText().toString();
      //get finish time
      String routeFinishTime = Utils.getCurrentTimeStringShort();
 //     Log.e(TAG,"stopping session, coordinate size: "+coordinates.size());

      //set route duration and finish time
      route.setFinishTime(routeFinishTime);
      route.setDuration(routeDuration);
      route.setDistance(txtDistance.getText().toString());
      route.setCalories(txtCalories.getText().toString());
      //get reference to db
      RoutesDbHelper.init(WalkingMapActivity.this);
      //insert route object to db
      long routeId = RoutesDbHelper.createRoute(route);
      Log.e(TAG, "route id: " + routeId);
      //read list of coordinates for the route
         coordinates = locationService.getCoordinates();
      //if we have at least two coordinates for the route
      if(coordinates.size()>1) {
         //set start and finish point from coordinates list
         route.setFinishPoint(coordinates.get(coordinates.size() - 1));
         route.setStartPoint(coordinates.get(0));
      }//if there is only one coordinate for the route
      else if(coordinates.size()==1) {
         //set start and finish points
         route.setFinishPoint(coordinates.get(0));
         route.setStartPoint(coordinates.get(0));
      }
         //insert list of coordinates to db
         RoutesDbHelper.createCoordinates(coordinates, routeId);
      //}
      //stop tracking
      locationService.stopTracking(isStarted);
      //Log.e(TAG, "route: " + route);
      //Log.e(TAG,"coordinates: "+coordinates);
      mMap.setMyLocationEnabled(false);
      //if bound to service
      if(serviceBound){
         //stop service and unbind
         stopService(serviceIntent);
         unbindService(serviceConnection);
         serviceBound = false;
      }
      //unregister receiever
      LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);


   }

   /**
    * pause tracking
    */
   private void pauseTracking(){
      //pause tracking and pause duration timer
      if(serviceBound){
         locationService.pauseTracking(isPaused);
      }
      pauseTimer();

   }

   /**
    * resume session tracking
    */
   private void resumeTracking(){
      //resume session tracking
      if(serviceBound) {
         locationService.resumeTracking(isPaused);
      }
      //resume duration timer
      startTimer();

   }
   public Location getStartLocation() {
      return startLocation;
   }

   public void setStartLocation(Location startLocation) {
      this.startLocation = startLocation;
   }

   /**
    * add new location point coordonate to the list
    * @param location current location
    */
   private void addPoint(Location location){
      //provided location is not null
      if(location!=null) {
         //add new coordinate to the list of coordinates
         Coordinate point = new Coordinate(location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getSpeed());
         coordinates.add(point);
      }
   }

   /**
    * runnable class for updating duration timer every second
    */
   public Runnable updateTimer = new Runnable() {

      public void run() {
         //get time in millisecs
         timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
         //handle resumed activity time
         updatedtime = timeSwapBuff + timeInMilliseconds;
         //extract hrs, mins,secs from time in millisecs
         secs = (int) (updatedtime / 1000);
         mins = secs / 60;
         secs = secs % 60;
         hrs = mins/60;
         //update text every second
         txtDuration.setText(String.format("%02d:%02d:%02d",hrs,mins, secs));
         handler.postDelayed(this, 1000);
      }

   };

   /**
    * start duration timer
    */
    private void startTimer(){
       //initialise duration start and update every second
       startTime = SystemClock.uptimeMillis();
       handler.postDelayed(updateTimer, 1000);
    }

   /**
    * stop duration timer
    */
   private void stopTimer(){
      //reset timer values
      startTime = 0L;
      timeInMilliseconds = 0L;
      timeSwapBuff = 0L;
      updatedtime = 0L;
      secs = 0;
      mins = 0;
      hrs = 0;
      //stop updating every second
      handler.removeCallbacks(updateTimer);
   }

   /**
    * pause duration timer
    */
   private void pauseTimer(){
      //get current time and save it
      timeSwapBuff += timeInMilliseconds;
      //stop updating
      handler.removeCallbacks(updateTimer);
   }

   /**
    * calculate estimation of calories burnt based on weight, speed and duration
    * followed the formula from http://www.shapesense.com/javascripts/walking-calorie-burn-calculator.js
    * @param km distance
    * @param duration
    * @return calories (in Cal) estimation
    */
   public static String calculateCalories(double km, String duration) {
      //speed in meters per minute
      //double metersPerMin = speed / 0.06;

      //duration in hours
      double hrs = getHoursFromDurationString(duration);
      double speed = km/hrs;
      double calPerKgPerHr = 0.0215*Math.pow(speed,3)-0.1765*Math.pow(speed,2)+0.871*speed+1.4577;
      double calories = Math.round(calPerKgPerHr*WEIGHT*hrs);
      return String.format("%.2f",calories);
   }

   /**
    * get hours value from current duration string
    * @param duration
    * @return hours
    */
   public static double getHoursFromDurationString(String duration){
      //split duration string to extract hrs,mins and secs
      String[] split = duration.split(":");
      int min = Integer.parseInt(split[1]);
      int sec = Integer.parseInt(split[2]);
      int hrs = Integer.parseInt(split[0]);

      return (double)(min + (sec/60))/60+hrs;
   }

   /**
    * update polyline to include new location to the route path
     * @param location
    */
   private void updatePolyline(Location location){
      polyline.setPoints(path);
      Log.e(TAG, "setting points with loc: " + path);
   }

   /**
    * update camera position to reflect new location
    * @param location
    */
   private void updateCamera(Location location){
      Log.e(TAG, "updating camera with loc: " + location);
      LatLngBounds.Builder builder = new LatLngBounds.Builder();
      //include route points to the boundary builder
      for (LatLng latLng : path) {
         builder.include(latLng);
      }
      mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
   }

   /**
    * get LatLng object from location
    * @param location
    * @return LatLng object
    */
   private LatLng getLatLng(Location location){
      return new LatLng(location.getLatitude(), location.getLongitude());

   }
   public Coordinate getStartPoint(){
      return new Coordinate(getStartLocation().getLatitude(),getStartLocation().getLongitude());

   }

   /**
    * handle action bar visibility
    * if session started - hide
    * if session finished - show
    */
   private void setActionBarVisibility(){
      if(isStarted)
         getSupportActionBar().hide();
      else
         getSupportActionBar().show();
   }

}
