# AppRemoteConfig for Android

Configure apps remotely: A simple but effective way to manage apps remotely.

Create a simple configuration file that is easy to maintain and host, yet provides important flexibility to specify settings based on your needs.

General info about AppRemoteConfig can be found [here](https://github.com/egeniq/app-remote-config).

### Build Instructions

WORK IN PROGRESS

You can compile the `AppRemoteConfig` module for Android using [Scade](https://www.scade.io).

    /Applications/Scade.app/Contents/PlugIns/ScadeSDK.plugin/Contents/Resources/Libraries/scd/bin/scd \
        archive \
        --type android-aar \
        --path . \
        --platform android-arm64-v8a \
        --platform android-x86_64 \
        --android-ndk ~/Library/Android/sdk/ndk/26.1.10909125 \
        --generate-android-manifest \
        --android-gradle /Applications/Scade.app/Contents/PlugIns/ScadeSDK.plugin/Contents/Resources/Libraries/ScadeSDK/thirdparty/gradle
