package dv606.eo222fw.walkish;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evgeniya O'Regan Pevchikh on 27/01/2016.
 */
public class LocationService extends Service implements LocationListener {
   // flag for GPS status
   private boolean isGPSEnabled = false;
   // flag for network status
   private boolean isNetworkEnabled = false;
   // flag for location accessability status
   private boolean locationAvailable = false;
   //location manager
   private LocationManager locationManager;
   //current location
   private Location currentLocation = null;
   //previous location
   private Location previousLocation = null;
   //binder
   private final IBinder locationBind = new LocationBinder();
   //list of coordinates
   private List<Coordinate> coordinates;
   //distance
   private double distance = 0.0;
   //flag for tracking in progress
   private boolean isTracking = false;
   //flag for tracking paused
   private boolean isTrackingPaused = false;
   //minimum distance to change for updates in meters
   private static long MIN_DISTANCE = 0; // 0 meters
   //minimum time between updates in milliseconds
   private static long MIN_TIME = 1000 * 1 * 3; // 3 seconds
   //speed between two location
   private double speed = 0.0;
   //notification id
   private static final int NOTIFY_ID=134;
   private final String TAG = "service";
   //local broadcast manager
   private LocalBroadcastManager broadcaster;
   //two minutes variable in milliseconds
   private static final int TWO_MINUTES = 1000 * 60 * 2;

   public static final String LOCATION_UPDATE = "NEW_LOCATION_INFO";


   @Override
   public void onCreate() {
      locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
      coordinates = new ArrayList<Coordinate>();

      isGPSEnabled = locationManager
              .isProviderEnabled(LocationManager.GPS_PROVIDER);

      // getting network status
      isNetworkEnabled = locationManager
              .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
      //if(!isGPSEnabled)
         //gpsOffAlert();
      //get start location
      currentLocation = getLastKnownLocation();
      if(currentLocation!=null) {
         //add start location to list of coordinates
         addPoint(currentLocation);
      }
      //read location update intervals from shared preferences
      getIntervals(Utils.readGPSSettings(this));
      Log.e(TAG, "on create, time: "+MIN_TIME+", distance: "+MIN_DISTANCE);
      broadcaster = LocalBroadcastManager.getInstance(this);
   }
   public class LocationBinder extends Binder {
      LocationService getService() {
         return LocationService.this;
      }
   }

   @Override
   public boolean onUnbind(Intent intent) {
      return super.onUnbind(intent);
   }

   @Override
   public void onLocationChanged(Location location) {
      Log.e(TAG,"location changed, speed: "+getSpeed(currentLocation,location));
      //check for reasonable speed (in case of huge difference between two locations)
      //expected walking speed is somewhere between 1.3 to 2.5 m/s
      //if speed is ok
      //if(Utils.isWithinRange(1.3,2.5,getSpeed(currentLocation,location))) {
         //if new location is better than the current location that we got before location change update
         if (isBetterLocation(location, currentLocation)) {
            //assign outdated location as a previous point
            previousLocation = currentLocation;
            //assign new location as current location
            currentLocation = location;
            //add new location to the list
            addPoint(currentLocation);
            //get total distance so far
            distance += calculateDistance(previousLocation, currentLocation);
            //if speed from location object is null - calculate speed manually
            //based on distance and time between two locations
            if (currentLocation.getSpeed() == 0) {
               //get speed manually in km/h
               speed = getSpeed(previousLocation,currentLocation)*3.6;
               Log.e(TAG, "location changed, manual speed: " + speed);
               //set speed value to the last coordinate added to the list
               coordinates.get(getLast()).setSpeed(speed);
            }
         }


         Log.e(TAG, "location changed, location: " + location + " distance: " + distance);
         //send broadcast on location update
         Intent intentLocation = new Intent(LOCATION_UPDATE);
         intentLocation.putExtra("location", location);
         broadcaster.sendBroadcast(intentLocation);
      //}
   }

   /**
    * get speed in meter per seconds
    * @param previous location
    * @param current location
    * @return speed in m/s
    */
   private double getSpeed(Location previous, Location current){
      double speed;
      //get time between two locations
      long seconds = (current.getTime() - previous.getTime()) / 1000L;
      double distance = calculateDistance(previous,current);
      speed = distance/seconds;
      return speed;
   }
   @Override
   public void onStatusChanged(String s, int i, Bundle bundle) {
         Log.e(TAG,"loc listener, ");
   }

   @Override
   public void onProviderEnabled(String s) {

   }

