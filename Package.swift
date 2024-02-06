// swift-tools-version: 5.9

import Foundation
import PackageDescription

let package = Package(
    name: "AppRemoteConfig",
    defaultLocalization: "en",
    products: [
        .library(
            name: "AppRemoteConfigAndroid",
            type: .dynamic,
            targets: ["AppRemoteConfigAndroid"]
        )
    ],
    dependencies: [
      //  .package(path: "../app-remote-config"),
        .package(url: "https://github.com/egeniq/app-remote-config.git", branch: "develop"),
        .package(url: "https://github.com/scade-platform/swift-java.git", branch: "main")
    ],
    targets: [
        .target(
            name: "AppRemoteConfigAndroid",
            dependencies: [
                "AppRemoteConfig",
                .product(name: "Java", package: "swift-java")
            ]),
        .testTarget(
            name: "AppRemoteConfigAndroidTests",
            dependencies: ["AppRemoteConfigAndroid"]
        )
    ]
)
