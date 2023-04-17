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
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class StrengthEffect : ShoulderEffect {

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<StrengthShoulderStatusEffect>().firstOrNull()
        val lastTimeUse = pokemon.saveToNBT(NbtCompound()).getLong("STRENGTH_EFFECT_LAST_USE")
        val twoMinutesInMillis = 2 * 60 * 1000 // 2 minutes in milliseconds
        val timeDiff = System.currentTimeMillis() - lastTimeUse

        if (effect != null) {
            effect.pokemonIds.add(pokemon.uuid)
        }
        if (effect == null){
            if(timeDiff >= twoMinutesInMillis) {
                player.addStatusEffect(StrengthShoulderStatusEffect(mutableListOf(pokemon.uuid), false, "STRENGTH"))
                pokemon.loadFromNBT(setPokemonCooldown(pokemon))
            } else {
                player.addStatusEffect(StrengthShoulderStatusEffect(mutableListOf(pokemon.uuid), true, "STRENGTH"))
            }

        }
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<StrengthShoulderStatusEffect>().firstOrNull()
        effect?.pokemonIds?.remove(pokemon.uuid)
    }
    fun setPokemonCooldown(pokemon: Pokemon): NbtCompound {
        val nbt = pokemon.saveToNBT(NbtCompound())
        nbt.putLong("STRENGTH_EFFECT_LAST_USE", System.currentTimeMillis())

        return nbt
    }

    class StrengthShoulderStatusEffect(pokemonIds: MutableList<UUID>, isInCooldown: Boolean, buffName: String) : ShoulderStatusEffect(pokemonIds, StatusEffects.STRENGTH, 10 * 20, isInCooldown, buffName ) {}

}
