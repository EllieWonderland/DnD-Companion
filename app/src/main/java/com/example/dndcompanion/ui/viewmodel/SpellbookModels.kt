package com.example.dndcompanion.ui.viewmodel

import com.google.gson.annotations.SerializedName

data class SpellDto(
    @SerializedName("spell_id") val spellId: String,
    @SerializedName("name_de") val nameDe: String,
    @SerializedName("name_en") val nameEn: String,
    val level: Int,
    val school: String,
    @SerializedName("casting_time") val castingTime: String,
    val range: SpellRangeDto?,
    val components: SpellComponentsDto?,
    val duration: SpellDurationDto?,
    @SerializedName("area_of_effect") val areaOfEffect: SpellAoeDto?,
    @SerializedName("saving_throw") val savingThrow: String?,
    val damage: SpellDamageDto?,
    val classes: List<String>?,
    val description: String?
) {
    fun toSpell(): Spell {
        val rangeStr = if (range != null) {
            if (range.distance > 0) {
                val m = kotlin.math.round(range.distance * 0.3 * 2.0) / 2.0
                val mStr = if (m % 1.0 == 0.0) m.toInt().toString() else m.toString()
                val f = (range.distance / 5.0).toInt()
                "${range.type} $mStr m / $f Felder"
            } else {
                range.type
            }
        } else "Berührung"
        val durationStr = if (duration != null) "${duration.type}${if (duration.concentration) " (Konzentration)" else ""}" else "Spontan"
        
        val descBuilder = StringBuilder()
        if (!description.isNullOrBlank()) {
            descBuilder.append(description).append("\n\n")
        }

        descBuilder.append("Schule: $school\n")
        if (savingThrow != null) descBuilder.append("Rettungswurf: $savingThrow\n")
        if (damage != null) {
            descBuilder.append("Art: ${damage.type}")
            if (damage.baseDice != null) descBuilder.append(" (${damage.baseDice})")
            if (damage.upcastDicePerLevel != null) descBuilder.append(" [Upcast: +${damage.upcastDicePerLevel} pro Stufe]")
            descBuilder.append("\n")
        }
        if (areaOfEffect != null) {
            val sizeM = kotlin.math.round(areaOfEffect.size * 0.3 * 2.0) / 2.0
            val sizeMStr = if (sizeM % 1.0 == 0.0) sizeM.toInt().toString() else sizeM.toString()
            val sizeF = areaOfEffect.size / 5
            descBuilder.append("Wirkungsbereich: ${areaOfEffect.type} ($sizeMStr m / $sizeF Felder)\n")
        }
        val actualClasses = classes ?: emptyList()
        val classesStr = actualClasses.joinToString(", ")
        descBuilder.append("Klassen: $classesStr\n")

        return Spell(
            id = spellId,
            name = nameDe,
            level = level,
            castingTime = castingTime,
            range = rangeStr.trim(),
            duration = durationStr,
            componentsV = components?.v == true,
            componentsS = components?.s == true,
            componentsM = components?.m?.isNotBlank() == true,
            materialCost = components?.m ?: "",
            description = descBuilder.toString().trim(),
            classes = actualClasses,
            school = school.trim(),
            isPrepared = false,
            isRitual = castingTime.contains("Ritual", ignoreCase = true) || school.contains("Ritual", ignoreCase = true)
        )
    }
}

data class SpellRangeDto(
    val type: String,
    val distance: Double
)

data class SpellComponentsDto(
    val v: Boolean,
    val s: Boolean,
    val m: String?,
    @SerializedName("m_cost") val mCost: Int?,
    @SerializedName("m_consumed") val mConsumed: Boolean?
)

data class SpellDurationDto(
    val type: String,
    val concentration: Boolean
)

data class SpellAoeDto(
    val type: String,
    val size: Int
)

data class SpellDamageDto(
    val type: String,
    @SerializedName("base_dice") val baseDice: String?,
    @SerializedName("upcast_dice_per_level") val upcastDicePerLevel: String?
)
