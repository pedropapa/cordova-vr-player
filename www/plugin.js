var exec = require('cordova/exec');

var PLUGIN_NAME = 'GoogleVRPlayer';

var GoogleVRPlayer = {
  playVideo: function (videoUrl, fallbackVideoUrl, videoType, displayMode, successCallback, errorCallback) {
    exec(successCallback, errorCallback, PLUGIN_NAME, 'playVideo', [videoUrl, fallbackVideoUrl, videoType, displayMode]);
  },
  getDate: function (cb) {
    exec(cb, null, PLUGIN_NAME, 'getDate', []);
  }
};

module.exports = GoogleVRPlayer;
