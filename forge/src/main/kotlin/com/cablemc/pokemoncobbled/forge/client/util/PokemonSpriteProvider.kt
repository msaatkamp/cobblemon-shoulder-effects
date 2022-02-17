package com.cablemc.pokemoncobbled.forge.client.util

import com.cablemc.pokemoncobbled.forge.common.pokemon.Pokemon
import com.cablemc.pokemoncobbled.common.util.cobbledResource
import net.minecraft.resources.ResourceLocation

object PokemonSpriteProvider {
    fun getSprite(pokemon: Pokemon): ResourceLocation {
        // Check for shiny
        val shinySprite = cobbledResource("sprites/pokemon/${pokemon.species.name}-shiny.png")
        if (pokemon.shiny && shinySprite.exists()) {
            return shinySprite
        }
        // If nothing else return base species sprite
        return cobbledResource("sprites/pokemon/${pokemon.species.name}-base.png")
    }
}