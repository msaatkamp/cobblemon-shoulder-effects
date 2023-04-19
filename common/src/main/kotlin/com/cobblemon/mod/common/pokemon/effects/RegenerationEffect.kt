/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.effects

import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.time.Instant
import java.util.UUID

class RegenerationEffect : ShoulderEffect {

    private val lastTimeUsed: MutableMap<UUID, Long> = mutableMapOf()
    private val buffName: String = "Regeneration"
    private val buffDurationSeconds: Int = 10

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<RegenerationShoulderStatusEffect>().firstOrNull()
        if (effect != null) {
            effect.pokemonIds.add(pokemon.uuid)
        }
        if (effect == null){
            val lastTimeUse = lastTimeUsed[pokemon.uuid]
            val currentTime = Instant.now().epochSecond
            val cdAfterEffect = 2 * 60 + buffDurationSeconds // 2 minutes in seconds
            val timeDiff = if (lastTimeUse != null) currentTime - lastTimeUse else Long.MAX_VALUE

            lastTimeUsed[pokemon.uuid] = Instant.now().epochSecond
            if (timeDiff >= cdAfterEffect) {
            
                player.addStatusEffect(
                    RegenerationShoulderStatusEffect(
                        mutableListOf(pokemon.uuid),
                        buffName,
                        buffDurationSeconds
                    )
                )
                
                player.sendMessage(Text.literal("$buffName effect applied from ${pokemon.species.name}."))   
            } else {
                player.sendMessage(Text.literal("$buffName effect is still on cooldown for ${cdAfterEffect - timeDiff} seconds."))
            }

        }
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<RegenerationShoulderStatusEffect>().firstOrNull()
        effect?.pokemonIds?.remove(pokemon.uuid)
        lastTimeUsed[pokemon.uuid] = Instant.now().epochSecond // Update if remove pokémon from it's shoulder
    }
 
    class RegenerationShoulderStatusEffect(pokemonIds: MutableList<UUID>, buffName: String, duration: Int) : ShoulderStatusEffect(pokemonIds, StatusEffects.REGENERATION, duration * 20, buffName ) {}

}
