package dv606.eo222fw.walkish;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evgeniya O'Regan Pevchikh on 25/01/2016.
 */
//custom adapter class used to display routes in a list
public class RoutesListAdapter extends BaseAdapter {
   //reference to routes history activity
   private RoutesHistoryActivity routesActivity;
   //list of routes
   private List<Route> routes = new ArrayList<Route>();

   public RoutesListAdapter(RoutesHistoryActivity routesActivity) {
      this.routesActivity = routesActivity;
   }

   @Override
   public int getCount() {
      return routes.size();
   }

   @Override
   public Object getItem(int i) {
      return routes.get(i);
   }

   @Override
   public long getItemId(int i) {
      return i;
   }

   @Override
   public View getView(int i, View view, ViewGroup viewGroup) {
      if (null == view)
         view = LayoutInflater.from(routesActivity).inflate(
                 R.layout.history_elements, null);
      //get route object
      Route route = (Route)getItem(i);
      //get references to UI elements
      TextView txtDistance = (TextView) view.findViewById(R.id.txt_history_distance);
      TextView txtTime = (TextView) view.findViewById(R.id.txt_from_to);
      TextView txtDate = (TextView) view.findViewById(R.id.txt_history_date);
      //set text of Ui elements
      txtDistance.setText(String.format("%s km",route.getDistance()));
      txtTime.setText(String.format("%s to %s",route.getStartTime(),route.getFinishTime()));
      txtDate.setText(String.format("%s",route.getDate()));

      return view;
   }

   public List<Route> getRoutes() {
      return routes;
   }

   public void setRoutes(List<Route> routes) {
      this.routes = routes;
   }
}
