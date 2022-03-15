package com.designmyfeed.app.fragment;


import static android.os.Environment.DIRECTORY_DOCUMENTS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings.PluginState;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.designmyfeed.app.App;
import com.designmyfeed.app.Config;
import com.designmyfeed.app.activity.Share_activity;
import com.designmyfeed.app.util.GetFileInfo;
import com.designmyfeed.app.R;
import com.designmyfeed.app.widget.webview.WebToAppChromeClient;
import com.designmyfeed.app.widget.webview.WebToAppWebClient;
import com.designmyfeed.app.activity.MainActivity;
import com.designmyfeed.app.widget.AdvancedWebView;
import com.designmyfeed.app.widget.scrollable.ToolbarWebViewScrollListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;


public class WebFragment extends Fragment implements AdvancedWebView.Listener, SwipeRefreshLayout.OnRefreshListener{

    //Layouts
    public FrameLayout rl;
    public AdvancedWebView browser;
    public SwipeRefreshLayout swipeLayout;
    public ProgressBar progressBar;

    //WebView Clients
    public WebToAppChromeClient chromeClient;
    public WebToAppWebClient webClient;

    //WebView Session
    public String mainUrl = null;
    static String URL = "url";
    public int firstLoad = 0;
    private boolean clearHistory = false;
    String today="",after_fiftheen="";
    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.
    public static File SHARE_FILE_PATH = null;
    public static String SHARE_IMAGE_URL = "";

    String[] permissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public WebFragment() {
        // Required empty public constructor
    }

