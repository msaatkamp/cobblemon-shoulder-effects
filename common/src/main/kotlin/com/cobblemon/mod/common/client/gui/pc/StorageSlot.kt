/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec3f

open class StorageSlot(
    x: Int, y: Int,
    private val parent: StorageWidget,
    onPress: PressAction
) : ButtonWidget(x, y, StorageWidget.SLOT_SIZE, StorageWidget.SLOT_SIZE, Text.literal("StorageSlot"), onPress) {

    companion object {
        private val genderIconMale = cobblemonResource("ui/pc/gender_icon_male.png")
        private val genderIconFemale = cobblemonResource("ui/pc/gender_icon_female.png")
        private val selectPointerResource = cobblemonResource("ui/pc/pc_pointer.png")
    }

    override fun playDownSound(soundManager: SoundManager) {
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if (shouldRender()) {
            renderSlot(matrices, x, y)
        }
    }

    fun renderSlot(matrices: MatrixStack, posX: Int, posY: Int) {
        val pokemon = getPokemon() ?: return

        // Render Pokémon
        matrices.push()
        matrices.translate(posX + (StorageWidget.SLOT_SIZE / 2.0), posY + 3.0, 0.0)
        matrices.scale(2.5F, 2.5F, 1F)
        drawProfilePokemon(
            renderablePokemon = pokemon.asRenderablePokemon(),
            matrixStack = matrices,
            rotation = Quaternion.fromEulerXyzDegrees(Vec3f(13F, 35F, 0F)),
            state = null,
            scale = 4.5F
        )
        matrices.pop()

        // Ensure labels are not hidden behind Pokémon render
        matrices.push()
        matrices.translate(0.0, 0.0, 100.0)
        // Level
        drawScaledText(
            matrixStack = matrices,
            text = lang("ui.lv.number", pokemon.level),
            x = posX + 1,
            y = posY + 1,
            shadow = true,
            scale = PCGUI.SCALE
        )

        if (pokemon.gender != Gender.GENDERLESS) {
            blitk(
                matrixStack = matrices,
                texture = if (pokemon.gender == Gender.MALE) genderIconMale else genderIconFemale,
                x = (posX + 21) / PCGUI.SCALE,
                y = (posY + 1) / PCGUI.SCALE,
                width = 6,
                height = 8,
                scale = PCGUI.SCALE
            )
        }
        matrices.pop()

        if (isSelected()) {
            blitk(
                matrixStack = matrices,
                texture = selectPointerResource,
                x = (posX + 10) / PCGUI.SCALE,
                y = ((posY - 3) / PCGUI.SCALE) - parent.pcGui.selectPointerOffsetY,
                width = 11,
                height = 8,
                scale = PCGUI.SCALE
            )
        }
    }

    open fun getPokemon(): Pokemon? {
        return null
    }

    open fun isSelected(): Boolean {
        return false
    }

    open fun shouldRender(): Boolean {
        return true
    }
}