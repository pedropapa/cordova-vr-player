#import <UIKit/UIKit.h>
#import "GoogleVRPlayer.h"
#import <DGActivityIndicatorView.h>

@interface VideoPlayerViewController : UIViewController {}

@property (nonatomic, retain) NSString* videoUrl;
@property (nonatomic, retain) NSString* fallbackVideoUrl;
@property (nonatomic, retain) NSString* displayMode;
@property (nonatomic, assign) BOOL fallbackVideoPlayed;
@property (nonatomic, strong) NSString* callbackId;
@property (nonatomic, strong) NSString* videoType;
@property (nonatomic, strong) CDVPlugin* googleVRPlayer;
@property (nonatomic, strong) DGActivityIndicatorView* monoActivityIndicatorView;
@property (nonatomic, strong) DGActivityIndicatorView* stereoActivityIndicatorView1;
@property (nonatomic, strong) DGActivityIndicatorView* stereoActivityIndicatorView2;
@property (nonatomic, strong) NSTimer *timer;
-(void)sendPluginInformation:(NSString*) message;
-(void)sendPluginInformation:(NSString*)message andDuration:(NSTimeInterval)duration;
-(void)sendPluginError:(NSString*) message;
-(void)changeDisplayMode:(NSString*) displayMode;
-(void)loadVideo;
-(void)playVideo;
-(void)showLoader;
-(void)hideLoader;

@end
