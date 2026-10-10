package org.nxy.hasstools.objects

import kotlinx.serialization.Serializable

@Serializable
enum class FusedProvider {
    NONE,
    SYSTEM,
    GOOGLE,
    AMAP
}

/**
 * 速度档位。
 *
 * 每个档位定义「速度上界」和对应的「定位间隔」（单位：秒）。
 * 速度达到该档位时，使用该档位的间隔。
 *
 * @property speedMetersPerSecond 速度上界（米/秒），不包含上界本身
 * @property intervalSeconds 该档位对应的定位间隔（秒）
 */
@Serializable
data class SpeedInterval(
    val speedMetersPerSecond: Float = 0f,
    val intervalSeconds: Int = 0
)

/**
 * 定位间隔配置。
 *
 * 速度档位按 `speedMetersPerSecond` 升序排列，依次匹配；
 * 都不满足时使用 `fallbackIntervalSeconds`。
 *
 * 位置持续未变化时会逐次拉长间隔：前 `ignoreFastStepCount` 次每次递增
 * `ignoreStepSeconds` 秒，之后每次递增 `ignoreSlowStepSeconds` 秒。
 *
 * @property enabled 是否启用自定义间隔（关闭时使用内置默认档位）
 * @property speeds 速度档位列表
 * @property fallbackIntervalSeconds 超出所有档位时的间隔（秒）
 * @property ignoreStepSeconds 位置未变化时每次递增的秒数（前段）
 * @property ignoreFastStepCount 前段递增的次数上限
 * @property ignoreSlowStepSeconds 超出前段后每次递增的秒数
 * @property minimumIntervalSeconds 两次定位之间的最小间隔（秒）
 */
@Serializable
data class IntervalConfig(
    val enabled: Boolean = false,
    val speeds: List<SpeedInterval> = defaultSpeeds(),
    val fallbackIntervalSeconds: Int = 60,
    val ignoreStepSeconds: Int = 600,
    val ignoreFastStepCount: Int = 5,
    val ignoreSlowStepSeconds: Int = 900,
    val minimumIntervalSeconds: Int = 30
) {
    companion object {
        /** 内置默认档位：静止 5min、步行 3min、城市道路 2min、高速 1min */
        fun defaultSpeeds(): List<SpeedInterval> = listOf(
            SpeedInterval(0.5f, 300),
            SpeedInterval(8.33f, 180),
            SpeedInterval(13.89f, 120),
            SpeedInterval(Float.MAX_VALUE, 60)
        )
    }

    /**
     * 计算本次的定位间隔。
     *
     * @param speed 当前速度（米/秒）
     * @param ignoreTimes 位置未变化的累计次数
     * @return 间隔秒数
     */
    fun resolveIntervalSeconds(speed: Float, ignoreTimes: Int): Int {
        if (ignoreTimes > 0) {
            val fastCount = ignoreFastStepCount.coerceAtLeast(0)

            return if (ignoreTimes <= fastCount) {
                ignoreTimes * ignoreStepSeconds
            } else {
                fastCount * ignoreStepSeconds +
                        (ignoreTimes - fastCount) * ignoreSlowStepSeconds
            }
        }

        val matched = speeds
            .filter { speed < it.speedMetersPerSecond }
            .minByOrNull { it.speedMetersPerSecond }

        return matched?.intervalSeconds ?: fallbackIntervalSeconds
    }
}

@Serializable
data class LocationConfig(
    val enabled: Boolean = false,
    val networkTriggerEnabled: Boolean = true,
    val fusedProvider: FusedProvider = FusedProvider.NONE,
    val intervalConfig: IntervalConfig = IntervalConfig()
)