    public static WebFragment newInstance(String url) {
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    public void setBaseUrl(String url){
        this.mainUrl = url;
        this.clearHistory = true;
        browser.loadUrl(mainUrl);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && mainUrl == null) {
            mainUrl = getArguments().getString(URL);
            firstLoad = 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rl = (FrameLayout) inflater.inflate(R.layout.fragment_observable_web_view, container,
                false);

        progressBar = (ProgressBar) rl.findViewById(R.id.progressbar);
        browser = (AdvancedWebView) rl.findViewById(R.id.scrollable);
        swipeLayout = (SwipeRefreshLayout) rl.findViewById(R.id.swipe_container);
        SHARE_IMAGE_URL = "";
        return rl;
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Config.PULL_TO_REFRESH)
            swipeLayout.setOnRefreshListener(this);
        else
            swipeLayout.setEnabled(false);

        // Setting the webview listeners
        browser.setListener(this, this);

        // Setting the scroll listeners (if applicable)
        if (MainActivity.getCollapsingActionBar()) {

            ((MainActivity) getActivity()).showToolbar(this);

            browser.setOnScrollChangeListener(browser, new ToolbarWebViewScrollListener() {
                @Override
                public void onHide() {
                    ((MainActivity) getActivity()).hideToolbar();
                }

                @Override
                public void onShow() {
                    ((MainActivity) getActivity()).showToolbar(WebFragment.this);
                }
            });

        }

        // set javascript and zoom and some other settings
        browser.requestFocus();
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setBuiltInZoomControls(false);
        browser.getSettings().setAppCacheEnabled(true);
        browser.getSettings().setDatabaseEnabled(true);
        browser.getSettings().setDomStorageEnabled(true);
        // Below required for geolocation
        browser.setGeolocationEnabled(true);
        // 3RD party plugins (on older devices)
        browser.getSettings().setPluginState(PluginState.ON);
        /*PKUMAR 08-07-2020*/

        /**/

        if (Config.MULTI_WINDOWS) {
            browser.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            browser.getSettings().setSupportMultipleWindows(true);
        }


        webClient = new WebToAppWebClient(this, browser);

        browser.setWebViewClient(webClient);

        chromeClient = new WebToAppChromeClient(this, rl, browser, swipeLayout, progressBar);
        browser.setWebChromeClient(chromeClient);

        // load url (if connection available
        if (webClient.hasConnectivity(mainUrl, true)) {
            String pushurl = ((App) getActivity().getApplication()).getPushUrl();
            if (pushurl != null){
                browser.loadUrl(pushurl);
            } else {
                browser.loadUrl(mainUrl);
            }
        } else {
            try {
                ((MainActivity) getActivity()).hideSplash();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onRefresh() {
        browser.reload();
    }

    @SuppressLint("NewApi")
    @Override
    public void onPause() {
        super.onPause();
        browser.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        browser.onDestroy();
    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();
        browser.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    public void onDownloadRequested(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        if (!checkPermissions()) return;


        try {
            //image/jpeg
            //application/mp4
            //audio/mpeg
            Log.e("mimetype 2",mimetype+"");
            Log.e("url 2",url+"");

            SHARE_IMAGE_URL = "";
            SHARE_IMAGE_URL = url+"";

            if(mimetype.equalsIgnoreCase("image/jpeg"))
            {
                String filename = null;
                try {

                    filename = new GetFileInfo().execute(url).get();
                    Log.e("filename 2",filename+"");

                    if (filename != null) { //if filename null than executive

                        saveImage(App.image);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("errrrr",e+"");
                }

            }else if(mimetype.equalsIgnoreCase("application/mp4"))
            {
                DownloadManager mdDownloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                File wallpaperDirectory;// = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    wallpaperDirectory = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ "/"+"DesignMyFeed" );
                } else {
                    wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + "/"+"DesignMyFeed");
                }
                // have the object build the directory structure, if needed.
                if (!wallpaperDirectory.exists()) {
                    wallpaperDirectory.mkdirs();
                }

                String filename_video=Calendar.getInstance().getTimeInMillis() + ".mp4";

                File destinationFile = new File(wallpaperDirectory, filename_video);
                request.setDescription(filename_video+"");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationUri(Uri.fromFile(destinationFile));
                mdDownloadManager.enqueue(request);

                SHARE_FILE_PATH = new File(destinationFile.getAbsolutePath());
                Intent i = new Intent(getActivity(), Share_activity.class);
                i.putExtra("type","video");
                startActivity(i);

            }else if(mimetype.equalsIgnoreCase("audio/mpeg"))
            {
                DownloadManager mdDownloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                File wallpaperDirectory;// = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    wallpaperDirectory = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ "/"+"DesignMyFeed" );
                } else {
                    wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + "/"+"DesignMyFeed");
                }
                // have the object build the directory structure, if needed.
                if (!wallpaperDirectory.exists()) {
                    wallpaperDirectory.mkdirs();
                }
                String filename_video=Calendar.getInstance().getTimeInMillis() + ".mp3";
                File destinationFile = new File(wallpaperDirectory, filename_video);
                // File destinationFile = new File(Environment.getExternalStorageDirectory(), getFileName(url,"mp4"));
                request.setDescription(filename_video+"");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationUri(Uri.fromFile(destinationFile));
                mdDownloadManager.enqueue(request);

                SHARE_FILE_PATH = new File(destinationFile.getAbsolutePath());
                Intent i = new Intent(getActivity(), Share_activity.class);
                i.putExtra("type","audio");
                startActivity(i);

            }


        }catch (Exception e)
        {

            Log.e("errrrr",e+"");
        }

    }

    private static boolean hasPermissionToDownload(final Activity context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED )
            return true;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.download_permission_explaination);
        builder.setPositiveButton(R.string.common_permission_grant, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Fire off an async request to actually get the permission
                // This will show the standard permission request dialog UI
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    context.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        return false;
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {


            result = ContextCompat.checkSelfPermission(getActivity(),p);
            //Log.e("result",result+"");
            //Log.e("permi",result+"");
            if (result != PackageManager.PERMISSION_GRANTED) {

                //Log.e("per 2",p+"");
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                    Log.e("cccc","hhhh");

                } else {
                    // no permissions granted.
                    Log.e("cccc","hh");
                }
                return;
            }
        }
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        if (firstLoad == 0 && MainActivity.getCollapsingActionBar()){
            ((MainActivity) getActivity()).showToolbar(this);
            firstLoad = 1;
        } else if (firstLoad == 0){
            firstLoad = 1;
        }
    }

    @Override
    public void onPageFinished(String url) {
        if (!url.equals(mainUrl)
                && getActivity() != null
                && getActivity() instanceof MainActivity
                && Config.INTERSTITIAL_PAGE_LOAD)
            ((MainActivity) getActivity()).showInterstitial();

        try {
            ((MainActivity) getActivity()).hideSplash();
        } catch (Exception e){
            e.printStackTrace();
        }

        if (clearHistory)
        {
            clearHistory = false;
            browser.clearHistory();
        }

        hideErrorScreen();
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onExternalPageRequest(String url) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        browser.onActivityResult(requestCode, resultCode, data);
    }

    // sharing
    public void shareURL() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String appName = getString(R.string.app_name);
        shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_body), browser.getTitle(), appName + " https://play.google.com/store/apps/details?id=" + getActivity().getPackageName()));
        startActivity(Intent.createChooser(shareIntent, getText(R.string.sharetitle)));
    }

    public void showErrorScreen(String message) {
        final View stub = rl.findViewById(R.id.empty_view);
        stub.setVisibility(View.VISIBLE);

        ((TextView) stub.findViewById(R.id.title)).setText(message);
        stub.findViewById(R.id.retry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (browser.getUrl() == null) {
                    browser.loadUrl(mainUrl);
                } else {
                    browser.loadUrl("javascript:document.open();document.close();");
                    browser.reload();
                }
            }
        });
    }

    public void hideErrorScreen(){
        final View stub = rl.findViewById(R.id.empty_view);
        if (stub.getVisibility() == View.VISIBLE)
        stub.setVisibility(View.GONE);
    }

    public String saveImage(Bitmap myBitmap) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        Log.e("Build.VERSION.SDK_INT", Build.VERSION.SDK_INT + ""); //29 //30
        Log.e("Build.VERSION_CODES.R", Build.VERSION_CODES.R + "");//30  //30

        File wallpaperDirectory;// = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);



        // wallpaperDirectory = new File(Environment.getExternalStorageDirectory().getPath() + IMAGE_DIRECTORY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wallpaperDirectory = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ "/"+"DesignMyFeed" );
        } else {
            wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + "/"+"DesignMyFeed");
        }

        /*if (Build.VERSION_CODES.R > Build.VERSION.SDK_INT) {

            wallpaperDirectory = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS)
                    .getPath() + "/DesignMyFeed");

        } else {

            Log.e("lll", "--ll");
            wallpaperDirectory = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS)
                    .getPath() + "/DesignMyFeed");
        }*/
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".jpeg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(getActivity(), new String[]{f.getPath()}, new String[]{"image/jpeg"}, null);
            fo.close();

            // user_profile = 1;

            try {
                //et_multiple_photo.setText(f.getAbsolutePath());
                if (f.exists()) {
                    Log.e("TAG", "File Saved::--->" + f.getAbsolutePath());

                    SHARE_FILE_PATH = f;
                    Intent i = new Intent(getActivity(), Share_activity.class);
                    i.putExtra("type","image");
                    startActivity(i);


                }
            } catch (Exception e) {

                Log.e("errr ex",e+"");
            }

            return f.getAbsolutePath();
        } catch (Exception e1) {

            Log.e("error",e1+"");
        }
        return "";
    }

}
