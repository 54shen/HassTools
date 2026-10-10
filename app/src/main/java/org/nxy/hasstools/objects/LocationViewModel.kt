package org.nxy.hasstools.objects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.nxy.hasstools.data.LocationDataStore

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore: LocationDataStore = LocationDataStore()
    private val _locationConfig: MutableStateFlow<LocationConfig> = MutableStateFlow(LocationConfig())
    val locationConfig: StateFlow<LocationConfig> = _locationConfig

    init {
        _locationConfig.value = dataStore.readData()
        viewModelScope.launch {
            dataStore.dataFlow.collectLatest { config ->
                _locationConfig.value = config
            }
        }
    }

    // 更新配置
    private fun updateLocationConfig(newConfig: LocationConfig) {
        _locationConfig.value = newConfig
        viewModelScope.launch {
            dataStore.saveData(newConfig)
        }
    }

    // 是否启用定位服务
    fun setEnabled(enabled: Boolean) {
        val updatedConfig = _locationConfig.value.copy(enabled = enabled)
        updateLocationConfig(updatedConfig)
    }

    // 是否启用网络触发
    fun setNetworkTriggerEnabled(enabled: Boolean) {
        val updatedConfig = _locationConfig.value.copy(networkTriggerEnabled = enabled)
        updateLocationConfig(updatedConfig)
    }

    // 设置融合定位提供者
    fun setFusedProvider(provider: FusedProvider) {
        val updatedConfig = _locationConfig.value.copy(fusedProvider = provider)
        updateLocationConfig(updatedConfig)
    }

    // 更新整个定位间隔配置
    fun setIntervalConfig(intervalConfig: IntervalConfig) {
        val updatedConfig = _locationConfig.value.copy(intervalConfig = intervalConfig)
        updateLocationConfig(updatedConfig)
    }

    // 是否启用自定义定位间隔
    fun setIntervalEnabled(enabled: Boolean) {
        val updatedConfig = _locationConfig.value.copy(
            intervalConfig = _locationConfig.value.intervalConfig.copy(enabled = enabled)
        )
        updateLocationConfig(updatedConfig)
    }

    // 更新单个速度档位的间隔（秒）
    fun setSpeedIntervalSeconds(index: Int, seconds: Int) {
        val intervalConfig = _locationConfig.value.intervalConfig

        val updatedSpeeds = intervalConfig.speeds.mapIndexed { i, item ->
            if (i == index) item.copy(intervalSeconds = seconds) else item
        }

        updateLocationConfig(
            _locationConfig.value.copy(intervalConfig = intervalConfig.copy(speeds = updatedSpeeds))
        )
    }

    // 更新位置未变化时的递增规则（秒）
    fun setIgnoreInterval(stepSeconds: Int, slowStepSeconds: Int) {
        val updatedConfig = _locationConfig.value.copy(
            intervalConfig = _locationConfig.value.intervalConfig.copy(
                ignoreStepSeconds = stepSeconds,
                ignoreSlowStepSeconds = slowStepSeconds
            )
        )
        updateLocationConfig(updatedConfig)
    }

    // 更新前段递增的次数上限
    fun setIgnoreFastStepCount(count: Int) {
        val updatedConfig = _locationConfig.value.copy(
            intervalConfig = _locationConfig.value.intervalConfig.copy(ignoreFastStepCount = count)
        )
        updateLocationConfig(updatedConfig)
    }

    // 更新最小间隔（秒）
    fun setMinimumIntervalSeconds(seconds: Int) {
        val updatedConfig = _locationConfig.value.copy(
            intervalConfig = _locationConfig.value.intervalConfig.copy(minimumIntervalSeconds = seconds)
        )
        updateLocationConfig(updatedConfig)
    }
}
