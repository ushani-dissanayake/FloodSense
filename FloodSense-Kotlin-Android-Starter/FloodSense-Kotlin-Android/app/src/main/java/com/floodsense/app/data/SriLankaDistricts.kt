package com.floodsense.app.data

data class SriLankaDistrict(
    val name: String,
    val province: String
) {
    val stationIds: List<String>
        get() = FieldLogCatalog.idsForHomeDistrict(name)

    val localGauge: Boolean
        get() = stationIds.isNotEmpty()

    val stationId: String
        get() = stationIds.firstOrNull().orEmpty()
}

/**
 * Home only lists districts that appear in Kalu_Ganga_1000_Records.xlsx:
 * Kalutara (Putupaula, Kalawellawa) and Ratnapura (Ellagawa, Rathnapura, Magura).
 */
object SriLankaDistricts {
    const val DEFAULT = "Kalutara District"
    const val DEFAULT_STATION_ID = "kalu-millaniya"

    val all: List<SriLankaDistrict> = listOf(
        district("Colombo District", "Western"),
        district("Gampaha District", "Western"),
        district("Kalutara District", "Western"),
        district("Galle District", "Southern"),
        district("Matara District", "Southern"),
        district("Hambantota District", "Southern"),
        district("Ratnapura District", "Sabaragamuwa"),
        district("Kegalle District", "Sabaragamuwa"),
        district("Kandy District", "Central"),
        district("Matale District", "Central"),
        district("Nuwara Eliya District", "Central"),
        district("Kurunegala District", "North Western"),
        district("Puttalam District", "North Western"),
        district("Anuradhapura District", "North Central"),
        district("Polonnaruwa District", "North Central"),
        district("Jaffna District", "Northern"),
        district("Kilinochchi District", "Northern"),
        district("Mannar District", "Northern"),
        district("Mullaitivu District", "Northern"),
        district("Vavuniya District", "Northern"),
        district("Trincomalee District", "Eastern"),
        district("Batticaloa District", "Eastern"),
        district("Ampara District", "Eastern"),
        district("Badulla District", "Uva"),
        district("Monaragala District", "Uva")
    )

    private val byKey: Map<String, SriLankaDistrict> =
        all.associateBy { key(it.name) }

    val home: List<SriLankaDistrict>
        get() {
            val names = FieldLogCatalog.homeDistrictNames
            val listed = all.filter { it.name in names }
            return listed.ifEmpty {
                listOf(resolve("Kalutara District"), resolve("Ratnapura District"))
            }
        }

    fun resolveHome(name: String?): SriLankaDistrict {
        val resolved = resolve(name)
        return home.firstOrNull { it.name == resolved.name }
            ?: home.firstOrNull { it.name == DEFAULT }
            ?: resolve(DEFAULT)
    }

    fun resolve(name: String?): SriLankaDistrict {
        val raw = name.orEmpty().trim()
        if (raw.isBlank()) return byKey.getValue(key(DEFAULT))
        byKey[key(raw)]?.let { return it }
        byKey[key("$raw District")]?.let { return it }
        return all.firstOrNull { district ->
            district.name.contains(raw, ignoreCase = true) ||
                raw.contains(district.name.removeSuffix(" District"), ignoreCase = true)
        } ?: byKey.getValue(key(DEFAULT))
    }

    fun defaultStationId(districtName: String?): String =
        resolveHome(districtName).stationId

    private fun district(name: String, province: String) = SriLankaDistrict(name, province)

    private fun key(name: String): String =
        name.trim().lowercase().removeSuffix(" district")
}
