package com.airpods.manager.domain.model

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    NEARBY    // Detected via BLE but not connected for audio
}
