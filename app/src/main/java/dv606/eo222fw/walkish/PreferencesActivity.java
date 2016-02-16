package dv606.eo222fw.walkish;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Created by Evgeniya O'Regan Pevchikh on 30/01/2016.
 */
public class PreferencesActivity extends PreferenceActivity {
   private static Context context;
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      context = this;
      getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

   }
   /**
    * A preference value change listener that updates the preference's summary
    * to reflect its new value.
    */
   private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object value) {
         String stringValue = value.toString();

         if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

         } else if (preference instanceof RingtonePreference) {
            // For ringtone preferences, look up the correct display value
            // using RingtoneManager.
            if (TextUtils.isEmpty(stringValue)) {
               // Empty values correspond to 'silent' (no ringtone).
               preference.setSummary(R.string.pref_ringtone_silent);

            } else {
               Ringtone ringtone = RingtoneManager.getRingtone(
                       preference.getContext(), Uri.parse(stringValue));

               if (ringtone == null) {
                  // Clear the summary if there was a lookup error.
                  preference.setSummary(null);
               } else {
                  // Set the summary to reflect the new ringtone display
                  // name.
                  String name = ringtone.getTitle(preference.getContext());
                  preference.setSummary(name);
               }
            }

         } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
         }
         return true;
      }
   };

   /**
    * Binds a preference's summary to its value. More specifically, when the
    * preference's value is changed, its summary (line of text below the
    * preference title) is updated to reflect the value. The summary is also
    * immediately updated upon calling this method. The exact display format is
    * dependent on the type of preference.
    *
    * @see #sBindPreferenceSummaryToValueListener
    */
   private static void bindPreferenceSummaryToValue(Preference preference) {
      // Set the listener to watch for value changes.
      preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

      // Trigger the listener immediately with the preference's
      // current value.
      sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
              PreferenceManager
                      .getDefaultSharedPreferences(preference.getContext())
                      .getString(preference.getKey(), ""));
   }


   public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
   {
      @Override
      public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences_walktastic);
         //register on shared preferences change listener
         getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
         //Log.e("prefs frag", "registered for listener");
      }
      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
         //Log.e("prefs frag","changed, pref: "+sharedPreferences+", string: "+s);

         checkEditTextValue(findPreference(s), s);
      }

      /**
       * ensure that weight value provided by user is valid
       * otherwise assign default value and show warning
       * @param preference
       * @param key
       */
      private void checkEditTextValue(Preference preference, String key){
         if (preference instanceof EditTextPreference) {
            EditTextPreference weight = (EditTextPreference) preference;
            if(!checkWeightRange(weight.getText())){
               Toast.makeText(context,"Weight value should not be null and should be within reasonable range. ",Toast.LENGTH_SHORT).show();
               weight.setText("60");
            }
            //Log.e("prefs frag", "getText: " + weight.getText() + ", getedittext: " + weight.getEditText());


         }
      }

      /**
       * check if weight value is not empty and within certain range (15 to 180 kg)
       * @param value edit text value
       * @return true if not empty and within range, false otherwise
       */
      private boolean checkWeightRange(String value){
         if(TextUtils.isEmpty(value))
            return false;
         else{
            return Utils.isWithinRange(15,180,value);
         }
      }
   }

}
