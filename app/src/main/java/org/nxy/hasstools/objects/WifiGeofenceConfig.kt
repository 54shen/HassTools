package org.nxy.hasstools.objects

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class WifiGeofenceConfig(
    val enabled: Boolean = false,
    val items: List<WifiGeofence> = emptyList(),
)

@Serializable
data class GeofenceFunction(
    val fastEnter: Boolean = true,
    val leaveProtection: Boolean = true,
    /**
     * 命中该围栏时使用的上报间隔（秒）。
     *
     * 为 null 表示「未单独设置」，沿用全局的上报间隔。
     * 用可空类型而非默认 1800，因为「不设置」和「设置为某个值」是两种语义。
     */
    val reportIntervalSeconds: Int? = null,
)

@Serializable
data class WifiGeofence(
    val geofenceId: String = "${System.nanoTime()}-${UUID.randomUUID()}",
    val geofenceName: String = "",
    val effectUserIds: Set<String> = emptySet(),
    val zoneId: String = "",
    val enabled: Boolean = true,
    val functions: GeofenceFunction = GeofenceFunction(),
    val ssidList: Set<WifiGeofenceWifiInfo> = emptySet(),
    val bssidList: Set<WifiGeofenceWifiInfo> = emptySet(),
)

@Serializable
data class WifiGeofenceWifiInfo(
    val ssid: String,
    val bssid: String,
)
