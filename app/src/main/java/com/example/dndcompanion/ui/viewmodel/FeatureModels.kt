package com.example.dndcompanion.ui.viewmodel

import com.google.gson.annotations.SerializedName

data class Feature(
    val id: String,
    val name: String,
    val nameEn: String,
    val type: String,
    val category: String? = null,
    val levelReq: Int,
    val classReq: List<String> = emptyList(),
    val raceReq: List<String> = emptyList(),
    val description: String
)

data class FeatureDto(
    val id: String,
    val name: String,
    @SerializedName("name_en") val nameEn: String,
    val type: String,
    val category: String? = null,
    @SerializedName("level_req") val levelReq: Int,
    @SerializedName("class_req") val classReq: List<String>? = null,
    @SerializedName("race_req") val raceReq: List<String>? = null,
    val description: String
) {
    fun toFeature(): Feature {
        return Feature(
            id = id,
            name = name,
            nameEn = nameEn,
            type = type,
            category = category,
            levelReq = levelReq,
            classReq = classReq ?: emptyList(),
            raceReq = raceReq ?: emptyList(),
            description = description
        )
    }
}

data class FeatureListDto(
    val features: List<FeatureDto>
)
