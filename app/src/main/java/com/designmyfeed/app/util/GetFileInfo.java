package com.designmyfeed.app.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.designmyfeed.app.App;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sherdle
 */
public class GetFileInfo extends AsyncTask<String, Integer, String> {

    protected String doInBackground(String... urls) {
        URL url;
        String filename = null;
        try {
            url = new URL(urls[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            conn.setInstanceFollowRedirects(false);

            String depo = conn.getHeaderField("Content-Disposition");
            //Log.e("depo",depo+"");

            if (depo == null)
                return null;
            String depoSplit[] = depo.split("filename=");
            filename = depoSplit[1].replace("filename=", "").replace("\"", "").trim();

            try {
                URL url2 = new URL(url+"");
                App.image = BitmapFactory.decodeStream(url2.openConnection().getInputStream());

                //Log.e("image 1",App.image+"");

            } catch(Exception e) {
                Log.e("errr",e+"");
            }

        } catch (Exception e) {
            Log.e("errr 2",e+"");
        }
        return filename;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        // use result as file name
    }



}