
var exec = require('cordova/exec');

var PLUGIN_NAME = 'GoogleVRPlayer';

var GoogleVRPlayer = {
  playVideo: function(videoUrl, fallbackVideoUrl, displayMode, cb) {
    exec(cb, null, PLUGIN_NAME, 'playVideo', [videoUrl, fallbackVideoUrl, displayMode]);
  },
  getDate: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'getDate', []);
  }
};

module.exports = GoogleVRPlayer;
