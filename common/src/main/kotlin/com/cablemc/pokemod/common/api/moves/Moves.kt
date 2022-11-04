/*
 * Copyright (C) 2022 Pokemod Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemod.common.api.moves

import com.cablemc.pokemod.common.Pokemod
import com.cablemc.pokemod.common.api.data.JsonDataRegistry
import com.cablemc.pokemod.common.api.moves.adapters.DamageCategoryAdapter
import com.cablemc.pokemod.common.api.moves.categories.DamageCategory
import com.cablemc.pokemod.common.api.reactive.SimpleObservable
import com.cablemc.pokemod.common.api.types.ElementalType
import com.cablemc.pokemod.common.api.types.adapters.ElementalTypeAdapter
import com.cablemc.pokemod.common.net.messages.client.data.MovesRegistrySyncPacket
import com.cablemc.pokemod.common.util.pokemodResource
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlin.io.path.Path
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

/**
 * Registry for all known Moves
 */
object Moves : JsonDataRegistry<MoveTemplate> {
    override val gson = GsonBuilder()
        .registerTypeAdapter(DamageCategory::class.java, DamageCategoryAdapter)
        .registerTypeAdapter(ElementalType::class.java, ElementalTypeAdapter)
        .setLenient()
        .disableHtmlEscaping()
        .create()

    override val id = pokemodResource("moves")
    override val type = ResourceType.SERVER_DATA
    override val typeToken: TypeToken<MoveTemplate> = TypeToken.get(MoveTemplate::class.java)
    override val resourcePath = Path("moves")
    override val observable = SimpleObservable<Moves>()

    private val allMoves = mutableMapOf<String, MoveTemplate>()
    private val idMapping = mutableMapOf<Int, MoveTemplate>()
    override fun reload(data: Map<Identifier, MoveTemplate>) {
        this.reload(data, true)
    }

    internal fun reload(data: Map<Identifier, MoveTemplate>, applyId: Boolean) {
        this.allMoves.clear()
        data.forEach { (identifier, moveTemplate) ->
            this.allMoves[identifier.path] = moveTemplate
            if (!applyId) {
                this.idMapping[moveTemplate.id] = moveTemplate
            }
        }
        if (applyId) {
            applyIDs()
        }
        Pokemod.LOGGER.info("Loaded {} moves", this.allMoves.size)
        this.observable.emit(this)
    }

    private fun applyIDs() {
        var id = 0
        this.allMoves.values
            .sortedBy { it.name }
            .forEach {
                it.id = id++
                idMapping[it.id] = it
            }
    }

    override fun sync(player: ServerPlayerEntity) {
        MovesRegistrySyncPacket().sendToPlayer(player)
    }

    fun getByName(name: String) = allMoves[name.lowercase()]
    fun getByNumericalId(id: Int) = idMapping[id]!!
    fun getByNameOrDummy(name: String) = allMoves[name.lowercase()] ?: MoveTemplate.dummy(name.lowercase())
    fun getExceptional() = getByName("tackle") ?: allMoves.values.random()
    fun count() = allMoves.size
    fun names(): Collection<String> = this.allMoves.keys.toSet()
    fun all() = this.allMoves.values.toList()
}