/**
 */
package com.neotrino;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;
import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import java.io.IOException;

public class GoogleVRPlayer extends CordovaPlugin {
  private static final String TAG = "GoogleVRPlayer";
  private VrVideoView videoView;

  public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
  public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
  public static final int LOAD_VIDEO_STATUS_ERROR = 2;

  public static final int PLAY_VIDEO_STATUS_UNKNOWN = 0;
  public static final int PLAY_VIDEO_STATUS_PLAYING = 1;

  private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;
  private int playVideoStatus = PLAY_VIDEO_STATUS_UNKNOWN;

  /**
   * Tracks the file to be loaded across the lifetime of this app.
   **/
  private Uri fileUri;
  private Uri fallbackVideoUri;

  /**
   * Configuration information for the video.
   **/
  private VrVideoView.Options videoOptions = new VrVideoView.Options();

  private GoogleVRPlayer.VideoLoaderTask backgroundVideoLoaderTask;
  private Boolean fallbackVideoLoaded = false;

  private CallbackContext callbackContext;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    videoView = new VrVideoView(cordova.getActivity());

    videoView.setInfoButtonEnabled(false);
    videoView.setStereoModeButtonEnabled(false);
    videoView.setTouchTrackingEnabled(false);
    videoView.setTransitionViewEnabled(false);
    videoView.setFullscreenButtonEnabled(false);

    Log.d(TAG, "Initializing GoogleVRPlayer");
  }

  public void sendPluginInformation(String message) {
    JSONArray data = new JSONArray();
    data.put(message);

    PluginResult pr = new PluginResult(PluginResult.Status.OK, data);
    pr.setKeepCallback(true);

    callbackContext.sendPluginResult(pr);
  }

  public void finish() {
    Log.d(TAG, "FINISH PLAYING");
    videoView.pauseVideo();
    videoView.setVisibility(View.INVISIBLE);
    videoView.setDisplayMode(VrWidgetView.DisplayMode.EMBEDDED);
    videoView.pauseRendering();
  }

  public void sendPluginInformation(String message, long duration) {
    JSONArray data = new JSONArray();
    data.put(message);
    data.put(duration);

    PluginResult pr = new PluginResult(PluginResult.Status.OK, data);
    pr.setKeepCallback(true);

    callbackContext.sendPluginResult(pr);
  }

  public void sendPluginError(String message) {
    PluginResult pr = new PluginResult(PluginResult.Status.ERROR, message);
    pr.setKeepCallback(true);

    callbackContext.sendPluginResult(pr);
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if (action.equals("loadVideo")) {
      String videoUrl = args.getString(0);
      String fallbackVideoUrl = args.getString(1);
      String videoType = args.getString(2);

      fallbackVideoUri = Uri.parse(fallbackVideoUrl);
      fileUri = Uri.parse(videoUrl);
      this.callbackContext = callbackContext;

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

      cordova.getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          launchVideoLoader(fileUri);
        }
      });
    }

    if (action.equals("playVideo")) {
      final String displayMode = args.getString(0);

      cordova.getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          sendPluginInformation("START_PLAYING");

          if (displayMode.equalsIgnoreCase("Fullscreen")) {
            videoView.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_MONO);
          } else if (displayMode.equalsIgnoreCase("FullscreenVR")) {
            videoView.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_STEREO);
          } else {
            Log.d(TAG, "Unsupported display mode, fallback to MONO");

            sendPluginError("VIDEO_TYPE_UNSUPPORTED");

            videoView.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_MONO);
          }

          playVideoStatus = PLAY_VIDEO_STATUS_PLAYING;
          videoView.seekTo(0);
          videoView.playVideo();
        }
      });
    }
    return true;
  }

  private void launchVideoLoader(Uri fileUri) {
    // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
    // take 100s of milliseconds.
    if (backgroundVideoLoaderTask != null) {
      // Cancel any task from a previous intent sent to this activity.
      backgroundVideoLoaderTask.cancel(true);
    }
    backgroundVideoLoaderTask = new GoogleVRPlayer.VideoLoaderTask();
    backgroundVideoLoaderTask.execute(Pair.create(fileUri, videoOptions));
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

      if(playVideoStatus != PLAY_VIDEO_STATUS_PLAYING) {
        videoView.pauseVideo();
      }

      Log.i(TAG, "Sucessfully loaded video ");
      loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
    }

    @Override
    public void onDisplayModeChanged(int newDisplayMode) {
      Log.i(TAG, "onDisplayModeChanged " + newDisplayMode);

      if (newDisplayMode != VrWidgetView.DisplayMode.FULLSCREEN_STEREO && newDisplayMode != VrWidgetView.DisplayMode.FULLSCREEN_MONO) {
        finish();
      }
    }

    /**
     * Called by video widget on the UI thread on any asynchronous error.
     */
    @Override
    public void onLoadError(String errorMessage) {
      // An error here is normally due to being unable to decode the video format.
      sendPluginError("CANNOT_DECODE");
      loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
      //Attempt to load fallback video
      if (!fallbackVideoLoaded) {
        fallbackVideoLoaded = true;
        launchVideoLoader(fallbackVideoUri);
      } else {
//                Toast.makeText(
//                        cordova.getActivity(), "Error loading video: " + errorMessage, Toast.LENGTH_LONG)
//                        .show();
      }
      Log.e(TAG, "Error loading video: " + errorMessage);
    }


    /**
     * Update the UI every frame.
     */
    @Override
    public void onNewFrame() {
      sendPluginInformation("DURATION_UPDATE", videoView.getCurrentPosition());
    }

    /**
     * Make the video play in a loop. This method could also be used to move to the next video in
     * a playlist.
     */
    @Override
    public void onCompletion() {
      Log.e(TAG, "Video finished: " + videoView.getCurrentPosition());

      sendPluginInformation("FINISHED_PLAYING", videoView.getDuration());
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
        Log.i(TAG, "onPostExecute");

        videoView.setEventListener(new GoogleVRPlayer.ActivityEventListener());
        videoView.loadVideo(fileUri, videoOptions);

        sendPluginInformation("START_LOADING");
      } catch (IOException e) {
        Log.i(TAG, "onPostExecute CANNOT_DECODE");
        sendPluginError("CANNOT_DECODE");

        // An error here is normally due to being unable to locate the file.
        loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
        // Since this is a background thread, we need to switch to the main thread to show a toast.
        videoView.post(new Runnable() {
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
