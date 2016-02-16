package dv606.eo222fw.walkish;

/**
 * Created by Evgeniya O'Regan Pevchikh on 23/01/2016.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * class containing static helper methods
 */
public class Utils {
   /**
    * @return date in format Monday, 4 July 2016
    */
   public static String getFullDateString() {
      return DateFormat.format("EEEE, d MMMM yyyy", Calendar.getInstance().getTime()).toString();
   }

   /**
    * @return date in format Mon, 4/5/2016
    */
   public static String getShortDateString() {
      return DateFormat.format("EEE, d/MM/yyyy", Calendar.getInstance().getTime()).toString();
   }

   /**
    * @return system time in format 00:00:00
    */
   public static String getCurrentTimeString() {
      return DateFormat.format("kk:mm:ss", System.currentTimeMillis()).toString();
   }
   /**
    * @return system time in format 00:00
    */
   public static String getCurrentTimeStringShort() {
      return DateFormat.format("kk:mm", System.currentTimeMillis()).toString();
   }

   /**
    * get elapsed time in milliseconds in format hh:mm:ss
    * @param start start time in nanoseconds
    * @param finish finish time in nanoseconds
    * @return duration string in format 00:00:00
    */
   public static String getDurationString(long start, long finish){
      long durationInNanoSecs = finish - start;
      long durationInMills = TimeUnit.NANOSECONDS.toMillis(durationInNanoSecs);
      return DateFormat.format("kk:mm:ss", durationInMills).toString();
   }
   /**
    * change buttons appearance depending on lock status
    *
    * @param context  context
    * @param isLocked lock status
    * @param isPaused pause status
    * @param btnStop  stop button
    * @param btnPause resume/pause button
    */
   public static void setBtnBgColour(Context context, boolean isLocked, boolean isPaused, Button btnStop, Button btnPause) {
      //if lock is on
      if (isLocked) {
         //change buttons bg to gray
         btnStop.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
         btnPause.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
         //disable button click
         btnPause.setEnabled(false);
         btnStop.setEnabled(false);
      }//if lock is off
      else {
         //set active buttons colours
         btnStop.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));
         togglePauseResumeBtn(context, isPaused, btnPause);
         //enable button click
         btnPause.setEnabled(true);
         btnStop.setEnabled(true);
      }

   }

   /**
    * change lock image button colour and image
    *
    * @param imgBtn   imageButton object
    * @param context
    * @param isLocked false:unlocked; true: locked
    */
   public static void toggleLockBtn(ImageButton imgBtn, Context context, boolean isLocked) {
      //if lock is on
      if (isLocked) {//set blue bg and show appropriate image
         imgBtn.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
         imgBtn.setImageResource(R.drawable.ic_action_lock_on);
      } else {//if lock is off - change bg and image accordingly
         imgBtn.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
         imgBtn.setImageResource(R.drawable.ic_action_lock_off);
      }
   }

   /**
    * change pause/resume button background colour and text
    *
    * @param context  context
    * @param isPaused true:paused, false - not
    * @param btn      button object
    */
   public static void togglePauseResumeBtn(Context context, boolean isPaused, Button btn) {//based on paused status set appropriate bg colour and text
      if (!isPaused) {
         btn.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
         btn.setText("PAUSE");
      } else {
         btn.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
         btn.setText("RESUME");
      }
   }

   public static List<Coordinate> getCoordinateList(String kml){
      List<Coordinate> coordinates = new ArrayList<Coordinate>();
      //get array of coordinates by splitting the string
      String[] markers = kml.split(" ");
      //  Log.e("handler", "route  handler, split 1 length : "+markers.length);
      //loop through each coordinate
      for(int i=0;i<markers.length;i++)
      {
         //populate list of coordinates
         coordinates.add(createCoordinate(markers[i]));

      }
      return coordinates;
   }
   /**
    * extract coordinates from a string of lat, lng and alt
    * @param c string containing latitude, lontitude and altitude separated by commas
    * @return coordinate object
    */
   private static Coordinate createCoordinate(String c)
   {
      //split that string by delimiter
      String[] splitted = c.split(",");
      //Log.e("handler", "route  handler, split 1 length : "+splitted.length+" coordinate: "+splitted);
      //extract location latitude
      double lat = Double.parseDouble(splitted[1].trim());
      //extract location longtitude
      double lng = Double.parseDouble(splitted[0].trim());
      //extract location altitude
      double alt = Double.parseDouble(splitted[2].trim());

      return new Coordinate(lat,lng);

   }

   /**
    * Gets a SharedPreferences instance that points to the default file that is used by the preference framework in the given context
    * @param context
    * @return SharedPreferences instance that can be used to retrieve and listen to values of the preferences
    */
   public static SharedPreferences readPreferences(Context context){
      return PreferenceManager.getDefaultSharedPreferences(context);
   }

   /**
    * read map type position value from shared preferences
    * @param context
    * @return int value of map type position in map array
    */
   public static int readMapType(Context context){
      SharedPreferences prefs = readPreferences(context);
      return Integer.parseInt(prefs.getString("mapType", "2"));
   }

   /**
    * read route colour position from shared preferences
    * @param context
    * @return int value of chosen position
    */
   public static int readRouteColour(Context context){
      SharedPreferences prefs = readPreferences(context);
      return Integer.parseInt(prefs.getString("routeColour", "1"));
   }

   /**
    * read gps settings from shared preferences
    * @param context
    * @return in tvalue of chosen position
    */
   public static int readGPSSettings(Context context){
      SharedPreferences prefs = readPreferences(context);
      return Integer.parseInt(prefs.getString("refreshGPS", "1"));
   }

   /**
    * read weight value from preferences
    * @param context
    * @return int value of entered weight
    */
   public static int readWeight(Context context){
      SharedPreferences prefs = readPreferences(context);
      return Integer.parseInt(prefs.getString("weight", "60"));
   }
   //return values to set app appearance from settings values
   public static int getSettingsValues(String description, int value)
   {
      //return route colour
      if(description.equals("colour"))
      {
         switch (value)
         {
            case 1: return Color.BLUE;
            case 2: return Color.BLACK;
            case 3: return Color.GREEN;
            case 4: return Color.RED;
         }
      }//return map type
      else if(description.equals("type"))
      {
         switch (value)
         {
            case 1: return GoogleMap.MAP_TYPE_HYBRID;
            case 2: return GoogleMap.MAP_TYPE_NORMAL;
            case 3: return GoogleMap.MAP_TYPE_SATELLITE;
            case 4: return GoogleMap.MAP_TYPE_TERRAIN;
         }

      }
      return 0;
   }

   /**
    * check if provided value is within given range
    * @param min min range value
    * @param max max range value
    * @param value value to check
    * @return true if within range, false otherwise
    */
   public static boolean isWithinRange(int min,int max, String value){
      int number = Integer.parseInt(value);
      return number >= min && number <= max;
   }

   public static boolean isWithinRange(double min,double max, double number){
      //int number = Integer.parseInt(value);
      return number >= min && number <= max;
   }
   /**
    * check if there is network coverage
    * @return true if internet is available, false otherwise
    */
   public static boolean isOnline(Context c)
   {
      ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo info = connMgr.getActiveNetworkInfo();

      return (info != null && info.isConnected());
   }
}
