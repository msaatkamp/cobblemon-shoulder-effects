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
import net.minecraft.text.Text
import java.util.*

class HasteEffect : ShoulderEffect {

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<HasteShoulderStatusEffect>().firstOrNull()
        val lastTimeUse = pokemon.saveToNBT(NbtCompound()).getLong("HASTE_EFFECT_LAST_USE")
        val twoMinutesInMillis = 2 * 60 * 1000 // 2 minutes in milliseconds
        val timeDiff = System.currentTimeMillis() - lastTimeUse

        if (effect != null) {
            effect.pokemonIds.add(pokemon.uuid)
        }
        if (effect == null){
            if(timeDiff >= twoMinutesInMillis) {
                player.addStatusEffect(HasteShoulderStatusEffect(mutableListOf(pokemon.uuid), false, "HASTE"))
                player.sendMessage(Text.literal("Set two minutes cooldown"))

                setPokemonCooldown(pokemon)
            } else {
                player.addStatusEffect(HasteShoulderStatusEffect(mutableListOf(pokemon.uuid), true, "HASTE"))
                player.sendMessage(Text.literal("HASTE_EFFECT_LAST_USE IsInCooldown time diff: ${timeDiff * 1000}"))

            }

        }
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<HasteShoulderStatusEffect>().firstOrNull()
        effect?.pokemonIds?.remove(pokemon.uuid)
    }
    fun setPokemonCooldown(pokemon: Pokemon) {
        val nbt = pokemon.saveToNBT(NbtCompound())
        nbt.putLong("HASTE_EFFECT_LAST_USE", System.currentTimeMillis())

        pokemon.loadFromNBT(nbt)
    }

    class HasteShoulderStatusEffect(pokemonIds: MutableList<UUID>, isInCooldown: Boolean, buffName: String) : ShoulderStatusEffect(pokemonIds, StatusEffects.HASTE, 10 * 20, isInCooldown, buffName ) {}

}
