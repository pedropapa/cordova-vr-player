#import "GoogleVRPlayer.h"
#import "VideoPlayerViewController.h"
#import "GVRVideoView.h"

#import <Cordova/CDVAvailability.h>

@implementation GoogleVRPlayer

- (void)pluginInitialize {
}

- (void)loadVideo:(CDVInvokedUrlCommand *)command {
    NSString * videoUrl = [command.arguments objectAtIndex:0];
    NSString * fallbackVideoUrl = [command.arguments objectAtIndex:1];
    NSString * videoType = [command.arguments objectAtIndex:2];
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

    CATransition *transition = [CATransition animation];
    transition.duration = 0;
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
    transition.type = kCATransitionPush;
    transition.subtype = kCATransitionReveal;
    [self.viewController.view.window.layer addAnimation:transition forKey:nil];

    if (@available(iOS 11.0, *)) {
        [self.viewController prefersHomeIndicatorAutoHidden];
        [self.viewController setNeedsUpdateOfHomeIndicatorAutoHidden];
    }

    [self.viewController presentViewController:self.vc animated:YES completion:NULL];
}

- (void)playVideo:(CDVInvokedUrlCommand *)command {
    NSString * displayMode = [command.arguments objectAtIndex:0];

    [self.viewController presentViewController:self.vc animated:YES completion:NULL];

    [(VideoPlayerViewController *) self.vc changeDisplayMode: displayMode];
    [(VideoPlayerViewController *) self.vc playVideo];
}

@end
