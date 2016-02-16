package dv606.eo222fw.walkish;

import java.io.Serializable;

/**
 * Created by Evgeniya O'Regan Pevchikh on 25/01/2016.
 */

/**
 * model for coordinate object
 */
public class Coordinate implements Serializable {
   //coordinate id
   private int id;
   //route id associated with this coordinate
   private int routeId;
   //coordinate latitude
   private double lat;
   //coordinate longitude
   private double lng;
   //coordinate altitude
   private double alt;
   //coordinate speed
   private double speed;
//constructors
   public Coordinate() {
   }



   public Coordinate(double lat, double lng, double alt, double speed) {
      this.lat = lat;
      this.lng = lng;
      this.alt = alt;
      this.speed = speed;
   }

   public Coordinate(double lat, double lng) {
      this.lat = lat;
      this.lng = lng;
   }
//getters and setters
   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public double getLat() {
      return lat;
   }

   public void setLat(double lat) {
      this.lat = lat;
   }

   public double getLng() {
      return lng;
   }

   public void setLng(double lng) {
      this.lng = lng;
   }

   public double getAlt() {
      return alt;
   }

   public void setAlt(double alt) {
      this.alt = alt;
   }

   public double getSpeed() {
      return speed;
   }

   public void setSpeed(double speed) {
      this.speed = speed;
   }

   public int getRouteId() {
      return routeId;
   }

   public void setRouteId(int routeId) {
      this.routeId = routeId;
   }

   @Override
   public String toString() {
      return "Coordinate{" +
              "id=" + id +
              ", routeId=" + routeId +
              ", lat=" + lat +
              ", lng=" + lng +
              ", alt=" + alt +
              ", speed=" + speed +
              '}';
   }
}
