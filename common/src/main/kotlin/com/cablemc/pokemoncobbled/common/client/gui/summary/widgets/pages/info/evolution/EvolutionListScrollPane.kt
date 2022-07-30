package com.cablemc.pokemoncobbled.common.client.gui.summary.widgets.pages.info.evolution

import com.cablemc.pokemoncobbled.common.api.gui.ColourLibrary
import com.cablemc.pokemoncobbled.common.api.gui.blitk
import com.cablemc.pokemoncobbled.common.api.gui.drawText
import com.cablemc.pokemoncobbled.common.api.pokemon.evolution.EvolutionDisplay
import com.cablemc.pokemoncobbled.common.client.CobbledResources
import com.cablemc.pokemoncobbled.common.client.gui.summary.SummaryButton
import com.cablemc.pokemoncobbled.common.client.gui.summary.widgets.ModelWidget
import com.cablemc.pokemoncobbled.common.client.gui.summary.widgets.common.ModelSectionScrollPane
import com.cablemc.pokemoncobbled.common.client.gui.summary.widgets.type.DualTypeWidget
import com.cablemc.pokemoncobbled.common.client.gui.summary.widgets.type.SingleTypeWidget
import com.cablemc.pokemoncobbled.common.client.render.drawScaled
import com.cablemc.pokemoncobbled.common.pokemon.Pokemon
import com.cablemc.pokemoncobbled.common.util.asTranslated
import com.cablemc.pokemoncobbled.common.util.cobbledResource
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import kotlin.math.roundToInt

/**
 * Displays the different possible pending evolutions in the [Pokemon.evolutionProxy].
 *
 * @property pokemon The currently selected [Pokemon] from the party list.
 *
 * @author Licious
 * @since June 15th, 2022
 */
