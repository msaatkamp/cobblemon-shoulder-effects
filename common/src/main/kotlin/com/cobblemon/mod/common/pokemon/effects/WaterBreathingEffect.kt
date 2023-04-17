/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.effects

import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderStatusEffect
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity
import java.util.UUID

class WaterBreathingEffect : ShoulderEffect {

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<WaterBreathingShoulderStatusEffect>().firstOrNull()
        if (effect != null) {
            effect.pokemonIds.add(pokemon.uuid)
        } else {
            player.addStatusEffect(WaterBreathingShoulderStatusEffect(mutableListOf(pokemon.uuid), false, "Aguita Quentita"))
        }
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<WaterBreathingShoulderStatusEffect>().firstOrNull()
        effect?.pokemonIds?.remove(pokemon.uuid)
    }

    class WaterBreathingShoulderStatusEffect(pokemonIds: MutableList<UUID>, isInCooldown: Boolean, buffName: String) : ShoulderStatusEffect(pokemonIds, StatusEffects.HASTE, 300, isInCooldown, buffName) {}
}