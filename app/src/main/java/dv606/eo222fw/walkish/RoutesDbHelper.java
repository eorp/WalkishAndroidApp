package dv606.eo222fw.walkish;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evgeniya O'Regan Pevchikh on 25/01/2016.
 */
public class RoutesDbHelper extends SQLiteOpenHelper {
   static RoutesDbHelper instance = null;
   static SQLiteDatabase database = null;

   // Logcat tag
   private static final String TAG = "DatabaseHelper";
   //db name
   private static final String DATABASE_NAME = "routes.db";
   //db version
   private static final int DATABASE_VERSION = 1;
   //tables names
   public static final String ROUTES_TABLE_NAME = "routes";
   public static final String COORDINATES_TABLE_NAME = "coordinates";
   public static final String ROUTES_COORDINATES_TABLE_NAME = "routes_coordinates";
   //common columns name
   public static final String COLUMN_ID = "_id";
   //routes table columns
   public static final String COLUMN_ROUTE_NAME = "name";
   public static final String COLUMN_ROUTE_START_TIME = "start_time";
   public static final String COLUMN_ROUTE_FINISH_TIME = "finish_time";
   public static final String COLUMN_ROUTE_DURATION = "duration";
   public static final String COLUMN_ROUTE_DISTANCE = "distance";
   public static final String COLUMN_ROUTE_CALORIES = "calories";
   public static final String COLUMN_ROUTE_DATE = "date";
   public static final String COLUMN_ROUTE_NOTE = "note";
   //coordinates table columns
   public static final String COLUMN_COORDINATE_LAT = "lat";
   public static final String COLUMN_COORDINATE_LNG = "lng";
   public static final String COLUMN_COORDINATE_ALT = "alt";
   public static final String COLUMN_COORDINATE_SPEED = "speed";
   //routes_coordinates table columns
   private static final String COLUMN_ROUTE_ID = "route_id";
   private static final String COLUMN_COORDINATE_ID = "coordinate_id";
   //route table create statement
   private static final String CREATE_ROUTE_TABLE = "CREATE TABLE IF NOT EXISTS " + ROUTES_TABLE_NAME + " ( "
           + COLUMN_ID + " INTEGER primary key autoincrement, "
           + COLUMN_ROUTE_NAME + " TEXT NOT NULL, "
           + COLUMN_ROUTE_START_TIME + " TEXT NOT NULL, "
           + COLUMN_ROUTE_FINISH_TIME + " TEXT NOT NULL, "
           + COLUMN_ROUTE_DURATION + " TEXT NOT NULL, "
           + COLUMN_ROUTE_DISTANCE + " TEXT, "
           + COLUMN_ROUTE_CALORIES + " TEXT, "
           + COLUMN_ROUTE_DATE + " TEXT NOT NULL, "
           + COLUMN_ROUTE_NOTE + " TEXT) ";
   //coordinate table create statement
   private static final String CREATE_COORDINATE_TABLE = "CREATE TABLE IF NOT EXISTS " + COORDINATES_TABLE_NAME + " ( "
           + COLUMN_ID + " INTEGER primary key autoincrement, "
           + COLUMN_COORDINATE_LAT + " TEXT NOT NULL, "
           + COLUMN_COORDINATE_LNG + " TEXT NOT NULL, "
           + COLUMN_COORDINATE_ALT + " TEXT, "
           + COLUMN_COORDINATE_SPEED + " TEXT NOT NULL) ";
   //route_coordinate table create statement
   private static final String CREATE_ROUTE_COORDINATE_TABLE = "CREATE TABLE IF NOT EXISTS " + ROUTES_COORDINATES_TABLE_NAME + " ( "
           + COLUMN_ID + " INTEGER primary key autoincrement, "
           + COLUMN_ROUTE_ID + " INTEGER NOT NULL, "
           + COLUMN_COORDINATE_ID + " INTEGER NOT NULL) ";


   /**
    * instantiate this class
    * @param context
    */
   public static void init(Context context) {
      if (null == instance) {
         instance = new RoutesDbHelper(context);
      }
   }