   @Override
   public void onProviderDisabled(String s) {

   }

   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return locationBind;
   }


   /**
    * get last known location quering all available providers
    * @return location
    */
   public Location getLastKnownLocation() {
      //locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
      List<String> providers = locationManager.getProviders(true);
      // getting GPS status
      isGPSEnabled = locationManager
              .isProviderEnabled(LocationManager.GPS_PROVIDER);

      // getting network status
      isNetworkEnabled = locationManager
              .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
      Location bestLocation = null;
      for (String provider : providers) {

         Location loc = locationManager.getLastKnownLocation(provider);
         Log.e(TAG, "provider found: " + provider + " location: " + loc);
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

   /**
    * check if gps is enabled on device
    * @return true if enabled, false otherwise
    */
   public boolean isGPSEnabled() {
      return isGPSEnabled;
   }

   /**
    * get current location
    * @return
    */
   public Location getCurrentLocation() {
      return currentLocation;
   }

   /**
    * register location update listener
    * @param provider location provider
    * @param intervalTime minimum time interval between updates
    * @param intervalDistance minimum distance interval between updates
    */
   private void startLocationListening(String provider, long intervalTime, float intervalDistance)
   {  //start listening to location updates with specified parameters
      locationManager.requestLocationUpdates(provider, intervalTime, intervalDistance, this);
      Log.e(TAG,"registered for location updates, provider: "+provider);

   }

   /**
    * stop listening to location updates
    */
   private void stopLocationListening(){
      locationManager.removeUpdates(this);
      locationManager.removeGpsStatusListener(mStatusListener);
      Log.e(TAG, "unregistered from location updates");
   }

   /**
    * start tracking
    * @param isStarted
    */
   public void startTracking(boolean isStarted)
   {
      Log.e(TAG, "started tracking, gps is: " + isGPSEnabled);
      isTracking = isStarted;
      //check if location provider are enabled, start listening for location changes and show notification
      startListening();
   }

   /**
    * stop tracking
    * @param isStarted
    */
   public void stopTracking(boolean isStarted){
      Log.e(TAG,"stopped tracking");
      isTracking = isStarted;
      //unregister location updates listener
      stopLocationListening();
      stopForeground(true);
   }

   /**
    * pause tracking
    * @param paused
    */
   public void pauseTracking(boolean paused){
      isTrackingPaused = paused;
      //unregister location updates listener
      stopLocationListening();
      Log.e(TAG, "paused tracking, cur loc: " + currentLocation + " prev loc: " + previousLocation);
   }

   /**
    * resume tracking
    * @param paused
    */
   public void resumeTracking(boolean paused){
      isTrackingPaused = paused;
      Log.e(TAG, "resumed tracking");
      //resume listening to location updates
      startListening();

      }
   /**
    * add new location point coordonate to the list
    * @param location current location
    */
   private void addPoint(Location location){
      Coordinate point = new Coordinate(location.getLatitude(),location.getLongitude(),location.getAltitude(),(location.getSpeed()*3.6));
      coordinates.add(point);
   }

   /**
    * get distance between two locations
    * @param previous location
    * @param current location
    * @return distance in km
    */
   private double calculateDistance(Location previous, Location current){
      return (current.distanceTo(previous))*0.001;
   }

   /**
    * get last element of the coordinates list
    * @return
    */
   private int getLast(){
      return getCoordinates().size()-1;
   }

   public List<Coordinate> getCoordinates() {
      return coordinates;
   }

   public double getDistance() {
      return distance;
   }

   /**
    * get speed for current location
    * @return either manually calculated speed or speed provided by location object (km/h)
    */
   public double getSpeed() {
      if(currentLocation.getSpeed()!=0)
         return currentLocation.getSpeed();
      else
         return speed;
   }

   /**
    * get average speed for calories calculation
    * @return average speed in km/h
    */
   public double getAverageSpeed(){
      double avSpeed = 0.0;
      for(Coordinate c:getCoordinates()){
         avSpeed+=c.getSpeed();
         Log.e(TAG,"speed: "+c.getSpeed()+" av speed: "+avSpeed);
      }
      return avSpeed/(getCoordinates().size());
   }

   public boolean isTracking() {
      return isTracking;
   }

   public boolean isTrackingPaused() {
      return isTrackingPaused;
   }
   /** Determines whether one Location reading is better than the current Location fix
    * from http://developer.android.com/guide/topics/location/strategies.html
    * @param newLocation  The new Location that you want to evaluate
    * @param currentBestLocation  The current Location fix, to which you want to compare the new one
    */
   protected boolean isBetterLocation(Location newLocation, Location currentBestLocation) {
      if (currentBestLocation == null) {
         // A new location is always better than no location
         return true;
      }

      // Check whether the new location fix is newer or older
      //by time
      long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
      boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
      boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
      boolean isNewer = timeDelta > 0;

      // If it's been more than two minutes since the current location, use the new location
      // because the user has likely moved
      if (isSignificantlyNewer) {
         return true;
         // If the new location is more than two minutes older, it must be worse
      } else if (isSignificantlyOlder) {
         return false;
      }

      // Check whether the new location fix is more or less accurate
      int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
      Log.e(TAG,"acc new: "+newLocation.getAccuracy()+", acc old: "+currentBestLocation.getAccuracy()+", acc diff: "+accuracyDelta);
      boolean isLessAccurate = accuracyDelta > 0;
      boolean isMoreAccurate = accuracyDelta < 0;
      boolean isSignificantlyLessAccurate = accuracyDelta > 50;

      // Check if the old and new location are from the same provider
      boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
              currentBestLocation.getProvider());

      // Determine location quality using a combination of timeliness and accuracy
      if (isMoreAccurate) {
         return true;
      } else if (isNewer && !isLessAccurate) {
         return true;
      } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
         return true;
      }
      return false;
   }

   /** Checks whether two providers are the same */
   private boolean isSameProvider(String provider1, String provider2) {
      if (provider1 == null) {
         return provider2 == null;
      }
      return provider1.equals(provider2);
   }

   /**
    * show notification
    * @param provider name of location provider to display in notification
    */
   private void showNotification(String provider){
      //create an ongoing notification while session is running
      //clicking on notification will bring the walking session activity
      int id = 1;
      if(provider.equalsIgnoreCase("GPS"))
         id = 100;
      else if(provider.equalsIgnoreCase("Network"))
         id = 101;
      else
         id = 134;
      Log.e(TAG,"notif, provider: "+provider +", id: "+id);
      Intent intent = new Intent(this, WalkingMapActivity.class);
      //bring walking activity to front if it exists, create new instance if activity was killed
      intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
      PendingIntent contentIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT );

      Notification.Builder builder = new Notification.Builder(this);

      builder.setContentIntent(contentIntent)
              .setSmallIcon(R.drawable.ic_walk_white)
              .setTicker("@string/app_name")
              .setOngoing(true)
       //       .setContentText("Using "+provider)
              .setContentTitle("Session is currently running")
      ;
      Notification not = builder.build();

      startForeground(id, not);

   }

   /**
    * check if location providers are enabled and start listening for updates
    * show notification
    */
   private void startListening() {
      locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
      isGPSEnabled = locationManager
              .isProviderEnabled(LocationManager.GPS_PROVIDER);

      // getting network status
      isNetworkEnabled = locationManager
              .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
      //if GPS is enabled start listening to updates from GPD provider at chosen intervals
      if (isGPSEnabled()) {
         startLocationListening(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE);
         locationManager.addGpsStatusListener(mStatusListener);
         //create an ongoing notification while session is running
         //clicking on notification will bring the walking session activity
         showNotification("GPS");
         /**
          * for testing purposes - listen to network provider
          */
         //Log.e(TAG,"network is on: "+isNetworkEnabled);
         /*if (isNetworkEnabled) {
            startLocationListening(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE);
            //create an ongoing notification while session is running
            //clicking on notification will bring the walking session activity
            showNotification("Network");
            Log.e(TAG, "network listening");
         }*/

      }

   }

   /**
    * read user preferences for update location intervals
    * @param value - chosen option
    */
   private void getIntervals(int value){
      switch (value){
         case 1: MIN_TIME = 1000 * 1 * 15;
            MIN_DISTANCE = 0;
            break;
         case 2: MIN_TIME = 1000 * 1 * 30;
            MIN_DISTANCE = 30;
            break;
         case 3: MIN_TIME = 1000 * 1 * 60;
            MIN_DISTANCE = 60;
            break;
         case 4: MIN_TIME = 1000 * 1 * 120;
            MIN_DISTANCE = 100;
            break;
      }
   }

   /**
    * listen to GPS status changes
    */
   private GpsStatus.Listener mStatusListener = new GpsStatus.Listener()
   {
      @Override
      public synchronized void onGpsStatusChanged(int event)
      {
         switch (event)
         {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                  GpsStatus status = locationManager.getGpsStatus(null);
                  int mSatellites = 0;
                  Iterable<GpsSatellite> list = status.getSatellites();
                  for (GpsSatellite satellite : list)
                  {
                     if (satellite.usedInFix())
                     {
                        mSatellites++;
                     }
                  }
               //Toast.makeText(LocationService.this,"number of satellites is "+mSatellites,Toast.LENGTH_SHORT).show();

               break;
            case GpsStatus.GPS_EVENT_STOPPED:
               //Toast.makeText(LocationService.this,"GPS stopped",Toast.LENGTH_SHORT).show();
               break;
            case GpsStatus.GPS_EVENT_STARTED:
           //    Toast.makeText(LocationService.this,"GPS started",Toast.LENGTH_SHORT).show();
               break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
             //  Toast.makeText(LocationService.this,"GPS has first fix on location",Toast.LENGTH_SHORT).show();
               break;

            default:
               break;
         }
      }
   };

   /**
    * check if there is network coverage
    * @return true if internet is available, false otherwise
    */
   public boolean isOnline()
   {
      ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo info = connMgr.getActiveNetworkInfo();

      return (info != null && info.isConnected());
   }

   @Override
   public void onDestroy() {
      stopLocationListening();
      super.onDestroy();
   }
}
