/*
 * Copyright (C) 2022 Pokemon Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.gen1

import com.cablemc.pokemod.common.client.render.models.blockbench.asTransformed
import com.cablemc.pokemod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cablemc.pokemod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cablemc.pokemod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.Y_AXIS
import com.cablemc.pokemod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class BeedrillModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithSpecificChildren("beedrill", listOf("body","antenna_right","antenna_right2","antenna_left","antenna_left2","wing_right","wing_right2","wing_left","wing_left2","abdomen","arm_right","arm_left","leg_right","leg_left","leg_left2","leg_right2","head_AI","head","arm_right2","arm_left2"))
    override val head = getPart("head")
    override val leftWing = getPart("wing_left")
    override val rightWing = getPart("wing_right")

    override val portraitScale = 1.5F
    override val portraitTranslation = Vec3d(0.1, 0.2, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.0, 0.0)

    override fun registerPoses() {
        registerPose(
            poseName = "standing",
            poseTypes = setOf(PoseType.NONE, PoseType.PROFILE, PoseType.PORTRAIT, PoseType.STAND, PoseType.HOVER, PoseType.FLOAT),
            transformTicks = 10,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("0015_beedrill/beedrill", "air_idle")
            ),
            transformedParts = arrayOf(rootPart.asTransformed().addPosition(Y_AXIS, -10F))
        )

        registerPose(
            poseName = "walk",
            poseTypes = setOf(PoseType.WALK, PoseType.FLY, PoseType.SWIM),
            transformTicks = 10,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("0015_beedrill/beedrill", "air_fly")
            ),
            transformedParts = arrayOf(rootPart.asTransformed().addPosition(Y_AXIS, -5F))
        )
    }
}