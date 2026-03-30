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
    AIRPODS_MAX_USB_C(0x2022, "AirPods Max (USB-C)", false),
    BEATS_FLEX(0x2010, "Beats Flex", false),
    BEATS_SOLO_PRO(0x2009, "Beats Solo Pro", false),
    BEATS_SOLO3(0x2005, "Beats Solo3 Wireless", false),
    BEATS_STUDIO3(0x2006, "Beats Studio3 Wireless", false),
    BEATS_STUDIO_BUDS(0x2017, "Beats Studio Buds", false),
    BEATS_STUDIO_BUDS_PLUS(0x2021, "Beats Studio Buds+", false),
    BEATS_STUDIO_PRO(0x2023, "Beats Studio Pro", false),
    BEATS_FIT_PRO(0x2020, "Beats Fit Pro", true),
    BEATS_X(0x2007, "Beats X", false),
    POWERBEATS3(0x2003, "Powerbeats3 Wireless", true),
    POWERBEATS_4(0x200C, "Powerbeats 4", true),
    POWERBEATS_PRO(0x200B, "Powerbeats Pro", true),
    UNKNOWN(0x0000, "Unknown Device", false);

    val isInEarCapable: Boolean get() = hasStem || this == AIRPODS_MAX || this == AIRPODS_MAX_USB_C

    companion object {
        fun fromModelId(id: Int): AirPodsModel =
            entries.firstOrNull { it != UNKNOWN && it.modelId == id } ?: UNKNOWN
    }
}