   /**
    * get db instance
    * @return
    */
   public static SQLiteDatabase getDatabase() {
      if (null == database) {
         database = instance.getWritableDatabase();
      }
      return database;
   }

   /**
    * deactivate db
    */
   public static void deactivate() {
      if (null != database && database.isOpen()) {
         database.close();
      }
      database = null;
      instance = null;
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      // creating required tables
      db.execSQL(CREATE_ROUTE_TABLE);
      db.execSQL(CREATE_COORDINATE_TABLE);
      db.execSQL(CREATE_ROUTE_COORDINATE_TABLE);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int i, int i1) {
      // on upgrade drop older tables
      db.execSQL("DROP TABLE IF EXISTS " + CREATE_ROUTE_TABLE);
      db.execSQL("DROP TABLE IF EXISTS " + CREATE_COORDINATE_TABLE);
      db.execSQL("DROP TABLE IF EXISTS " + CREATE_ROUTE_COORDINATE_TABLE);
      //create new tables
      onCreate(db);
   }

   public RoutesDbHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }

   /****                   Routes table methods                 *****/

   public static long createRoute(Route route){
      ContentValues cv = new ContentValues();
      cv.put(COLUMN_ROUTE_NAME,route.getName());
      cv.put(COLUMN_ROUTE_START_TIME,route.getStartTime());
      cv.put(COLUMN_ROUTE_FINISH_TIME,route.getFinishTime());
      cv.put(COLUMN_ROUTE_DURATION,route.getDuration());
      cv.put(COLUMN_ROUTE_DISTANCE,route.getDistance());
      cv.put(COLUMN_ROUTE_CALORIES,route.getCalories());
      cv.put(COLUMN_ROUTE_DATE,route.getDate());
      cv.put(COLUMN_ROUTE_NOTE, route.getNote());
      long routeId = getDatabase().insert(ROUTES_TABLE_NAME, null, cv);
      Log.e(TAG,"route id: "+routeId);
      return routeId;
   }

   /**
    * get route object
    * @param id - selected route
    * @return route object
    */
   public static Route getRoute(int id){
      String[] columns = new String[] {
               COLUMN_ID,
               COLUMN_ROUTE_NAME,
               COLUMN_ROUTE_START_TIME,
               COLUMN_ROUTE_FINISH_TIME,
               COLUMN_ROUTE_DURATION,
               COLUMN_ROUTE_DISTANCE,
               COLUMN_ROUTE_CALORIES,
               COLUMN_ROUTE_DATE,
               COLUMN_ROUTE_NOTE
      };
      Cursor c = getDatabase().query(ROUTES_TABLE_NAME, columns, COLUMN_ID+"="+id, null, null, null,
              null);

      Route route = null;
      if(c.moveToFirst()){
         route = new Route();
         route.setId(c.getInt(c.getColumnIndex(COLUMN_ID)));
         route.setCalories(c.getString(c.getColumnIndex(COLUMN_ROUTE_CALORIES)));
         route.setDate(c.getString(c.getColumnIndex(COLUMN_ROUTE_DATE)));
         route.setDistance(c.getString(c.getColumnIndex(COLUMN_ROUTE_DISTANCE)));
         route.setDuration(c.getString(c.getColumnIndex(COLUMN_ROUTE_DURATION)));
         route.setFinishTime(c.getString(c.getColumnIndex(COLUMN_ROUTE_FINISH_TIME)));
         route.setName(c.getString(c.getColumnIndex(COLUMN_ROUTE_NAME)));
         route.setNote(c.getString(c.getColumnIndex(COLUMN_ROUTE_NOTE)));
         route.setStartTime(c.getString(c.getColumnIndex(COLUMN_ROUTE_START_TIME)));
      }
      c.close();
      return route;
   }

   /**
    *
    * @return cursor to query all columns from routes table
    */
   public static Cursor getRouteCursor() {

      String[] columns = new String[] {
              COLUMN_ID,
              COLUMN_ROUTE_NAME,
              COLUMN_ROUTE_START_TIME,
              COLUMN_ROUTE_FINISH_TIME,
              COLUMN_ROUTE_DURATION,
              COLUMN_ROUTE_DISTANCE,
              COLUMN_ROUTE_CALORIES,
              COLUMN_ROUTE_DATE,
              COLUMN_ROUTE_NOTE
      };
      Cursor c = getDatabase().query(ROUTES_TABLE_NAME, columns, null, null, null, null,
              null);

      return c;
   }

   /**
    *
    * @return list of all routes
    */
   public static List<Route> getAllRoutes() {
      List<Route> routes = new ArrayList<Route>();
      Cursor c = RoutesDbHelper.getRouteCursor();

      if (c.moveToFirst()) {

         do {
            Route route = new Route();
            route.setId(c.getInt(c.getColumnIndex(COLUMN_ID)));
            route.setCalories(c.getString(c.getColumnIndex(COLUMN_ROUTE_CALORIES)));
            route.setDate(c.getString(c.getColumnIndex(COLUMN_ROUTE_DATE)));
            route.setDistance(c.getString(c.getColumnIndex(COLUMN_ROUTE_DISTANCE)));
            route.setDuration(c.getString(c.getColumnIndex(COLUMN_ROUTE_DURATION)));
            route.setFinishTime(c.getString(c.getColumnIndex(COLUMN_ROUTE_FINISH_TIME)));
            route.setName(c.getString(c.getColumnIndex(COLUMN_ROUTE_NAME)));
            route.setNote(c.getString(c.getColumnIndex(COLUMN_ROUTE_NOTE)));
            route.setStartTime(c.getString(c.getColumnIndex(COLUMN_ROUTE_START_TIME)));

            routes.add(route);
         } while (c.moveToNext());
      }
      c.close();
      return routes;
   }

   /**
    * delete route by id
    * @param id route id
    */
   public static void deleteRoute(long id){
      //get list of all coordinates for this route
      List<Coordinate> coordinates = getCoordinatesByRouteId(id);
      //delete all coordinates associated with this route
      for(Coordinate coordinate: coordinates){
         //delete associated entries from route_coordinate table
    //     deleteRouteCoordinate(id,coordinate.getId());
         deleteCoordinate(coordinate.getId());
      }
      //delete selected route entry
      getDatabase().delete(ROUTES_TABLE_NAME, COLUMN_ID + "=" + id, null);
   }

   /*************** coordinates table ********************/
   /**
    * insert new coordinate
    * @param coordinate coordinate object
    * @param route_id route id associated with this coordinate
    * @return id of created entry
    */
   public static long createCoordinate(Coordinate coordinate, long route_id){
      ContentValues cv = new ContentValues();
      cv.put(COLUMN_COORDINATE_LAT,coordinate.getLat());
      cv.put(COLUMN_COORDINATE_LNG,coordinate.getLng());
      cv.put(COLUMN_COORDINATE_ALT,coordinate.getAlt());
      cv.put(COLUMN_COORDINATE_SPEED,coordinate.getSpeed());
      //insert row to coordinates table
      long id = getDatabase().insert(COORDINATES_TABLE_NAME, null, cv);
      Log.e(TAG,"coord id: "+id);
      //insert row to coordinate_route table
      createRouteCoordinate(route_id, id);
      return id;
   }

   /**
    * save list of coordinates
    * @param coordinates list to save
    * @param id associated route id
    */
   public static void createCoordinates(List<Coordinate> coordinates, long id){
      for(Coordinate coordinate: coordinates){
         createCoordinate(coordinate,id);
      }
   }

   /**
    * get coordinate by id
    * @param id coordinate id
    * @return coordinate object
    */
   public static Coordinate getCoordinate(long id){
      String[] columns = new String[] {
              COLUMN_ID,
              COLUMN_COORDINATE_LAT,
              COLUMN_COORDINATE_LNG,
              COLUMN_COORDINATE_ALT,
              COLUMN_COORDINATE_SPEED
      };
      String where = COLUMN_ID+"="+id;
      Cursor c =getDatabase().query(COORDINATES_TABLE_NAME, columns, where, null, null, null,
              null);

      Coordinate coordinate = null;
      if(c.moveToFirst()){
         coordinate = new Coordinate();
         coordinate.setId(c.getInt(c.getColumnIndex(COLUMN_ID)));
         coordinate.setLat(Double.valueOf(c.getString(c.getColumnIndex(COLUMN_COORDINATE_LAT))));
         coordinate.setLng(Double.valueOf(c.getString(c.getColumnIndex(COLUMN_COORDINATE_LNG))));
         coordinate.setAlt(Double.valueOf(c.getString(c.getColumnIndex(COLUMN_COORDINATE_ALT))));
         coordinate.setSpeed(Double.valueOf(c.getString(c.getColumnIndex(COLUMN_COORDINATE_SPEED))));
      }
      c.close();
      return coordinate;
   }

   public static Coordinate getCoordinate(){
      String[] columns = new String[] {
              COLUMN_ID,
              COLUMN_COORDINATE_LAT,
              COLUMN_COORDINATE_LNG,
              COLUMN_COORDINATE_ALT,
              COLUMN_COORDINATE_SPEED
      };
      //String where = COLUMN_ID+"="+id;
      Cursor c =getDatabase().query(COORDINATES_TABLE_NAME, columns, null, null, null, null,
              null);

      Coordinate coordinate = null;
      if(c.moveToFirst()){
         coordinate = new Coordinate();
         coordinate.setId(c.getInt(c.getColumnIndex(COLUMN_ID)));
         coordinate.setLat(Double.valueOf(c.getString(c.getColumnIndex(COLUMN_COORDINATE_LAT))));
         coordinate.setLng(Double.valueOf(c.getString(c.getColumnIndex(COLUMN_COORDINATE_LNG))));
         coordinate.setAlt(Double.valueOf(c.getString(c.getColumnIndex(COLUMN_COORDINATE_ALT))));
         coordinate.setSpeed(Double.valueOf(c.getString(c.getColumnIndex(COLUMN_COORDINATE_SPEED))));
      }
      c.close();
      return coordinate;
   }
   /**
    * get list of coordinate for certain route
    * @param id route id
    * @return list of coordinates
    */
   public static List<Coordinate> getCoordinatesByRouteId(long id){
      List<Coordinate> coordinates = new ArrayList<Coordinate>();
      String[] columns = new String[]{COLUMN_COORDINATE_ID};
      String where = COLUMN_ROUTE_ID+"="+id;
      Cursor c =getDatabase().query(ROUTES_COORDINATES_TABLE_NAME, columns, where, null, null, null,
              null);

      if(c.moveToFirst()){
         do{
            Coordinate coordinate = getCoordinate(c.getInt(c.getColumnIndex(COLUMN_COORDINATE_ID)));
            coordinates.add(coordinate);
         }while (c.moveToNext());
      }
      return coordinates;
   }

   /**
    * delete selected coordinate
    * @param id coordinate id
    */
   public static void deleteCoordinate(long id){
      getDatabase().delete(COORDINATES_TABLE_NAME, COLUMN_ID + "=" + id, null);
   }

   /******************** route_coordinate table *********************/
   /**
    * create route_coordinate
    * @param route_id
    * @param coordinate_id
    * @return created row id
    */
   public static long createRouteCoordinate(long route_id,long coordinate_id){
      ContentValues cv = new ContentValues();
      cv.put(COLUMN_ROUTE_ID,route_id);
      cv.put(COLUMN_COORDINATE_ID,coordinate_id);
      long id = getDatabase().insert(ROUTES_COORDINATES_TABLE_NAME, null, cv);
      //Log.e(TAG,"route coord id: "+id);
      return id;
   }

   /**
    * delete entry by route and coordinate ids
    * @param route_id
    * @param coordinate_id
    */
   public static void deleteRouteCoordinate(long route_id,long coordinate_id){
      String where = COLUMN_ROUTE_ID+"="+route_id+" AND "+COLUMN_COORDINATE_ID+"="+coordinate_id;
      getDatabase().delete(ROUTES_COORDINATES_TABLE_NAME, where, null);
   }

}
