package org.nxy.hasstools.objects

import kotlinx.serialization.Serializable

@Serializable
enum class FusedProvider {
    NONE,
    SYSTEM,
    GOOGLE,
    AMAP
}

/** 两次上报之间的硬性最小间隔（秒），防止误填 0 导致疯狂上报。 */
const val MINIMUM_INTERVAL_SECONDS = 10

/**
 * 定位上报配置。
 *
 * 逻辑只有两条：
 * 1. 命中地理围栏（连着某个已配置的家庭/公司 Wi-Fi）时，使用该围栏自己的间隔；
 * 2. 未命中任何围栏时，使用 [reportIntervalSeconds] 这个全局间隔。
 *
 * 不再有任何速度分档、不再有「位置未变化递增」逻辑。
 *
 * @property enabled 是否启用定位上报
 * @property networkTriggerEnabled 网络切换时是否插队上报一次
 * @property fusedProvider 融合定位提供方
 * @property reportIntervalSeconds 在外（未命中围栏）时的上报间隔（秒）
 * @property geofenceRelaxEnabled 是否启用「地理围栏免打扰」总开关；
 *           关闭后即使命中围栏也使用全局间隔
 */
@Serializable
data class LocationConfig(
    val enabled: Boolean = false,
    val networkTriggerEnabled: Boolean = true,
    val fusedProvider: FusedProvider = FusedProvider.NONE,
    val reportIntervalSeconds: Int = 60,
    val geofenceRelaxEnabled: Boolean = true
)

/**
 * 解析本次实际使用的上报间隔。
 *
 * @param geofenceIntervalSeconds 当前命中围栏的间隔；未命中传 null
 * @param globalIntervalSeconds 全局（在外）间隔
 * @param geofenceRelaxEnabled 围栏免打扰总开关
 * @return 最终生效的间隔秒数，不会低于 [MINIMUM_INTERVAL_SECONDS]
 */
fun resolveIntervalSeconds(
    geofenceIntervalSeconds: Int?,
    globalIntervalSeconds: Int,
    geofenceRelaxEnabled: Boolean,
): Int {
    val seconds = if (geofenceRelaxEnabled && geofenceIntervalSeconds != null) {
        geofenceIntervalSeconds
    } else {
        globalIntervalSeconds
    }
    return seconds.coerceAtLeast(MINIMUM_INTERVAL_SECONDS)
}