class EvolutionListScrollPane(private val pokemon: Pokemon) : ModelSectionScrollPane<EvolutionListScrollPane.EvolutionOption>(
    overlayTexture = OVERLAY_RESOURCE,
    topOffset = 0,
    bottomOffset = 0,
    entryWidth = ENTRY_WIDTH,
    entryHeight = ENTRY_HEIGHT
) {

    var render = false

    override fun createEntries(): Collection<EvolutionOption> = this.pokemon.evolutionProxy.client().map { evolutionDisplay ->
        EvolutionOption(this.pokemon, evolutionDisplay)
    }

    override fun render(poseStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (this.render) {
            this.renderPropositionText(poseStack, this.left + PROPOSITION_X_OFFSET, this.top + PROPOSITION_Y_OFFSET)
            super.render(poseStack, mouseX, mouseY, partialTicks)
        }
    }

    private fun renderPropositionText(matrices: MatrixStack, x: Int, y: Int) {
        matrices.push()
        matrices.scale(PROPOSITION_TEXT_SCALE, PROPOSITION_TEXT_SCALE, 1F)
        drawText(
            poseStack = matrices,
            font = CobbledResources.NOTO_SANS_BOLD,
            text = "pokemoncobbled.ui.evolve_offer".asTranslated(),
            x = x / PROPOSITION_TEXT_SCALE, y = y / PROPOSITION_TEXT_SCALE,
            centered = true,
            colour = ColourLibrary.WHITE, shadow = false
        )
        matrices.pop()
    }

    inner class EvolutionOption(private val pokemon: Pokemon, private val evolution: EvolutionDisplay) : AlwaysSelectedEntryListWidget.Entry<EvolutionOption>() {

        private var lastKnownButton: SummaryButton? = null

        override fun render(
            matrices: MatrixStack,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            // We render this first so it fits nicely behind the entry itself
            this.renderModelUnderlay(matrices, x, y)
            // We want to offset the entries a bit for them to not collide with the scroll bar
            blitk(
                matrixStack = matrices,
                texture = ENTRY_RESOURCE,
                x = x + ENTRY_X_OFFSET, y = y,
                width = entryWidth, height = entryHeight,
            )
            this.renderPreviewName(matrices, x, y)
            this.renderButton(matrices, mouseX, mouseY, tickDelta, x, y)
            this.renderPreviewType(matrices, mouseX, mouseY, tickDelta, x, y)
            this.renderModelPreview(matrices, mouseX, mouseY, tickDelta, x, y)
        }

        // ToDo narration should return Undiscovered or something among those lines if not registered in the Pokédex just so it makes a bit more sense coming from TTS
        override fun getNarration(): Text = this.displayName()

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (this.lastKnownButton?.isHovered == true) {
                this.lastKnownButton?.onPress()
                return true
            }
            return false
        }

        private fun displayName(): MutableText {
            // ToDo return ??? if not registered as caught in Pokédex
            return this.evolution.species.translatedName
        }

        private fun renderModelUnderlay(matrices: MatrixStack, x: Int, y: Int) {
            blitk(
                matrixStack = matrices,
                texture = MODEL_UNDERLAY_RESOURCE,
                x = x + MODEL_UNDERLAY_X_OFFSET, y = y,
                width = MODEL_UNDERLAY_WIDTH, height = MODEL_UNDERLAY_HEIGHT,
            )
        }

        private fun renderPreviewName(matrices: MatrixStack, x: Int, y: Int) {
            // ToDo figure me out
            val client = MinecraftClient.getInstance()
            val text = this.displayName().apply {
                style = style.withFont(CobbledResources.NOTO_SANS_BOLD)
            }
            val textWidth = client.textRenderer.getWidth(text).toFloat()
            val scaleMultiplier = if (textWidth >= POKEMON_NAME_MAX_WIDTH) POKEMON_NAME_MAX_WIDTH / textWidth else 1F
            val textScale = (POKEMON_NAME_SCALE * scaleMultiplier).coerceAtMost(POKEMON_NAME_SCALE)
            client.textRenderer.drawScaled(
                matrixStack = matrices,
                text = text,
                x = x.toFloat() + POKEMON_NAME_X_OFFSET, y = y.toFloat() + POKEMON_NAME_Y_OFFSET + (1 - textScale),
                scaleX = textScale, scaleY = textScale,
                colour = ColourLibrary.WHITE
            )
        }

        private fun renderButton(matrices: MatrixStack, mouseX: Int, mouseY: Int, tickDelta: Float, x: Int, y: Int) {
            SummaryButton(
                x + BUTTON_X_OFFSET, y + BUTTON_Y_OFFSET,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                0, 0, 0,
                resource = BUTTON_RESOURCE,
                clickAction = { this.acceptAndClose() },
                text = "pokemoncobbled.ui.evolve".asTranslated()
            ).also { button ->
                this.lastKnownButton = button
                button.render(matrices, mouseX, mouseY, tickDelta)
            }
        }

        private fun acceptAndClose() {
            ModelWidget.render = true
            MinecraftClient.getInstance().player?.closeScreen()
            this.pokemon.evolutionProxy.client().start(this.evolution)
        }

        private fun renderPreviewType(matrices: MatrixStack, mouseX: Int, mouseY: Int, tickDelta: Float, x: Int, y: Int) {
            val isDualType = this.evolution.form.secondaryType != null
            matrices.push()
            matrices.scale(TYPE_SCALE, TYPE_SCALE, 1F)
            val xOffset = if (isDualType) DUAL_TYPE_X_OFFSET else SINGLE_TYPE_X_OFFSET
            val actualX = (x * 1.25).roundToInt() + xOffset
            val actualY = (y * 1.25).roundToInt() + TYPE_Y_OFFSET
            val widget = if (isDualType)
                DualTypeWidget(actualX, actualY, TYPE_WIDTH, TYPE_HEIGHT, Text.of(""), this.evolution.form.primaryType, this.evolution.form.secondaryType!!)
            else
                SingleTypeWidget(actualX, actualY, TYPE_WIDTH, TYPE_HEIGHT, this.evolution.form.primaryType, false)
            widget.render(matrices, mouseX, mouseY, tickDelta)
            matrices.pop()
        }

        private fun renderModelPreview(matrices: MatrixStack, mouseX: Int, mouseY: Int, tickDelta: Float, x: Int, y: Int) {

        }

    }

    companion object {

        private val OVERLAY_RESOURCE = cobbledResource("ui/summary/summary_moves_change.png")

        // Entry
        private val ENTRY_RESOURCE = cobbledResource("ui/summary/summary_info_evolve_slot.png")
        private const val ENTRY_WIDTH = 99
        private const val ENTRY_HEIGHT = 38
        private const val ENTRY_X_OFFSET = -7

        // Proposition text
        private const val PROPOSITION_TEXT_SCALE = .50F
        private const val PROPOSITION_X_OFFSET = 35
        private const val PROPOSITION_Y_OFFSET = -15

        // Pokémon name text
        private const val POKEMON_NAME_MAX_WIDTH = ENTRY_WIDTH - 5
        private const val POKEMON_NAME_SCALE = .55F
        private const val POKEMON_NAME_X_OFFSET = 27
        private const val POKEMON_NAME_Y_OFFSET = 3

        // Confirmation button
        private val BUTTON_RESOURCE = cobbledResource("ui/summary/summary_info_evolve_slot_button.png")
        private const val BUTTON_WIDTH = 31
        private const val BUTTON_HEIGHT = 11
        private const val BUTTON_X_OFFSET = 57
        private const val BUTTON_Y_OFFSET = 15

        // Type preview
        private const val TYPE_WIDTH = 19
        private const val TYPE_HEIGHT = 19
        private const val TYPE_SCALE = .80F
        private const val DUAL_TYPE_X_OFFSET = 35
        private const val SINGLE_TYPE_X_OFFSET = DUAL_TYPE_X_OFFSET + 7
        private const val TYPE_Y_OFFSET = 17

        // Model preview
        private val MODEL_UNDERLAY_RESOURCE = cobbledResource("ui/summary/summary_info_evolve_underlay.png")
        private const val MODEL_UNDERLAY_WIDTH = 33
        private const val MODEL_UNDERLAY_HEIGHT = MODEL_UNDERLAY_WIDTH
        private const val MODEL_UNDERLAY_X_OFFSET = -4

    }

}