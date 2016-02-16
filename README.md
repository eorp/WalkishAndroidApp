# WalkishAndroidApp
Final project for Android for Java Developers course - GPS tracker Android Applicaton

Quite a few people use tracking application on their Android devices. Personally, I use  Runtastic application for tracking my, mostly walking, sessions. Since the free version of Runtastic is full of ads, I decided to implement a similar application, Walkish, as a final project for this course. 

The main purpose of Walkish application is tracking walking activities by saving one’s GPS locations and displaying the route in real time on Google maps. Each walking session is saved in a database and can be viewed at any time by displaying information about that session including the route on the map.

Application functionality include the following:
-   Start, pause, resume and stop GPS tracking;
-	Draw the route on Google maps in real time;
-	Display duration, distance and calories burnt;
-	Save information about each activity in a database;
-	Retrieve and display saved information for each activity;
-	Delete selected saved activity;
-	Run GPS tracking in the background;  
-	Provide notification service to enable returning to the application from the background;
-	Configurable settings.

Google Maps API key is located in app\src\debug\res\values\google_maps_api.xml.

# Application Functionality Overview
## Home Screen
Home screen (MainActivity.java) serves as a landing page for the application. From that page one can navigate to sessions history list, application settings (these options are available from the drop down menu) and to the page where one can start tracking the walking session (this option available by clicking the button at the bottom of the screen).
Current date and time are displayed on the home page.

![1](https://cloud.githubusercontent.com/assets/14193564/13066556/37834800-d45f-11e5-85aa-135edc5bcc82.png)

Figure 1 Home Screen

## Settings Screen

This page (PreferencesActivity.java) allows the user to customise certain values, such as:
-	Select location updates intervals;
-	Select  map type;
-	Select colour for drawing the route;
-	Provide user’s weight (needed for calories calculation, default value is 60 kg)

Location update intervals control the frequency of location updates requests during the walking session. More frequent updates consume more battery power, however, provide more accurate coordinates to display the route.

![2](https://cloud.githubusercontent.com/assets/14193564/13066338/cf97c1fa-d45c-11e5-94b4-23ae9b71ad2b.png)![3](https://cloud.githubusercontent.com/assets/14193564/13066342/cfaad222-d45c-11e5-82a2-db4d2e840e60.png)

Figure 2 Settings Screen and Update Intervals Settings Option

Map type refers to appearance of Google maps. Route colour allows the user to change colour of the path drawn on the map to show the route of the walking session.

![4](https://cloud.githubusercontent.com/assets/14193564/13066343/cfafbf76-d45c-11e5-8432-5a78b70a8671.png)
![5](https://cloud.githubusercontent.com/assets/14193564/13066341/cfa2bd3a-d45c-11e5-930f-cb598b84072b.png)

Figure 3 Map Types and Route Colour Settings Options

As mentioned already, weight of the user is required for calculation the estimated calories burnt during the walking session. Default value is 60 kg. Only numerical input is available for entering this value. There is a check performed on user’s input to ensure that entered value is within reasonable range (15 to 180 kg) and that it is not left blank. In such cases, a warning message will be displayed in form of Toast and the weight value will be automatically reverted to default value. 

![6](https://cloud.githubusercontent.com/assets/14193564/13066339/cf9e14ce-d45c-11e5-9bbe-c4dcd7bba1b2.png)

Figure 4 Weight Settings Option

## Walking Session Screen

This screen/activity (WalkingMapActivity.java) provides an option to start a walking session. This application relies on GPS to get information about locations, therefore, GPS should be enabled on the device. A warning is displayed, if that is not the case, giving options to go to device’s setting to turn GPS on or to return to previous screen if the user decides not to proceed to Settings.
Pressing “Start Activity” button will start tracking of the walking session. Duration of the session, as well as distance covered and calories burnt will be updated during the session to reflect progress. There are options to pause, resume and stop the session. This options are locked initially to prevent accidentally pressing on undesired options. The lock button indicates buttons status (grey background means inactivity, coloured background – active status).
Before session commencement, a menu is available to the user to access settings, home or history screens. After session start, this option is no longer available to ensure correct functionality of the application.
Once session has started, a notification is fired, therefore, if the user navigates away from the session tracking screen, it can be accessed again by clicking on that notification. Notification remains on until the session is stopped.
When the user decides to stop the session, all required information about that session is stored in a database.

![7](https://cloud.githubusercontent.com/assets/14193564/13066605/ccd90a70-d45f-11e5-9919-79b6ca01ad6f.png)
![8](https://cloud.githubusercontent.com/assets/14193564/13066604/ccd7d7cc-d45f-11e5-8d74-387a7e8504c1.png)

Figure 5 Walking Session Screen and Message Displayed if GPS is Turned Off

![9](https://cloud.githubusercontent.com/assets/14193564/13066349/cfe4e53e-d45c-11e5-8dcf-62efdbc4cb52.png)

Figure 6 Walking Session Started

## History Screen

Saved sessions can be viewed on History screen (RoutesHistoryActivity.java). This page can be accessed via menu available on other pages. Once a session tracking is stopped by the user, history screen will be displayed. Pressing and holding on a list element will bring up a dialog allowing the user to delete selected item. Clicking on the element in the history list will bring a page for selected session with more detailed information.

![10](https://cloud.githubusercontent.com/assets/14193564/13066350/cfed9bb6-d45c-11e5-9852-d6addaeee5a5.png)
![11](https://cloud.githubusercontent.com/assets/14193564/13066348/cfe2cb00-d45c-11e5-95f4-8ea6620f4d90.png)

Figure 7 Sessions History Screen and Option to Delete an Entry

## Session Details Screen

This page (HistoryDetailsActivity.java) displays saved information about selected session including session’s path drawn on the map.

![12](https://cloud.githubusercontent.com/assets/14193564/13066608/d8cf3318-d45f-11e5-9e0b-2314d6bb82ae.png)


Figure 8 Saved Session Details Screen


