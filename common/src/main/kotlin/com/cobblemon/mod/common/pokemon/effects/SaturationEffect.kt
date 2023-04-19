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

class SaturationEffect : ShoulderEffect {

    private val lastTimeUsed: MutableMap<UUID, Long> = mutableMapOf()
    private val cooldown: Int = 120 // 2 minutes in seconds
    private val buffName: String = "Saturation"
    private val buffDurationSeconds: Int = 10

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<SaturationShoulderStatusEffect>().firstOrNull()
        val lastTimeUse = lastTimeUsed[pokemon.uuid]
        val currentTime = Instant.now().epochSecond

        val timeDiff = if (lastTimeUse != null) currentTime - lastTimeUse else Long.MAX_VALUE

        if (effect != null) {
            effect.pokemonIds.add(pokemon.uuid)
        }
        if (effect == null){
            if(lastTimeUse!! > currentTime) {
                lastTimeUsed[pokemon.uuid] = Instant.now().epochSecond
            }
            if (timeDiff >= cooldown) {
                lastTimeUsed[pokemon.uuid] = Instant.now().epochSecond + buffDurationSeconds

                player.addStatusEffect(
                    SaturationShoulderStatusEffect(
                        mutableListOf(pokemon.uuid),
                        buffName,
                        buffDurationSeconds
                    )
                )

                player.sendMessage(Text.literal("$buffName effect applied from ${pokemon.species.name}."))
            } else {
                player.sendMessage(Text.literal("$buffName effect is still on cooldown for ${cooldown - timeDiff} seconds."))
            }

        }
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<SaturationShoulderStatusEffect>().firstOrNull()
        val lastTimeUse = lastTimeUsed[pokemon.uuid]
        val currentTime = Instant.now().epochSecond
        
        if (effect != null) {
            if(lastTimeUse!! > currentTime) {
                lastTimeUsed[pokemon.uuid] = Instant.now().epochSecond
            }
            effect.pokemonIds.remove(pokemon.uuid)
        }
    }
 
    class SaturationShoulderStatusEffect(pokemonIds: MutableList<UUID>, buffName: String, duration: Int) : ShoulderStatusEffect(pokemonIds, StatusEffects.SATURATION, duration * 20, buffName ) {}

}
