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
    [self.vc setValue:googleVRPlayer forKey:@"googleVRPlayer"];

    [self.viewController presentViewController:self.vc animated: NO completion:NULL];
    [self.viewController dismissViewControllerAnimated: NO completion:nil];
}

- (void)playVideo:(CDVInvokedUrlCommand *)command {
    NSString * displayMode = [command.arguments objectAtIndex:0];

    [self.viewController presentViewController:self.vc animated:YES completion:NULL];

    [(VideoPlayerViewController *) self.vc changeDisplayMode: displayMode];

    [(VideoPlayerViewController *) self.vc playVideo];
}



@end
