/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class MoltresModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("moltres")
    override val head = getPart("head")

    override val portraitScale = 2.8F
    override val portraitTranslation = Vec3d(-0.9, 0.45, 0.0)

    override val profileScale = 0.85F
    override val profileTranslation = Vec3d(0.0, 0.5, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var hover: PokemonPose
    lateinit var fly: PokemonPose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = UI_POSES + PoseType.STAND,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("moltres", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("moltres", "ground_idle")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseTypes = setOf(PoseType.FLOAT, PoseType.HOVER),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("moltres", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("moltres", "air_fly")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("moltres", "faint") else null
}