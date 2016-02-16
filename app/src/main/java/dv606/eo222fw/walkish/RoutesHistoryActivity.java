package dv606.eo222fw.walkish;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

/**
 * show list of routes saved in db
 */
public class RoutesHistoryActivity extends AppCompatActivity {
   //list view
   private ListView routesListView;
   //list adapter
   private RoutesListAdapter routesListAdapter;
   private final String TAG = "history";

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_routes_history);
      //get reference to list view
      routesListView = (ListView)findViewById(android.R.id.list);
      routesListView.setLongClickable(true);
      //set delete option by showing context menu on long list item's click
      routesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
         @Override
         public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            //get selected route object
            final Route route = (Route) routesListAdapter.getItem(i);
            //build delete route dialog
            AlertDialog.Builder dialog = new AlertDialog.Builder(RoutesHistoryActivity.this);
            dialog.setTitle("Delete");
            dialog.setMessage("Delete this route?");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                  //get reference to routes db
                  RoutesDbHelper.init(RoutesHistoryActivity.this);
                  //delete selected route if action is confirmed
                  RoutesDbHelper.deleteRoute(route.getId());
                  //update routes list
                  updateRoutesList();
               }
            });//no action is taken if user chooses not to delete the route
            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
               }
            });
            //show alert dialog
            dialog.show();

            return true;
         }
      });
      //init list adapter
      routesListAdapter = new RoutesListAdapter(this);
      //bind list adapter and list view
      routesListView.setAdapter(routesListAdapter);
      //on list item click bring up a new activity with more details for selected item
      routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //get selected route object
            Route route = (Route) routesListAdapter.getItem(i);
            //get route id
            long routeId = route.getId();
            //get route coordinates list
            List<Coordinate> coordinates = RoutesDbHelper.getCoordinatesByRouteId(routeId);
            Log.e(TAG,"route: "+route+", route id: "+routeId);
            Log.e(TAG,"coordinates: "+coordinates);
            route.setCoordinates(coordinates);
            Log.e(TAG, "route: " + route + ", route id: " + routeId);
            //create intent to start route details activity
            Intent intent = new Intent(RoutesHistoryActivity.this, HistoryDetailsActivity.class);
            //put selected route object as an extra
            intent.putExtra("route", route);

            startActivity(intent);
         }
      });
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_routes_history, menu);
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
   @Override
   protected void onPause() {
      RoutesDbHelper.deactivate();
      super.onPause();
   }

   @Override
   protected void onResume() {
      super.onResume();
      updateRoutesList();
   }
   /**
    * update route list to include routes added,
    * exclude deleted routes
    */
   public void updateRoutesList(){
      //get routes db reference
      RoutesDbHelper.init(RoutesHistoryActivity.this);
      //read all routes from db
      final List<Route> routes = RoutesDbHelper.getAllRoutes();
      Log.e("history", "route: " + routes);
      //populate routes array list with info from db
      routesListAdapter.setRoutes(routes);

      runOnUiThread(new Runnable() {
         public void run() {
            // reload routes list view content
            RoutesHistoryActivity.this.routesListAdapter.notifyDataSetChanged();
            //if there are no routes in db - display corresponding message
            //otherwise display a list of routes
            if (routes.size() > 0) {
               findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);
            } else {
               findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
            }
         }
      });
   }

   @Override
   public void onBackPressed() {
      startActivity(new Intent(this,MainActivity.class));
   }


}
