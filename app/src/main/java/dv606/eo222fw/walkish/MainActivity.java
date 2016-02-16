package dv606.eo222fw.walkish;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
   //UI elements
   private TextView txtDate;
   private TextView txtTime;
   private Button btnStart;
   //thread
   private Thread myThread = null;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      //get reference to UI elements
      txtDate = (TextView) findViewById(R.id.txt_date);
      txtTime = (TextView) findViewById(R.id.txt_time);
      btnStart = (Button) findViewById(R.id.btn_start);
      btnStart.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent walkIntent = new Intent(MainActivity.this, WalkingMapActivity.class);
            //Intent walkIntent = new Intent(MainActivity.this, RoutesHistoryActivity.class);
            startActivity(walkIntent);
         }
      });
      //display today's date
      txtDate.setText(Utils.getFullDateString());
      //show time
      showTime();

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();
      if (id == R.id.choose_settings) {
         startActivity(new Intent(this, PreferencesActivity.class));
      } else if (id == R.id.choose_history) {
         startActivity(new Intent(this,RoutesHistoryActivity.class));
      } else if (id == R.id.choose_session) {
         startActivity(new Intent(this,WalkingMapActivity.class));
      }

      return super.onOptionsItemSelected(item);
   }

   /**
    * display time, update it every second
    */
   private void showTime() {
      //initialise thread to update time every second
      Runnable myRunnableThread = new CountDownRunner();
      myThread = new Thread(myRunnableThread);
      //start thread
      myThread.start();
   }

   @Override
   protected void onPause() {
      super.onPause();
      if (myThread != null)
         myThread.interrupt();

   }

   @Override
   protected void onStart() {
      super.onStart();
      showTime();

   }

   @Override
   protected void onResume() {
      super.onResume();


   }

   @Override
   protected void onStop() {
      super.onStop();
      if (myThread != null)
         myThread.interrupt();
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
   }

   /**
    * run UI thread to update time every second
    */
   public void doWork() {
      runOnUiThread(new Runnable() {
         public void run() {
            try {
               //display current time
               txtTime.setText(Utils.getCurrentTimeString());
            } catch (Exception e) {
            }
         }
      });
   }

   private class CountDownRunner implements Runnable {
       @Override
      public void run() {
         while (!Thread.currentThread().isInterrupted()) {
            try {
               doWork();
               Thread.sleep(1000); // pause for 1 second
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            } catch (Exception e) {
            }
         }
      }
   }

}
