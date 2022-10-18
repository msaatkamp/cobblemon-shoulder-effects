/*
 * Copyright (C) 2022 Pokemon Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemod.common.client.render.models.blockbench.frame

import net.minecraft.client.model.ModelPart

interface QuadrupedFrame : ModelFrame {
    val foreLeftLeg: ModelPart
    val foreRightLeg: ModelPart
    val hindLeftLeg: ModelPart
    val hindRightLeg: ModelPart
}