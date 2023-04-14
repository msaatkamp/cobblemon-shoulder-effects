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
import net.minecraft.world.World
import java.util.UUID

class ConduitEffect : ShoulderEffect {
    class ConduitShoulderStatusEffect(val pokemonIds: MutableList<UUID>) :
            StatusEffectInstance(StatusEffects.CONDUIT_POWER, 6000, 0, true, false, false) {

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
            val world: World = entity.world
            duration = if (isShoulderedPokemon(entity.shoulderEntityLeft) || isShoulderedPokemon(entity.shoulderEntityRight)) {
                if (world.isWater(entity.blockPos)) {
                    200
                } else {
                    0
                }
            } else {
                0
            }
            return super.update(entity, overwriteCallback)
        }
    }

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<ConduitShoulderStatusEffect>().firstOrNull()
        if (effect != null) {
            effect.pokemonIds.add(pokemon.uuid)
        } else {
            player.addStatusEffect(ConduitShoulderStatusEffect(mutableListOf(pokemon.uuid)))
        }
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<ConduitShoulderStatusEffect>().firstOrNull()
        effect?.pokemonIds?.remove(pokemon.uuid)
    }
}