#import "GoogleVRPlayer.h"
#import "VideoPlayerViewController.h"
#import "GVRVideoView.h"

#import <Cordova/CDVAvailability.h>

@implementation GoogleVRPlayer

- (void)pluginInitialize {
}

- (void)playVideo:(CDVInvokedUrlCommand *)command {
    NSString * videoUrl = [command.arguments objectAtIndex:0];
    NSString * fallbackVideoUrl = [command.arguments objectAtIndex:1];
    NSString * videoType = [command.arguments objectAtIndex:2];
    NSString * displayMode = [command.arguments objectAtIndex:3];
    NSString * callbackId = command.callbackId;
    GoogleVRPlayer * googleVRPlayer = self;

    // Set the hasPendingOperation field to prevent the webview from crashing
    self.hasPendingOperation = YES;

    // Launch the storyboard
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    self.vc = [sb instantiateViewControllerWithIdentifier:@"videoBoardId"];

    [self.vc setValue:videoUrl forKey:@"videoUrl"];
    [self.vc setValue:fallbackVideoUrl forKey:@"fallbackVideoUrl"];
    [self.vc setValue:callbackId forKey:@"callbackId"];
    [self.vc setValue:videoType forKey:@"videoType"];
    [self.vc setValue:googleVRPlayer forKey:@"googleVRPlayer"];
    [self.vc setValue:displayMode forKey:@"displayMode"];

    NSLog(@"PLUGIN >>> videoUrl %@", videoUrl);
    NSLog(@"PLUGIN >>> fallbackVideoUrl %@", fallbackVideoUrl);
    NSLog(@"PLUGIN >>> callbackId %@", callbackId);
    NSLog(@"PLUGIN >>> videoType %@", videoType);
    NSLog(@"PLUGIN >>> googleVRPlayer %@", googleVRPlayer);

    if (@available(iOS 11.0, *)) {
        [self.viewController prefersHomeIndicatorAutoHidden];
        [self.viewController setNeedsUpdateOfHomeIndicatorAutoHidden];
    }

    [self.viewController presentViewController:self.vc animated: NO completion:NULL];
}

@end
