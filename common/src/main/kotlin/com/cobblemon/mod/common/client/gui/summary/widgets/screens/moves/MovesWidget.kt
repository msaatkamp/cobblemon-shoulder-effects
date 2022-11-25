/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.gui.MultiLineLabelK
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.net.messages.server.RequestMoveSwapPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import java.math.RoundingMode
import java.text.DecimalFormat

class MovesWidget(
    pX: Int, pY: Int,
    val summary: Summary
): SoundlessWidget(pX, pY, WIDTH, HEIGHT, Text.literal("MovesWidget")) {
    companion object {
        private const val WIDTH = 134
        private const val HEIGHT = 148
        const val MOVE_ICON_SIZE = 10
        const val SCALE = 0.5F

        private val decimalFormat = DecimalFormat("#.##").also {
            it.roundingMode = RoundingMode.CEILING
        }

        private val movesBaseResource = cobblemonResource("ui/summary/summary_moves_base.png")
        val movesPowerIconResource = cobblemonResource("ui/summary/summary_moves_icon_power.png")
        val movesAccuracyIconResource = cobblemonResource("ui/summary/summary_moves_icon_accuracy.png")
        val movesEffectIconResource = cobblemonResource("ui/summary/summary_moves_icon_effect.png")
    }

    var selectedMove: Move? = null

    private var index = -1
    private val moves = summary.selectedPokemon.moveSet.getMoves().map { move ->
        index++
        MoveSlotWidget(
            x + 13,
            y + 6 + (MoveSlotWidget.MOVE_HEIGHT + 3) * index,
            move,
            this
        )
    }.toMutableList().onEach {
        addWidget(it)
    }

    override fun render(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        blitk(
            matrixStack = pMatrixStack,
            texture = movesBaseResource,
            x= x,
            y = y,
            width = width,
            height = height
        )

        moves.forEach {
            it.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks)
        }

        // Move icons
        blitk(
            matrixStack = pMatrixStack,
            texture = movesPowerIconResource,
            x= (x + 7) / SCALE,
            y = (y + 114.5) / SCALE,
            width = MOVE_ICON_SIZE,
            height = MOVE_ICON_SIZE,
            scale = SCALE
        )

        blitk(
            matrixStack = pMatrixStack,
            texture = movesAccuracyIconResource,
            x= (x + 7) / SCALE,
            y = (y + 125.5) / SCALE,
            width = MOVE_ICON_SIZE,
            height = MOVE_ICON_SIZE,
            scale = SCALE
        )

        blitk(
            matrixStack = pMatrixStack,
            texture = movesEffectIconResource,
            x= (x + 7) / SCALE,
            y = (y + 136.5) / SCALE,
            width = MOVE_ICON_SIZE,
            height = MOVE_ICON_SIZE,
            scale = SCALE
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = lang("ui.power"),
            x = x + 14,
            y = y + 115,
            scale = SCALE,
            shadow = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = lang("ui.accuracy"),
            x = x + 14,
            y = y + 126,
            scale = SCALE,
            shadow = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = lang("ui.effect"),
            x = x + 14,
            y = y + 137,
            scale = SCALE,
            shadow = true
        )

        val mcFont = MinecraftClient.getInstance().textRenderer
        val movePower = if (selectedMove != null && selectedMove!!.power.toInt() > 0) selectedMove!!.power.toInt().toString().text() else "—".text()
        drawScaledText(
            matrixStack = pMatrixStack,
            text = movePower,
            x = (x + 62.5) - (mcFont.getWidth(movePower) * SCALE),
            y = y + 115,
            scale = SCALE,
            shadow = true
        )

        val moveAccuracy = if (selectedMove != null) format(selectedMove!!.accuracy).text() else "—".text()
        drawScaledText(
            matrixStack = pMatrixStack,
            text = moveAccuracy,
            x = (x + 62.5) - (mcFont.getWidth(moveAccuracy) * SCALE),
            y = y + 126,
            scale = SCALE,
            shadow = true
        )

        val moveEffect = if (selectedMove != null) format(selectedMove!!.effectChance).text() else "—".text()
        drawScaledText(
            matrixStack = pMatrixStack,
            text = moveEffect,
            x = (x + 62.5) - (mcFont.getWidth(moveEffect) * SCALE),
            y = y + 137,
            scale = SCALE,
            shadow = true
        )

        if (selectedMove != null) {
            pMatrixStack.push()
            pMatrixStack.scale(SCALE, SCALE, 1F)
            MultiLineLabelK.create(
                component = selectedMove!!.description,
                width = 57 / SCALE,
                maxLines = 5
            ).renderLeftAligned(
                poseStack = pMatrixStack,
                x = (x + 70) / SCALE,
                y = (y + 115) / SCALE,
                ySpacing = 5.5 / SCALE,
                colour = ColourLibrary.WHITE,
                shadow = true
            )
            pMatrixStack.pop()
        }
    }

    fun reorderMove(move: MoveSlotWidget, up: Boolean) {
        val movePos = moves.indexOf(move)
        if (moves.size <= movePos || movePos == -1) {
            return
        }
        var targetSlot: Int
        if (up) {
            targetSlot = movePos - 1
            if (targetSlot == -1)
                targetSlot = moves.size - 1
        } else {
            targetSlot = movePos + 1
            if (targetSlot >= moves.size)
                targetSlot = 0
        }

        CobblemonNetwork.sendToServer(
            RequestMoveSwapPacket(
                move1 = movePos,
                move2 = targetSlot,
                slot = CobblemonClient.storage.myParty.getPosition(summary.selectedPokemon.uuid)
            )
        )
    }

    public fun format(input: Double): String {
        if (input <= 0) return "—"
        return "${decimalFormat.format(input)}%"
    }

    public fun selectMove(move: Move?) {
        selectedMove = if (selectedMove == move || move == null) null else move
    }
}