
var exec = require('cordova/exec');

var PLUGIN_NAME = 'GoogleVRPlayer';

var GoogleVRPlayer = {
  loadVideo: function(videoUrl, fallbackVideoUrl, videoType, successCallback, errorCallback) {
    exec(successCallback, errorCallback, PLUGIN_NAME, 'loadVideo', [videoUrl, fallbackVideoUrl, videoType]);
  },
  playVideo: function(displayMode) {
    exec(null, null, PLUGIN_NAME, 'playVideo', [displayMode]);
  },
  getDate: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'getDate', []);
  }
};

module.exports = GoogleVRPlayer;
