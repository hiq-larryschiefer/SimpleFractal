Simple Android application for generating fractals.  At this time only Mandelbrot fractals are supported.

Use volume up/down to change the generator in use.

Timing data is ELAPSED time, not EXECUTION time.  Logged as well as printed via Toast.

STILL NEEDED:
- zoom in/out 
- configuration settings for #iterations, default zoom
- Native using RenderScript generator implementation

NOTE:
Because of the state of NDK builds and Android Studio (1.4), the build currently uses the deprecated NDK build mode.  Eventually when NDK builds are fully integrated this will be changed.  This means you'll need to edit your auto-generated local.properties file to add a line for the location of the NDK.  It is best to install the "NDK bundle" via the SDK manager and point the project to that.  For example:

`sdk.dir=/Users/larrys/tools/android-sdk
ndk.dir=/Users/larrys/tools/android-sdk/ndk-bundle`

You can find the "NDK Bundle" in the SDK Manager (stand alone) in the "Extras" section near the bottom of the window.  Alternatively, using the Android Studio built-in support for the SDK management, go to Android Studio's Preferences -> Appearance & Behavior -> System Settings -> Android SDK and select the "SDK Tools" tab.  Check the "Android NDK" and apply.

