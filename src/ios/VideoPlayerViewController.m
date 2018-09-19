#import <UIKit/UIKit.h>

#import "VideoPlayerViewController.h"
#import "GoogleVRPlayer.h"
#import "GVRVideoView.h"
#import <objc/runtime.h>

#import <DGActivityIndicatorView.h>

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

    self.displayMode = displayMode;

    CGFloat width =  80;
    CGFloat height = 80;

    UIWindow *window = [[UIApplication sharedApplication] keyWindow];
    UIView *topView = window.rootViewController.view;

    self.monoActivityIndicatorView = [[DGActivityIndicatorView alloc] initWithType:(DGActivityIndicatorAnimationType)[@(DGActivityIndicatorAnimationTypeBallClipRotate) integerValue] tintColor:[UIColor grayColor]];
    self.monoActivityIndicatorView.frame = CGRectMake((topView.frame.size.height / 2) - (width / 2), (topView.frame.size.width / 2) - (height / 2), width, height);
    [topView addSubview:self.monoActivityIndicatorView];

    self.stereoActivityIndicatorView1 = [[DGActivityIndicatorView alloc] initWithType:(DGActivityIndicatorAnimationType)[@(DGActivityIndicatorAnimationTypeBallClipRotate) integerValue] tintColor:[UIColor grayColor]];
    self.stereoActivityIndicatorView1.frame = CGRectMake((topView.frame.size.height / 4) - (width / 2), (topView.frame.size.width / 2) - (height / 2), width, height);
    [topView addSubview:self.stereoActivityIndicatorView1];

    self.stereoActivityIndicatorView2 = [[DGActivityIndicatorView alloc] initWithType:(DGActivityIndicatorAnimationType)[@(DGActivityIndicatorAnimationTypeBallClipRotate) integerValue] tintColor:[UIColor grayColor]];
    self.stereoActivityIndicatorView2.frame = CGRectMake((topView.frame.size.height * 0.75) - (width / 2), (topView.frame.size.width / 2) - (height / 2), width, height);
    [topView addSubview:self.stereoActivityIndicatorView2];

    [self showLoader];

    [self loadVideo];
}

-(BOOL)prefersHomeIndicatorAutoHidden{
    return true;
}

-(void)playVideo {
    [self sendPluginInformation:@"START_PLAYING"];

    [_videoView seekTo: 0];
    [_videoView play];
}

-(void)loadVideo {
    NSString *videoPath  =  self.videoUrl;

    NSURL *videoUri  = [NSURL URLWithString:videoPath];

    int videoType;
    if ([self.videoType isEqualToString:@"MONO"]) {
        videoType = kGVRVideoTypeMono;
    } else if ([self.videoType isEqualToString:@"STEREO"]) {
        videoType = kGVRVideoTypeStereoOverUnder;
    } else if ([self.videoType isEqualToString:@"SPHERICAL2"]) {
        videoType = kGVRVideoTypeSphericalV2;
    } else {
        videoType = kGVRVideoTypeMono;
    }

    [_videoView loadFromUrl:videoUri ofType: videoType];
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

    [self playVideo];
}

- (void)widgetView:(GVRWidgetView *)widgetView
didChangeDisplayMode:(GVRWidgetDisplayMode)displayMode{
    if (displayMode != kGVRWidgetDisplayModeFullscreen && displayMode != kGVRWidgetDisplayModeFullscreenVR){
        // Full screen closed, closing the view
        [_videoView stop];
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
        [_videoView loadFromUrl:videoUrl ofType:kGVRVideoTypeMono];
    }else{
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Failed to load video"
                                                        message:errorMessage
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];

        [self sendPluginError:errorMessage];

        [alert show];
    }
}

- (void) showLoader {
    if ([self.displayMode isEqualToString:@"FullscreenVR"]) {
        [self.stereoActivityIndicatorView1 startAnimating];
        [self.stereoActivityIndicatorView2 startAnimating];
    } else if ([self.displayMode isEqualToString:@"Fullscreen"]) {
        [self.monoActivityIndicatorView startAnimating];
    }
}

- (void) hideLoader {
    if ([self.displayMode isEqualToString:@"FullscreenVR"]) {
        [self.stereoActivityIndicatorView1 stopAnimating];
        [self.stereoActivityIndicatorView2 stopAnimating];
    } else if ([self.displayMode isEqualToString:@"Fullscreen"]) {
        [self.monoActivityIndicatorView stopAnimating];
    }
}

- (void)videoView:(GVRVideoView*)videoView didUpdatePosition:(NSTimeInterval)position {
    [self sendPluginInformation:@"DURATION_UPDATE" andDuration:position];

    if(self.timer != nil) {
        [self.timer invalidate];
    }

    self.timer = nil;

    [self hideLoader];

    self.timer = [NSTimer scheduledTimerWithTimeInterval:0.2
                                             target:self
                                           selector:@selector(showLoader)
                                           userInfo:nil
                                            repeats:NO];

    if (position == videoView.duration) {
        [self sendPluginInformation:@"FINISHED_PLAYING"];
        videoView.displayMode = kGVRWidgetDisplayModeEmbedded;
    }
}

@end
