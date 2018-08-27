#import <UIKit/UIKit.h>

#import "VideoPlayerViewController.h"
#import "GoogleVRPlayer.h"
#import "GVRVideoView.h"
#import <objc/runtime.h>

@interface VideoPlayerViewController () <GVRVideoViewDelegate>
@property (unsafe_unretained, nonatomic) IBOutlet GVRVideoView *videoView;
@end

@implementation VideoPlayerViewController

- (instancetype)init {
    self = [super initWithNibName:nil bundle:nil];
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];

    NSString *displayMode  =  [self valueForKey:@"displayMode"];
    NSString *callbackId  =  [self valueForKey:@"callbackId"];
    GoogleVRPlayer *googleVRPlayer  =  [self valueForKey:@"googleVRPlayer"];

    _videoView.delegate = self;
    _videoView.hidesTransitionView = YES;
    _videoView.enableFullscreenButton = NO;
    _videoView.enableCardboardButton = NO;
    _videoView.enableInfoButton = NO;
    _videoView.enableTouchTracking = NO;

    self.fallbackVideoPlayed = NO;

    [self sendPluginInformation:@"START_LOADING"];

    if ([displayMode isEqualToString:@"FullscreenVR"]) {
        _videoView.displayMode = kGVRWidgetDisplayModeFullscreenVR;
    } else if ([displayMode isEqualToString:@"Fullscreen"]) {
        _videoView.displayMode = kGVRWidgetDisplayModeFullscreen;
    } else {
        _videoView.displayMode = kGVRWidgetDisplayModeEmbedded;
    }

    [self loadVideo];
}

-(void)changeDisplayMode:(NSString *)displayMode {
    NSLog(@"DISPLAY_MODE 2");
    NSLog(displayMode);

    if ([displayMode isEqualToString:@"FullscreenVR"]) {
        [_videoView setDisplayMode: kGVRWidgetDisplayModeFullscreenVR];
    } else if ([displayMode isEqualToString:@"Fullscreen"]) {
        [_videoView setDisplayMode: kGVRWidgetDisplayModeFullscreen];
    } else {
        [_videoView setDisplayMode: kGVRWidgetDisplayModeEmbedded];
    }
}

-(void)playVideo {
    [self sendPluginInformation:@"START_PLAYING"];

//    [_videoView setDisplayMode: kGVRWidgetDisplayModeFullscreen];

    [_videoView play];
}

-(void)loadVideo {
    NSString *videoPath  =  self.videoUrl;

    NSURL *videoUri  = [NSURL URLWithString:videoPath];

    [_videoView loadFromUrl:videoUri ofType:kGVRVideoTypeMono];
}

-(void)sendPluginInformation:(NSString *)message {
    //    NSLog([NSString stringWithFormat:@"test %@", message]);
    NSMutableArray *data = [NSMutableArray array];
    [data addObject:message];

    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray: data];
    [result setKeepCallbackAsBool:YES];
    [self.googleVRPlayer.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

-(void)sendPluginInformation:(NSString *)message andDuration:(NSTimeInterval)duration {
    //    NSLog([NSString stringWithFormat:@"test %@", message]);
    NSMutableArray *data = [NSMutableArray array];

    NSNumber *myDoubleNumber = [NSNumber numberWithDouble:duration];

    [data addObject:message];
    [data addObject:myDoubleNumber];

    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray: data];
    [result setKeepCallbackAsBool:YES];
    [self.googleVRPlayer.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

-(void)sendPluginError:(NSString *)message {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: message];
    [result setKeepCallbackAsBool:YES];
    [self.googleVRPlayer.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

#pragma mark - GVRVideoViewDelegate

- (void)widgetView:(GVRWidgetView *)widgetView didLoadContent:(id)content {
    NSLog(@"Finished loading video");

    [self sendPluginInformation:@"FINISHED_LOADING"];
}

- (void)widgetView:(GVRWidgetView *)widgetView
didChangeDisplayMode:(GVRWidgetDisplayMode)displayMode{
    if (displayMode != kGVRWidgetDisplayModeFullscreen && displayMode != kGVRWidgetDisplayModeFullscreenVR){
        // Full screen closed, closing the view
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

- (void)widgetView:(GVRWidgetView *)widgetView
didFailToLoadContent:(id)content
  withErrorMessage:(NSString *)errorMessage {

    NSLog(@"Failed to load video: %@", errorMessage);
    NSString *videoPath  =  [self valueForKey:@"fallbackVideoUrl"];

    if (!([videoPath isEqual:[NSNull null]]) && ([errorMessage isEqualToString:@"Cannot Decode"] || self.fallbackVideoPlayed == NO)){
        self.fallbackVideoPlayed = YES;

        [self sendPluginError:@"CANNOT_DECODE"];

        NSURL *videoUrl  = [NSURL URLWithString:videoPath];
        [_videoView loadFromUrl:videoUrl
                         ofType:kGVRVideoTypeMono];
    }else{
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Failed to load video"
                                                        message:errorMessage
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
    }
}

- (void)videoView:(GVRVideoView*)videoView didUpdatePosition:(NSTimeInterval)position {
    [self sendPluginInformation:@"DURATION_UPDATE" andDuration:position];

    if (position == videoView.duration) {
        [self sendPluginInformation:@"FINISHED_PLAYING"];
        videoView.displayMode = kGVRWidgetDisplayModeEmbedded;
    }
}

@end
