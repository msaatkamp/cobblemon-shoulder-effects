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

class WaterBreathingEffect : ShoulderEffect {
    class WaterBreathShoulderStatusEffect(val pokemonIds: MutableList<UUID>) :
        StatusEffectInstance(StatusEffects.WATER_BREATHING, 2400, 0, true, false, false) {

        var cooldown = 0

        fun isShoulderedPokemon(shoulderEntity: NbtCompound?): Boolean {
            if (shoulderEntity == null) return false
            val pokemonNBT = shoulderEntity.getCompound("Pokemon")
            return pokemonNBT.containsUuid(POKEMON_UUID) && pokemonNBT.getUuid(POKEMON_UUID) in pokemonIds
        }

        override fun writeNbt(nbt: NbtCompound): NbtCompound {
            super.writeNbt(nbt)
            nbt.putInt("id", -999)
            return nbt
        }

        override fun update(entity: LivingEntity, overwriteCallback: Runnable?): Boolean {
            entity as? ServerPlayerEntity ?: return false
            // Remove effect if the pokemon that gave the status is not on the shoulder anymore
            if (!pokemonIds.any { id ->
                    id in listOf(entity.shoulderEntityLeft, entity.shoulderEntityRight)
                        .filterNotNull()
                        .mapNotNull { it.getCompound("Pokemon").getUuid(POKEMON_UUID) }
                }) {
                entity.removeStatusEffect(StatusEffects.WATER_BREATHING)
                return false
            }

            duration = 2400

            // Decrease cooldown timer if it's greater than 0
            if (cooldown > 0) {
                cooldown--
            }

            // Warn player when there's 10 seconds left
            if (duration == 10 * 20) { // 10 seconds remaining
                entity.sendMessage(Text.literal("Your water breath is about to wear off."))
            }

            return super.update(entity, overwriteCallback)
        }
    }

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<WaterBreathShoulderStatusEffect>().firstOrNull()
        if (effect != null && effect.cooldown == 0) {
            effect.pokemonIds.add(pokemon.uuid)
            effect.cooldown = 20 * 300 // 5 minute cooldown
        } else if (effect == null) {
            player.addStatusEffect(WaterBreathShoulderStatusEffect(mutableListOf(pokemon.uuid)))
        }
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        val effect = player.statusEffects.filterIsInstance<WaterBreathShoulderStatusEffect>().firstOrNull()
        effect?.pokemonIds?.remove(pokemon.uuid)
    }
}
