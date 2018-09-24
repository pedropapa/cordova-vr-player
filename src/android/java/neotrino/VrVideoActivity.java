package com.neotrino;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;

import android.widget.Toast;
import com.github.ybq.android.spinkit.style.FadingCircle;
import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.google.vr.sdk.widgets.video.VrVideoView.Options;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

import java.io.IOException;

public class VrVideoActivity extends Activity {
  private static final String TAG = VrVideoActivity.class.getSimpleName();

  public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
  public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
  public static final int LOAD_VIDEO_STATUS_ERROR = 2;

  private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

  /**
   * Tracks the file to be loaded across the lifetime of this app.
   **/
  private Uri fileUri;
  private Uri fallbackVideoUri;

  private ProgressBar progressBar;
  private FadingCircle fadingCircle;

  /**
   * Configuration information for the video.
   **/
  private Options videoOptions = new Options();

  private VideoLoaderTask backgroundVideoLoaderTask;
  private Boolean fallbackVideoLoaded = false;

  private VrVideoView videoWidgetView;
  Activity activity = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String package_name = getApplication().getPackageName();
    Resources resources = getApplication().getResources();

    setContentView(resources.getIdentifier("main_layout", "layout", package_name));

    loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

    // Bind input and output objects for the view.
    videoWidgetView = (VrVideoView) findViewById(resources.getIdentifier("video_view", "id", package_name));
    videoWidgetView.setEventListener(new ActivityEventListener());
    videoWidgetView.setVisibility(View.INVISIBLE);

    videoWidgetView.setInfoButtonEnabled(false);
    videoWidgetView.setStereoModeButtonEnabled(false);
    videoWidgetView.setTouchTrackingEnabled(false);
    videoWidgetView.setTransitionViewEnabled(false);
    videoWidgetView.setFullscreenButtonEnabled(false);

    progressBar = (ProgressBar) findViewById(resources.getIdentifier("spin_kit", "id", package_name));
    fadingCircle = new FadingCircle();

//        progressBar.setVisibility(View.VISIBLE);
    progressBar.setIndeterminateDrawable(fadingCircle);

