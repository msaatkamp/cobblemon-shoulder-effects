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
import com.cobblemon.mod.common.util.DataKeys.POKEMON_UUID
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.UUID

class InvisibilityEffect : ShoulderEffect {
    class InvisibilityShoulderStatusEffect(val pokemonIds: MutableList<UUID>) :
        StatusEffectInstance(StatusEffects.INVISIBILITY, 2400, 0, true, false, false) {

        var cooldown = 0

        fun isShoulderedPokemon(shoulderEntity: NbtCompound): Boolean {
            val pokemonNBT = shoulderEntity.getCompound("Pokemon")
            return pokemonNBT.containsUuid(POKEMON_UUID) && pokemonNBT.getUuid(POKEMON_UUID) in pokemonIds
        }

        override fun writeNbt(nbt: NbtCompound): NbtCompound {
            super.writeNbt(nbt)
            nbt.putInt("id", -999)
            return nbt
        }

        override fun update(entity: LivingEntity, overwriteCallback: Runnable?): Boolean {
            entity as ServerPlayerEntity

            duration = if (isShoulderedPokemon(entity.shoulderEntityLeft) || isShoulderedPokemon(entity.shoulderEntityRight)) {
                2400
            } else {
                0
            }

            // Decrease cooldown timer if it's greater than 0
            if (cooldown > 0) {
                cooldown--
            }

            // Warn player when there's 10 seconds left
            if (duration == 10 * 20) { // 10 seconds remaining
                entity.sendMessage(Text.literal("Your invisibility is about to wear off."))
            }

            return super.update(entity, overwriteCallback)
        }
    }

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<InvisibilityShoulderStatusEffect>().firstOrNull()
        if (effect != null && effect.cooldown == 0) {
            effect.pokemonIds.add(pokemon.uuid)
            effect.cooldown = 20 * 60 // 1 minute cooldown
        } else if (effect == null) {
            player.addStatusEffect(InvisibilityShoulderStatusEffect(mutableListOf(pokemon.uuid)))
        }
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<InvisibilityShoulderStatusEffect>().firstOrNull()
        effect?.pokemonIds?.remove(pokemon.uuid)
    }
}
