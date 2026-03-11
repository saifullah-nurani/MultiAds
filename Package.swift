// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "MultiAds",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(
            name: "MultiAdsCore",
            targets: ["coreKit"]
        ),
        .library(
            name: "MultiAdsAdmob",
            targets: ["admobKit"]
        )
    ],
    targets: [
        // Note: For a pure KMP project without checking in XCFrameworks, 
        // this is usually handled by pointing to the build artifacts or using a 
        // binaryTarget pointing to a remote URL where XCFrameworks are hosted.
        .binaryTarget(
            name: "coreKit",
            path: "./core/build/XCFrameworks/release/coreKit.xcframework"
        ),
        .binaryTarget(
            name: "admobKit",
            path: "./admob/build/XCFrameworks/release/admobKit.xcframework"
        )
    ]
)
