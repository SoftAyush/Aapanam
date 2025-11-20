package org.example.aapanam.util

import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityFlags
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.darwin.dispatch_get_main_queue

class IOSConnectivityManager : ConnectivityManager {
    override fun isNetworkAvailable(): Boolean {
        val reachability = SCNetworkReachabilityCreateWithName(null, "www.google.com")
        val flags = SCNetworkReachabilityFlags()
        SCNetworkReachabilityGetFlags(reachability, flags)

        return (flags.toInt() and kSCNetworkFlagsReachable) != 0
    }
}
