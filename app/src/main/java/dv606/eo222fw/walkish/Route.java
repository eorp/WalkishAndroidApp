package dv606.eo222fw.walkish;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Evgeniya O'Regan Pevchikh on 25/01/2016.
 */

/**
 * model class for route object
 */
public class Route implements Serializable {
   //route id
   private int id;
   //route name
   private String name;
   //route  start time
   private String startTime;
   //route finish time
   private String finishTime;
   ////route duration
   private String duration;
   //route distance
   private String distance;
   //route calories
   private String calories;
   //route date
   private String date;
   //route note - not used in this version
   private String note;
   //route start point coordinate object
   private Coordinate startPoint;
   //route finish point coordinate object
   private Coordinate finishPoint;
   //list of coordinates for this route
   private List<Coordinate> coordinates;

   public Route() {
   }

   //gettters and setters
   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getStartTime() {
      return startTime;
   }

   public void setStartTime(String startTime) {
      this.startTime = startTime;
   }

   public String getFinishTime() {
      return finishTime;
   }

   public void setFinishTime(String finishTime) {
      this.finishTime = finishTime;
   }

   public String getDuration() {
      return duration;
   }

   public void setDuration(String duration) {
      this.duration = duration;
   }

   public String getDistance() {
      return distance;
   }

   public void setDistance(String distance) {
      this.distance = distance;
   }

   public String getCalories() {
      return calories;
   }

   public void setCalories(String calories) {
      this.calories = calories;
   }

   public String getDate() {
      return date;
   }

   public void setDate(String date) {
      this.date = date;
   }

   public String getNote() {
      return note;
   }

   public void setNote(String note) {
      this.note = note;
   }

   public List<Coordinate> getCoordinates() {
      return coordinates;
   }

   public void setCoordinates(List<Coordinate> coordinates) {
      this.coordinates = coordinates;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Coordinate getStartPoint() {
      return startPoint;
   }

   public void setStartPoint(Coordinate startPoint) {
      this.startPoint = startPoint;
   }

   public Coordinate getFinishPoint() {
      return finishPoint;
   }

   public void setFinishPoint(Coordinate finishPoint) {
      this.finishPoint = finishPoint;
   }

   @Override
   public String toString() {
      return "Route{" +
              "id=" + id +
              ", name='" + name + '\'' +
              ", startTime='" + startTime + '\'' +
              ", finishTime='" + finishTime + '\'' +
              ", duration='" + duration + '\'' +
              ", distance='" + distance + '\'' +
              ", calories='" + calories + '\'' +
              ", date='" + date + '\'' +
              ", note='" + note + '\'' +
              ", startPoint=" + startPoint +
              ", finishPoint=" + finishPoint +
              ", coordinates=" + coordinates +
              '}';
   }
}
