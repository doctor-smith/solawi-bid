package org.evoleq.compose.style.data.device

import kotlin.math.roundToInt

sealed class DeviceType(
    open val minWidth: Int,
    open val maxWidth: Int,
    open val order: Int
) : Comparable<DeviceType> {

    data object Empty : DeviceType(0, 0, 0)
    data object Mobile  : DeviceType(0, 600, 1)
    data object Tablet  : DeviceType(601, 1024, 2)
    data object Laptop  : DeviceType(1025, 1440, 3)
    data object Desktop : DeviceType(1441, 1920, 4)
    data object Huge    : DeviceType(1921, Int.MAX_VALUE, 5)

    override operator fun compareTo(other: DeviceType) =
        order.compareTo(other.order)

    companion object {
        @Suppress("ReturnCount")
        fun detect(
            width: Double,
            userAgent: String,
            hasTouch: Boolean,
            maxTouchPoints: Int = 0
        ): DeviceType {
            val ua = userAgent.lowercase()
            val w = width.roundToInt()

            // ----------------------------
            // 1️⃣ iOS Geräte (zuverlässig im UA)
            // ----------------------------
            if (ua.contains("iphone")) return Mobile
            if (ua.contains("ipad")) return Tablet

            // iPad mit Desktop-User-Agent (iPadOS 13+)
            if (ua.contains("macintosh") && hasTouch && maxTouchPoints > 1) {
                return Tablet
            }

            // ----------------------------
            // 2️⃣ Android Geräte
            // ----------------------------
            if (ua.contains("android")) {
                return if (ua.contains("mobile")) Mobile else Tablet
            }

            // ----------------------------
            // 3️⃣ Touch-Heuristik (Fallback)
            // ----------------------------
            if (hasTouch) {
                if (w <= 600) return Mobile
                if (w <= 1024) return Tablet
            }

            // ----------------------------
            // 4️⃣ Width-Fallback für Desktop/Laptop
            // ----------------------------
            return when {
                w <= 1440 -> Laptop
                w <= 1920 -> Desktop
                else -> Huge
            }
        }
    }
}
