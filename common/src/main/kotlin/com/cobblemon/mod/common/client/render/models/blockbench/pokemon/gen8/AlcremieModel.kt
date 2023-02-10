/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.asTransformed
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class AlcremieModel (root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("alcremie")
    override val head = getPart("head")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")

    override val portraitScale = 2.0F
    override val portraitTranslation = Vec3d(-0.1, -0.65, 0.0)

    override val profileScale = 0.8F
    override val profileTranslation = Vec3d(0.0, 0.5, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var shoulderLeft: PokemonPose
    lateinit var shoulderRight: PokemonPose

    val shoulderOffset = 5

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = UI_POSES + PoseType.STATIONARY_POSES + PoseType.MOVING_POSES,
            transformTicks = 10,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("alcremie", "ground_idle")
            )
        )

        shoulderLeft = registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("alcremie", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.asTransformed().addPosition(TransformedModelPart.X_AXIS, shoulderOffset)
            )
        )

        shoulderRight = registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("alcremie", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.asTransformed().addPosition(TransformedModelPart.X_AXIS, -shoulderOffset)
            )
        )
    }

}