    activity = this;
    // Initial launch of the app or an Activity recreation due to rotation.
    handleIntent(getIntent());
  }

  /**
   * Called when the Activity is already running and it's given a new intent.
   */
  @Override
  protected void onNewIntent(Intent intent) {
    Log.i(TAG, this.hashCode() + ".onNewIntent()");
    // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
    // future invocations.
    setIntent(intent);
    // Load the new image.
    handleIntent(intent);
  }

  public int getLoadVideoStatus() {
    return loadVideoStatus;
  }


  private void launchVideoLoader(Uri fileUri) {
    // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
    // take 100s of milliseconds.
    if (backgroundVideoLoaderTask != null) {
      // Cancel any task from a previous intent sent to this activity.
      backgroundVideoLoaderTask.cancel(true);
    }
    backgroundVideoLoaderTask = new VideoLoaderTask();
    backgroundVideoLoaderTask.execute(Pair.create(fileUri, videoOptions));
  }

  private void handleIntent(Intent intent) {
    Bundle extras = intent.getExtras();
    if (extras != null) {
      fileUri = Uri.parse(extras.getString("videoUrl"));
      String fallbackVideo = extras.getString("fallbackVideo");
      String videoType = extras.getString("videoType");
      String displayMode = extras.getString("displayMode");

      Log.d(TAG, "fileUri " + fileUri);
      Log.d(TAG, "fallbackVideo " + fallbackVideo);
      Log.d(TAG, "videoType " + videoType);
      Log.d(TAG, "displayMode " + displayMode);

      if (fallbackVideo != null)
        fallbackVideoUri = Uri.parse(fallbackVideo);

      if (videoType != null) {
        if (videoType.equalsIgnoreCase("MONO")) {
          videoOptions.inputType = VrVideoView.Options.TYPE_MONO;
        } else if (videoType.equalsIgnoreCase("STEREO")) {
          videoOptions.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER;
        } else {
          Log.d(TAG, "Unsupported video type, fallback to MONO");

          sendPluginError("VIDEO_TYPE_UNSUPPORTED");

          videoOptions.inputType = VrVideoView.Options.TYPE_MONO;
        }
      }

      if (displayMode != null) {
        if (displayMode.equalsIgnoreCase("Fullscreen")) {
          videoWidgetView.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_MONO);
        } else if (displayMode.equalsIgnoreCase("FullscreenVR")) {
          videoWidgetView.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_STEREO);
        } else {
          Log.d(TAG, "Unsupported display mode, fallback to MONO");

          sendPluginError("VIDEO_TYPE_UNSUPPORTED");

          videoWidgetView.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_MONO);
        }
      }
    } else {
      fileUri = null;
    }

    launchVideoLoader(fileUri);

  }

  @Override
  protected void onPause() {
    super.onPause();
    // Prevent the view from rendering continuously when in the background.
    videoWidgetView.pauseRendering();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // Resume the 3D rendering.
    videoWidgetView.resumeRendering();
  }

  @Override
  protected void onDestroy() {
    // Destroy the widget and free memory.
    videoWidgetView.shutdown();
    super.onDestroy();
  }

  public void sendPluginInformation(String message) {
    JSONArray data = new JSONArray();
    data.put(message);

    PluginResult pr = new PluginResult(PluginResult.Status.OK, data);
    pr.setKeepCallback(true);

    Log.i(TAG, ">>>> PLUGIN " + message);

    GoogleVRPlayer.callbackContext.sendPluginResult(pr);
  }

  public void sendPluginInformation(String message, long duration) {
    JSONArray data = new JSONArray();
    data.put(message);
    data.put(duration);

    PluginResult pr = new PluginResult(PluginResult.Status.OK, data);
    pr.setKeepCallback(true);

    GoogleVRPlayer.callbackContext.sendPluginResult(pr);
  }

  public void terminate() {
    Log.d(TAG, "FINISH PLAYING");
//        videoWidgetView.pauseVideo();
//        videoWidgetView.setVisibility(View.INVISIBLE);
//        videoWidgetView.setDisplayMode(VrWidgetView.DisplayMode.EMBEDDED);
//        videoWidgetView.pauseRendering();
    activity.finish();
  }

  public void sendPluginError(String message) {
    PluginResult pr = new PluginResult(PluginResult.Status.ERROR, message);
    pr.setKeepCallback(true);

    GoogleVRPlayer.callbackContext.sendPluginResult(pr);
  }

  /**
   * Listen to the important events from widget.
   */
  private class ActivityEventListener extends VrVideoEventListener {
    /**
     * Called by video widget on the UI thread when it's done loading the video.
     */
    @Override
    public void onLoadSuccess() {
      sendPluginInformation("FINISHED_LOADING");

      Log.i(TAG, "Sucessfully loaded video ");

//            videoWidgetView.seekTo(0);
//            videoWidgetView.playVideo();
    }

    @Override
    public void onDisplayModeChanged(int newDisplayMode) {
      Log.i(TAG, "onDisplayModeChanged " + newDisplayMode);

      if (newDisplayMode != VrWidgetView.DisplayMode.FULLSCREEN_STEREO && newDisplayMode != VrWidgetView.DisplayMode.FULLSCREEN_MONO) {
        activity.finish();
      }
    }

    /**
     * Called by video widget on the UI thread on any asynchronous error.
     */
    @Override
    public void onLoadError(String errorMessage) {
      // An error here is normally due to being unable to decode the video format.
      sendPluginError("CANNOT_DECODE");

      //Attempt to load fallback video
      if (!fallbackVideoLoaded && fallbackVideoUri != null) {
        fallbackVideoLoaded = true;
        launchVideoLoader(fallbackVideoUri);
      } else {
        Toast.makeText(
                activity, "Um erro ocorreu ao carregar o vídeo", Toast.LENGTH_LONG)
                .show();

        activity.finish();
      }
      Log.e(TAG, "Error loading video: " + errorMessage);
    }


    /**
     * Update the UI every frame.
     */
    @Override
    public void onNewFrame() {
      sendPluginInformation("DURATION_UPDATE", videoWidgetView.getCurrentPosition());
    }

    /**
     * Make the video play in a loop. This method could also be used to move to the next video in
     * a playlist.
     */
    @Override
    public void onCompletion() {
      Log.e(TAG, "Video finished: " + videoWidgetView.getCurrentPosition());

      sendPluginInformation("FINISHED_PLAYING", videoWidgetView.getDuration());
      finish();
    }
  }

  /**
   * Helper class to manage threading.
   */
  class VideoLoaderTask extends AsyncTask<Pair<Uri, VrVideoView.Options>, Void, Boolean> {
    Uri fileUri = null;


    @Override
    protected Boolean doInBackground(Pair<Uri, VrVideoView.Options>... pairs) {
      fileUri = pairs[0].first;
      return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
      try {
        Log.i(TAG, "onPostExecute " + fileUri);

//                cordova.getActivity().setContentView(R.layout.spinner_mono);
//                LinearLayout l = (LinearLayout) View.inflate(cordova.getActivity(), R.layout.spinner_mono, null);
//                videoView.addView(l);

        videoWidgetView.setEventListener(new ActivityEventListener());
        videoWidgetView.loadVideo(fileUri, videoOptions);

        sendPluginInformation("START_LOADING");
      } catch (IOException e) {
        Log.i(TAG, "onPostExecute CANNOT_DECODE");
        sendPluginError("CANNOT_DECODE");

        // Since this is a background thread, we need to switch to the main thread to show a toast.
        videoWidgetView.post(new Runnable() {
          @Override
          public void run() {
//                        Toast
//                                .makeText(cordova.getActivity(), "Error opening file. ", Toast.LENGTH_LONG)
//                                .show();
          }
        });
        Log.e(TAG, "Could not open video: " + e);
      }
      super.onPostExecute(aBoolean);
    }
  }
}