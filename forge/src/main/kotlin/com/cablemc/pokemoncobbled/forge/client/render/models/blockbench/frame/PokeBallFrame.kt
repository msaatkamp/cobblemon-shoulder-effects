package com.cablemc.pokemoncobbled.forge.client.render.models.blockbench.frame

import net.minecraft.client.model.geom.ModelPart

interface PokeBallFrame : ModelFrame {
    val subRoot: ModelPart
    val lid: ModelPart
}