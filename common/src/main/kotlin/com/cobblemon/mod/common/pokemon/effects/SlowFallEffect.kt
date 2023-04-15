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
import java.util.UUID
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity

/*
 * Effect that allows for slow falling.
 * The value for [slowAfter] can be set per Form in the Species JSON.
 *
 * @author Qu
 * @since 2022-01-29
 */
class SlowFallEffect : ShoulderEffect {
    /*
     Long term we need to do this differently. There is a restriction in minecraft that for a StatusEffect, i.e. SLOW_FALLING,
     only one status instance can exist. If someone puts pokemon on their shoulder for slow fall and then they take the potion,
     they SHOULD have the effect even after they take their pokemon off the shoulder since the potion would still be in effect,
     but it won't have even been added because of the uniqueness constraint. Unclear how best to solve this.
     */
    class SlowFallShoulderStatusEffect(val pokemonIds: MutableList<UUID>) : StatusEffectInstance(StatusEffects.SLOW_FALLING, 2, 0, true, false, false) {
        fun isShoulderedPokemon(shoulderEntity: NbtCompound): Boolean {
            val pokemonNBT = shoulderEntity.getCompound("Pokemon")
            return pokemonNBT.containsUuid(POKEMON_UUID) && pokemonNBT.getUuid(POKEMON_UUID) in pokemonIds
        }

        override fun writeNbt(nbt: NbtCompound): NbtCompound {
            super.writeNbt(nbt)
            /*
             * StatusEffectInstance isn't really made to be extended. If they have the status when they log out,
             * it will be saved and loaded and any of our custom properties and subclassing doesn't mean shit.
             *
             * The way this effect works is that it's constantly checking if the Pokémon is where it should be. This
             * can only happen if it is this subclass so we have the pokemonId variable. We must not allow Minecraft
             * to load this back in. We can manually handle this shit, so the goal is to stop Minecraft from doing it
             * for us.
             *
             * Putting the ID as some impossible number means that when reading from NBT statically, it doesn't find a
             * match for the effect ID, so it doesn't bother. This is what we want because if the effect needs to
             * continue (like the Pokemon is still on the shoulder) then we handle that in our login handler - no need
             * for Minecraft to get in our way.
             *
             * - Hiro
             */
            nbt.putInt("Id", -999)
            return nbt
        }

        override fun update(entity: LivingEntity, overwriteCallback: Runnable?): Boolean {
            entity as ServerPlayerEntity
            duration = if (isShoulderedPokemon(entity.shoulderEntityLeft) || isShoulderedPokemon(entity.shoulderEntityRight)) {
                10
            } else {
                duration - 10
            }
            return super.update(entity, overwriteCallback)
        }
    }

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<SlowFallShoulderStatusEffect>().firstOrNull()
        if (effect != null) {
            effect.pokemonIds.add(pokemon.uuid)
        } else {
            player.addStatusEffect(SlowFallShoulderStatusEffect(mutableListOf(pokemon.uuid)))
        }
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<SlowFallShoulderStatusEffect>().firstOrNull()
        effect?.pokemonIds?.remove(pokemon.uuid)
    }
}