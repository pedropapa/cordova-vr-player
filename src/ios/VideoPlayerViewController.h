#import <UIKit/UIKit.h>
#import "GoogleVRPlayer.h"

@interface VideoPlayerViewController : UIViewController {}

@property (nonatomic, retain) NSString* videoUrl;
@property (nonatomic, retain) NSString* fallbackVideoUrl;
@property (nonatomic, retain) NSString* displayMode;
@property (nonatomic, assign) BOOL fallbackVideoPlayed;
@property (nonatomic, strong) NSString* callbackId;
@property (nonatomic, strong) CDVPlugin* googleVRPlayer;
-(void)sendPluginInformation:(NSString*) message;
-(void)sendPluginInformation:(NSString*)message andDuration:(NSTimeInterval)duration;
-(void)sendPluginError:(NSString*) message;
-(void)changeDisplayMode:(NSString*) displayMode;
-(void)loadVideo;
-(void)playVideo;

@end
