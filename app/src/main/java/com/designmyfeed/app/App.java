package com.designmyfeed.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.designmyfeed.app.activity.MainActivity;

import org.json.JSONObject;

public class App extends MultiDexApplication {

    private String push_url = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    public static Bitmap image;
    private static App mInstance;

    @Override public void onCreate() {
        super.onCreate();

        if (Config.ANALYTICS_ID.length() > 0) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
        mInstance = this;
        //OneSignal Push
        if (!TextUtils.isEmpty(getString(R.string.onesignal_app_id)))
            OneSignal.init(this, "REMOTE", getString(R.string.onesignal_app_id), new NotificationHandler());
    }

    // This fires when a notification is opened by tapping on it or one is received while the app is running.
    private class NotificationHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            try {
                JSONObject data = result.notification.payload.additionalData;

                String webViewUrl = (data != null) ? data.optString("url", null) : null;
                String browserUrl1 = result.notification.payload.launchURL.substring(6);
                String browserUrl ="https://designmyfeed.com/"+browserUrl1;

                /*  pkumar 19-3-2021 below code to check notification browserUrl by Toast*/
                //Context context = getApplicationContext();
                //CharSequence text = "Hello toast!";
                //int duration = Toast.LENGTH_LONG;
                //Toast toast = Toast.makeText(context, browserUrl, duration);
                //toast.show();
                push_url = browserUrl;

                if (webViewUrl != null || browserUrl != null) {
                    if (browserUrl != null || result.notification.isAppInFocus) {
                        browserUrl = (browserUrl == null) ? webViewUrl : browserUrl;
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUrl));
                        startActivity(browserIntent);
                        Log.v("INFO", "Received notification while app was on foreground or url for browser");
                    } else {
                        push_url = webViewUrl;
                    }
                } else if (!result.notification.isAppInFocus) {
                    Intent mainIntent;
                    mainIntent = new Intent(App.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                }


            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }

    public synchronized String getPushUrl(){
        String url = push_url;
        push_url = null;
        return url;
    }

    public synchronized void setPushUrl(String url){
        this.push_url = url;
    }

    public static synchronized App getInstance() {
        return mInstance;
    }
} 