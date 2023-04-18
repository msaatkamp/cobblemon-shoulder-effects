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

class SpeedEffect : ShoulderEffect {

    private val lastTimeUsed: MutableMap<UUID, Long> = mutableMapOf()
    private val buffName: String = "Speed"
    private val buffDurationSeconds: Int = 300

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<SpeedShoulderStatusEffect>().firstOrNull()
        if (effect != null) {
            effect.pokemonIds.add(pokemon.uuid)
        }
        if (effect == null){
            val lastTimeUse = lastTimeUsed[pokemon.uuid]
            val currentTime = Instant.now().epochSecond
            val twoMinutesInSeconds = 2 * 60 // 2 minutes in seconds
            val timeDiff = if (lastTimeUse != null) currentTime - lastTimeUse else Long.MAX_VALUE

            if (timeDiff >= twoMinutesInSeconds) {
                player.addStatusEffect(
                    SpeedEffect.SpeedShoulderStatusEffect(
                        mutableListOf(pokemon.uuid),
                        buffName,
                        buffDurationSeconds
                    )
                )
                lastTimeUsed[pokemon.uuid] = currentTime
                player.sendMessage(Text.literal("$buffName effect applied from ${pokemon.displayName} for $buffDurationSeconds seconds."))
            } else {
                player.sendMessage(Text.literal("$buffName effect is still on cooldown for ${twoMinutesInSeconds - timeDiff} seconds."))
            }

        }
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<SpeedShoulderStatusEffect>().firstOrNull()
        effect?.pokemonIds?.remove(pokemon.uuid)
    }

    class SpeedShoulderStatusEffect(pokemonIds: MutableList<UUID>, buffName: String, duration: Int) : ShoulderStatusEffect(pokemonIds, StatusEffects.SPEED, duration * 20, buffName ) {}

}
