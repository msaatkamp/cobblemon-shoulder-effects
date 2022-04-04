package com.cablemc.pokemoncobbled.common.api.spawning.detail

import com.cablemc.pokemoncobbled.common.PokemonCobbled.config
import com.cablemc.pokemoncobbled.common.api.pokemon.PokemonProperties
import com.cablemc.pokemoncobbled.common.api.pokemon.PokemonSpecies
import com.cablemc.pokemoncobbled.common.api.spawning.context.SpawningContext
import com.cablemc.pokemoncobbled.common.api.spawning.spawner.Spawner
import com.cablemc.pokemoncobbled.common.entity.pokemon.PokemonEntity

/**
 * A [SpawnDetail] for spawning a [PokemonEntity].
 *
 * @author Hiroku
 * @since February 13th, 2022
 */
class PokemonSpawnDetail : SpawnDetail() {
    companion object {
        val TYPE = "pokemon"
    }

    override val type: String = TYPE
    var pokemon = PokemonProperties()
    var levelRange: IntRange? = null
    /* todo breadcrumbing */

    override fun autoLabel() {
        super.autoLabel()
        if (pokemon.species != null) {
            val species = PokemonSpecies.getByName(pokemon.species!!.lowercase())
            if (species != null) {
                labels.addAll(
                    species.secondaryType?.let { listOf(species.primaryType.name.lowercase(), it.name.lowercase()) }
                    ?: listOf(species.primaryType.name.lowercase())
                )
            }
        }
    }

    fun getDerivedLevelRange() = levelRange.let { levelRange ->
        if (levelRange == null && pokemon.level == null) {
            IntRange(1, config.maxPokemonLevel)
        } else if (levelRange == null) {
            IntRange(pokemon.level!!, pokemon.level!!)
        } else {
            levelRange
        }
    }

    override fun doSpawn(spawner: Spawner, ctx: SpawningContext): SpawnAction<*> {
        // TODO should do more maybe
        return PokemonSpawnAction(spawner, ctx, this)
    }
}