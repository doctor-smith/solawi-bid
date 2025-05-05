package org.solyton.solawi.bid.application.data.device

sealed class DeviceType(open val minWidth: Int,open val maxWidth: Int, open val order: Int) {
    data object Empty: DeviceType(0,0, 0)
    data object Mobile : DeviceType(0,600, 1)
    data object Tablet : DeviceType(601,768, 2)
    data object Laptop : DeviceType(769, 1280, 3)
    data object Desktop: DeviceType(1281, 1440, 5)

    data object Huge : DeviceType(1441, Int.MAX_VALUE, Int.MAX_VALUE)
    // ...

    companion object {
        fun from(width: Double, pixelRatio: Double, isTouchDevice: Boolean, userAgent: String): DeviceType = getDeviceType(
            width, pixelRatio, isTouchDevice, userAgent
        )
    }
}

fun getDeviceType(width: Double, pixelRatio: Double, isTouchDevice: Boolean, userAgent: String): DeviceType {
    /*
    val width = window.innerWidth
    val pixelRatio = window.devicePixelRatio
    val isTouchDevice = js("('ontouchstart' in window)") as Boolean
    val userAgent = window.navigator.userAgent.lowercase()
*

     */
    return when {
        // Check for mobile devices (based on touch + pixel ratio + screen width)
        isTouchDevice && pixelRatio > 1 && width <= DeviceType.Mobile.maxWidth -> DeviceType.Mobile

        // Check for tablets (wider screens but still touch-based)
        isTouchDevice && pixelRatio > 1 && width >= DeviceType.Tablet.minWidth && width <= 1024 -> DeviceType.Tablet

        // User Agent check for tablets (iPad, PlayBook, etc.)
        userAgent.contains("ipad") || userAgent.contains("playbook") -> DeviceType.Tablet

        // User Agent check for mobile (iPhone, Android phones)
        userAgent.contains("iphone") || userAgent.contains("android") && !userAgent.contains("tablet") -> DeviceType.Mobile

        // Default: Desktop
        width >= DeviceType.Laptop.minWidth && width < DeviceType.Laptop.maxWidth -> DeviceType.Laptop
        width >= DeviceType.Desktop.minWidth && width < DeviceType.Desktop.maxWidth -> DeviceType.Desktop
        else -> DeviceType.Huge
    }
}

operator fun DeviceType.compareTo(other: DeviceType) = order compareTo other.order