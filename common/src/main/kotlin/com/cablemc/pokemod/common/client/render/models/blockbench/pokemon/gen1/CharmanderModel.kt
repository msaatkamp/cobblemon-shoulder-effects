/*
 * Copyright (C) 2022 Pokemon Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.gen1

import com.cablemc.pokemod.common.client.render.models.blockbench.PoseableEntityState
import com.cablemc.pokemod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cablemc.pokemod.common.client.render.models.blockbench.frame.BipedFrame
import com.cablemc.pokemod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cablemc.pokemod.common.entity.PoseType
import com.cablemc.pokemod.common.entity.PoseType.Companion.MOVING_POSES
import com.cablemc.pokemod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cablemc.pokemod.common.entity.PoseType.Companion.UI_POSES
import com.cablemc.pokemod.common.entity.pokemon.PokemonBehaviourFlag
import com.cablemc.pokemod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class CharmanderModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("charmander")
    override val head = getPart("head")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")
    override val rightLeg = getPart("leg_right")
    override val leftLeg = getPart("leg_left")

    override val portraitScale = 1.5F
    override val portraitTranslation = Vec3d(0.05, 0.3, 0.0)

    override val profileScale = 0.9F
    override val profileTranslation = Vec3d(0.0, 0.35, 0.0)

    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            condition = { it.getBehaviourFlag(PokemonBehaviourFlag.SLEEPING) },
            transformTicks = 10,
            idleAnimations = arrayOf(bedrock("0004_charmander/charmander", "sleep"))
        )

        val blink = quirk("blink") { bedrockStateful("0004_charmander/charmander", "blink").setPreventsIdle(false) }

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            transformTicks = 5,
            condition = { !it.getBehaviourFlag(PokemonBehaviourFlag.SLEEPING)},
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("0004_charmander/charmander", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 5,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("0004_charmander/charmander", "ground_idle"),
                bedrock("0004_charmander/charmander", "ground_walk")
            )
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isNotPosedIn(sleep)) bedrockStateful("0004_charmander/charmander", "faint") else null
}