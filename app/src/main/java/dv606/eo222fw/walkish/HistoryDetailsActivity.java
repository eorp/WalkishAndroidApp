package dv606.eo222fw.walkish;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
//show details for selected route including route path drawn on the map
public class HistoryDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {


   private GoogleMap mMap; // Might be null if Google Play services APK is not available.
   //UI elements
   private TextView txtDuration;
   private TextView txtDistance;
   private TextView txtSpeed;
   private TextView txtCalories;
   private TextView txtStartTime;
   private TextView txtFinishTime;
   //route object
   private Route route = null;
   //list of route coordinates
   private List<Coordinate> coordinates = new ArrayList<Coordinate>();
   //bounds builder
   private LatLngBounds.Builder builder;
   private final String TAG = "details";
   //map type
   private int mapType = GoogleMap.MAP_TYPE_NORMAL;
   //route colour
   private int routeColour = Color.BLUE;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_history_details);

      //get reference to Ui elements
      txtCalories = (TextView) findViewById(R.id.txt_history__calories);
      txtDistance = (TextView) findViewById(R.id.txt_history_distance);
      txtDuration = (TextView) findViewById(R.id.txt_history_duration);
      txtSpeed = (TextView) findViewById(R.id.txt_average_speed);
      txtStartTime = (TextView) findViewById(R.id.txt_start_time);
      txtFinishTime = (TextView) findViewById(R.id.txt_finish_time);
      //read map type value from shared preferences
      if(Utils.getSettingsValues("type",Utils.readMapType(this))!=0)
      mapType = Utils.getSettingsValues("type",Utils.readMapType(this));
      //read route colour from shared preferences
      if(Utils.getSettingsValues("colour",Utils.readRouteColour(this))!=0)
      routeColour = Utils.getSettingsValues("colour",Utils.readRouteColour(this));

      //check if there is route object passed to activity
      Bundle bundle = getIntent().getExtras();
      if (bundle != null && bundle.containsKey("route")) {
         //set alarm details
         setRoute((Route) bundle.getSerializable("route"));
      }
      //if route object was retreived successfully
      if(route!=null) {
         //set action bar title to route's name
         setTitle(String.format("%s",route.getName()));
         //get list of coordinates for the route to draw path
         coordinates = route.getCoordinates();
         //Log.e(TAG,"route: "+route);
         //if there was no coordinates recorded for the route - don't show map
         if(coordinates!=null)
            if(coordinates.size()==0)
               findViewById(R.id.map2).setVisibility(View.INVISIBLE);
         //setUpMapIfNeeded();
         //display route details
         showRoute();
      }
      else
         //otherwise return to previous activity
         finish();

   }

   @Override
   protected void onResume() {
      super.onResume();

      Log.e(TAG, "on resume, map: " + mMap);
      mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2))
              .getMap();
      Log.e(TAG, "on resume after, map: "+mMap);
      if (mMap != null) {
         mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
               Log.e(TAG, "map loaded");
               if (route.getCoordinates() != null)
                  if (route.getCoordinates().size() > 0) {

                     //move camera to display whole route
                     mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 70));
                  } else {
                     findViewById(R.id.map2).setVisibility(View.INVISIBLE);
                  }
            }
         });
         setUpMap();
      }


   }

   /**
    * set up map
    */
   private void setUpMap() {
      //Map Settings
      mMap.getUiSettings().setMyLocationButtonEnabled(true);
      mMap.getUiSettings().setCompassEnabled(true);
      mMap.getUiSettings().setZoomControlsEnabled(true);
      //map type from preferences
      mMap.setMapType(mapType);
      //provided we have a route object
      if(route!=null)
      {
         //draw a polyline for retreived coordinates
         //polyline options
         PolylineOptions options = new PolylineOptions();
         //polyline settings
         //path colour from settings
         options.color(routeColour);
         options.width(3);
         options.visible(true);
         //set boundaries to include whole route on the map
         builder = new LatLngBounds.Builder();
         //if we have route coordinates
         if(route.getCoordinates()!=null) {
            if (route.getCoordinates().size() > 0) {
               //loop through route coordinates and add them to polyline
               //include route points to the boundary builder
               for (Coordinate c : route.getCoordinates()) {
                  options.add(new LatLng(c.getLat(), c.getLng()));
                  builder.include(new LatLng(c.getLat(), c.getLng()));
               }
               //add polyline to the map
               mMap.addPolyline(options);
               //add start route marker to the map
               mMap.addMarker(new MarkerOptions().position(new LatLng(route.getCoordinates().get(0).getLat(), route.getCoordinates().get(0).getLng()))
                       .title("start")
                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_start))
                       .snippet("Starting point").anchor(0.5f, 0.7f));
               //add marker for destination point on the map
               mMap.addMarker(new MarkerOptions().position(new LatLng(route.getCoordinates().get((route.getCoordinates().size() - 1)).getLat(), route.getCoordinates().get((route.getCoordinates().size() - 1)).getLng()))
                       .title("Finish")
                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_end))
                       .snippet("Destination point").anchor(0.5f, 0.7f));
            }
         }

      }

   }


   /**
    * display route details
    */
   private void showRoute(){
      //set text to display corresponding values
      txtStartTime.setText(route.getStartTime());
      txtFinishTime.setText(route.getFinishTime());
      txtDuration.setText(route.getDuration());
      txtDistance.setText(String.valueOf(route.getDistance()));
      //txtCalories.setText(route.getCalories());
      //calculate average speed
      double avSpeed = 0.0;
      int i = 0;
      if(coordinates!=null)
      if(coordinates.size()>0){
         for(Coordinate c:coordinates){
            if(c.getSpeed()>0)
            avSpeed+=c.getSpeed();
            Log.e(TAG,"coordinate "+i+", is "+c);
            i++;
         }
         avSpeed = avSpeed/(coordinates.size());
      }
      double duration = WalkingMapActivity.getHoursFromDurationString(route.getDuration());
      avSpeed = Double.parseDouble(route.getDistance())/duration;
      txtSpeed.setText(String.format("%.2f km/h",avSpeed));
      txtCalories.setText(WalkingMapActivity.calculateCalories(Double.parseDouble(route.getDistance()),route.getDuration()));

   }
   public void setRoute(Route route) {
      this.route = route;
   }

   @Override
   public void onMapReady(GoogleMap googleMap) {
      Log.e(TAG,"map ready, map: "+googleMap);
      mMap = googleMap;
      //on map fully loaded
      mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
         @Override
         public void onMapLoaded() {
            Log.e(TAG, "map loaded");

            //move camera to display whole route
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
         }
      });
      setUpMap();
   }
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_details, menu);
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
      } else if (id == R.id.choose_home) {
         startActivity(new Intent(this,MainActivity.class));
      } else if (id == R.id.choose_session) {
         startActivity(new Intent(this,WalkingMapActivity.class));
      }

      return super.onOptionsItemSelected(item);
   }

}
