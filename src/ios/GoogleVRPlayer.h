#import <Cordova/CDVPlugin.h>
#import "VideoPlayerViewController.h"

@interface GoogleVRPlayer : CDVPlugin {
}

- (void)loadVideo:(CDVInvokedUrlCommand *)command;
- (void)playVideo:(CDVInvokedUrlCommand *)command;

@property (nonatomic, retain) UIViewController* vc;
@property (readwrite, assign) BOOL hasPendingOperation;

@end
