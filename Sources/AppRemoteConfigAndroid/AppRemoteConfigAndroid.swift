import Foundation
import Dispatch
import Java
import AppRemoteConfig

// THIS IS STIL WORK IN PROGRESS

// Downloads data from specified URL. Executes callback in main activity after download is finised.
@MainActor
public func downloadData(activity: JObject, url: String) async {
    do {
        let r = try Config(json: ["settings": ["bar": 43]])
        let platformVersion = OperatingSystemVersion(majorVersion: 1, minorVersion: 0, patchVersion: 0)
        let res = r.resolve(date: Date(), platform: .android, platformVersion: platformVersion, appVersion: try! Version("1.0.0"), buildVariant: .release)
        activity.call(method: "onDataLoaded", "\(res)")
    }
    catch {
        var userInfoStr = ""
        if let nsError = error as? NSError {
            userInfoStr = "\(nsError.userInfo)"
        }
        activity.call(method: "onDataLoaded", "ERROR loading from URL '\(url)': \(error) \(userInfoStr)")
    }
}

// NOTE: Use @_silgen_name attribute to set native name for a function called from Java
@_silgen_name("Java_com_example_swiftandroidexample_MainActivity_loadData")
public func MainActivity_loadData(env: UnsafeMutablePointer<JNIEnv>, activity: JavaObject, javaUrl: JavaString) {
    // Create JObject wrapper for activity object
    let mainActivity = JObject(activity)

    // Convert the Java string to a Swift string
    let str = String.fromJavaObject(javaUrl)

    // Start the data download asynchronously in the main actor
    Task {
        await downloadData(activity: mainActivity, url: str)
    }
}
