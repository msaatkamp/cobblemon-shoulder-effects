/*
 * Copyright (C) 2022 Pokemod Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.gen1

import com.cablemc.pokemod.common.client.render.models.blockbench.PoseableEntityState
import com.cablemc.pokemod.common.client.render.models.blockbench.asTransformed
import com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cablemc.pokemod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.Y_AXIS
import com.cablemc.pokemod.common.entity.PoseType.Companion.MOVING_POSES
import com.cablemc.pokemod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cablemc.pokemod.common.entity.PoseType.Companion.UI_POSES
import com.cablemc.pokemod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d
class MankeyModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("mankey")

    override val portraitScale = 1.5F
    override val portraitTranslation = Vec3d(0.1, 0.1, 0.0)

    override val profileScale = 0.8F
    override val profileTranslation = Vec3d(0.0, 0.5, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            transformTicks = 10,
            idleAnimations = arrayOf(
                bedrock("0056_mankey/mankey", "ground_idle")
            ),
            transformedParts = arrayOf(rootPart.asTransformed().addPosition(Y_AXIS, 1.5F))
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            idleAnimations = arrayOf(
                bedrock("0056_mankey/mankey", "ground_walk")
            ),
            transformedParts = arrayOf(rootPart.asTransformed().addPosition(Y_AXIS, 1.5F))
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("0056_mankey/mankey", "faint") else null
}