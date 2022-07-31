package com.cablemc.pokemoncobbled.common.util.adapters

import com.cablemc.pokemoncobbled.common.api.spawning.preset.BasicSpawnDetailPreset
import com.cablemc.pokemoncobbled.common.api.spawning.preset.SpawnDetailPreset
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * A simple map adapter for [SpawnDetailPreset] classes that uses the [SpawnDetailPreset.presetTypes] mapping.
 *
 * @author Hiroku
 * @since July 8th, 2022
 */
object SpawnDetailPresetAdapter : JsonDeserializer<SpawnDetailPreset> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): SpawnDetailPreset {
        json as JsonObject
        val type = json.get("type")?.asString ?: BasicSpawnDetailPreset.NAME
        val clazz = SpawnDetailPreset.presetTypes[type] ?: throw IllegalStateException("Unrecognized preset type: $type")
        return ctx.deserialize(json, clazz)
    }
}