package edu.stanford.spezikt.bluetooth.data.models

data class DeviceUiModel(
    val address: String,
    val measurementsCount: Int,
    val summary: String,
)