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

class RegenerationEffect : ShoulderEffect {
    class RegenerationShoulderStatusEffect(internal val pokemonIds: MutableList<UUID>) : StatusEffectInstance(StatusEffects.REGENERATION, 2, 0, true, false, false) {

        companion object {
            const val EFFECT_DURATION_SECONDS = 300 // 15 seconds
            const val COOLDOWN_DURATION_SECONDS = 6000 // 5min
        }

        private var cooldown = 0
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
            cooldown = maxOf(cooldown - 1, 0)

            val world = entity.world

            // Check if the player has lost three or more hunger points
            val healthLost = entity.maxHealth - entity.health
            val hasShoulderedPokemon = isShoulderedPokemon(entity.shoulderEntityLeft) || isShoulderedPokemon(entity.shoulderEntityRight)
            if (!hasShoulderedPokemon) {
                duration = maxOf(duration - EFFECT_DURATION_SECONDS, 0)
            }
            if (duration == 0 && cooldown == 0 && healthLost >= 3) {
                cooldown = COOLDOWN_DURATION_SECONDS
                duration = EFFECT_DURATION_SECONDS
            }
            if (duration == 40) { // 2 seconds remaining
                entity.sendMessage(Text.literal("Your regeneration protection is about to wear off."))
            }
            if (cooldown == 20) { // 1 seconds wait
                entity.sendMessage(Text.literal("Your regeneration protection protection is ready."))
            }
            return super.update(entity, overwriteCallback)
        }
    }

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<RegenerationShoulderStatusEffect>().firstOrNull()
        if (effect != null) {
            effect.pokemonIds.add(pokemon.uuid)
        } else {
            player.addStatusEffect(RegenerationShoulderStatusEffect(mutableListOf(pokemon.uuid)))
        }
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<RegenerationShoulderStatusEffect>().firstOrNull()
        effect?.pokemonIds?.remove(pokemon.uuid)
    }
}
