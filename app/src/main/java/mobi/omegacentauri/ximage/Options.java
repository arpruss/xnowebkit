package mobi.omegacentauri.ximage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import de.robv.android.xposed.XposedBridge;

public class Options extends PreferenceActivity {
    public static final String PREF_DETECT_BLOCK = "detect_block";
    static final String PREFS = "preferences";
    //    static final String PREF_OUTLOOK_COMPOSE = "outlook_compose";
    private boolean killProcess = false;

    private void mustExit() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Activate in Xposed Manager");
        alertDialog.setMessage("Before you can use the " + getApplicationInfo().nonLocalizedLabel + " module, you need to activate it in your Xposed Manager.");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Options.this.finish();
                    } });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Options.this.finish();
            } });
        alertDialog.show();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.v("ximage", "killing");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            /*
            Make sure you have
            <meta-data android:name="xposedminversion"
            android:value="93" />
            <meta-data android:name="xposedsharedprefs"
            android:value="true"/>
            in AndroidManifest.xml
             */
            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(PREFS);
            prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.options);
            killProcess = false;
        }
        catch(SecurityException e) {
            Log.e("ximage", "cannot make prefs world readable");
            killProcess = true;
            mustExit();
        }

        Window w = getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            w.setNavigationBarColor(Color.BLACK);
        }
    }
}