/**
 */
package com.neotrino;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GoogleVRPlayer extends CordovaPlugin {
  private static final String TAG = "GoogleVRPlayer";
  static CallbackContext callbackContext;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing GoogleVRPlayer");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
      GoogleVRPlayer.callbackContext = callbackContext;

    if(action.equals("playVideo")) {
      String videoUrl = args.getString(0);
      String fallbackVideoUrl = args.getString(1);
      String videoType = args.getString(2);
      String displayMode = args.getString(3);
      Log.d(TAG, videoUrl);
      Context context=this.cordova.getActivity().getApplicationContext();
      Intent intent=new Intent(context, VrVideoActivity.class);
      intent.putExtra("videoUrl", videoUrl);
      intent.putExtra("fallbackVideoUrl", fallbackVideoUrl);
      intent.putExtra("videoType", videoType);
      intent.putExtra("displayMode", displayMode);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      context.startActivity(intent);
    }
    return true;
  }

}