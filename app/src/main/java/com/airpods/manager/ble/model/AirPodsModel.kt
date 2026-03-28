package com.airpods.manager.ble.model

enum class AirPodsModel(val modelId: Int, val displayName: String, val hasStem: Boolean) {
    AIRPODS_1(0x2002, "AirPods (1st gen)", true),
    AIRPODS_2(0x200F, "AirPods (2nd gen)", true),
    AIRPODS_3(0x2013, "AirPods (3rd gen)", true),
    AIRPODS_4(0x2024, "AirPods (4th gen)", true),
    AIRPODS_4_ANC(0x2029, "AirPods (4th gen) ANC", true),
    AIRPODS_PRO(0x200E, "AirPods Pro", true),
    AIRPODS_PRO_2(0x2014, "AirPods Pro (2nd gen)", true),
    AIRPODS_MAX(0x200A, "AirPods Max", false),
    BEATS_FLEX(0x2010, "Beats Flex", false),
    BEATS_SOLO_PRO(0x2009, "Beats Solo Pro", false),
    BEATS_STUDIO_BUDS(0x2017, "Beats Studio Buds", false),
    POWERBEATS_PRO(0x200B, "Powerbeats Pro", true),
    UNKNOWN(0x0000, "Unknown Device", false);

    val isInEarCapable: Boolean get() = hasStem || this == AIRPODS_MAX

    companion object {
        fun fromModelId(id: Int): AirPodsModel =
            entries.firstOrNull { it.modelId == id } ?: UNKNOWN
    }
}
