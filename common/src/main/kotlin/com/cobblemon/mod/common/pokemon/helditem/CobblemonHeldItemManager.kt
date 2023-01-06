/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.helditem

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemManager
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.battles.runner.GraalShowdown
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.registry.Registry

/**
 * The Cobblemon implementation of [HeldItemManager].
 * It directly consumes the [Pokemon.heldItem] when required.
 * The literal IDs are the path of item identifiers under the [Cobblemon.MODID] namespace.
 *
 * @author Licious
 * @since December 30th, 2022
 */
object CobblemonHeldItemManager : HeldItemManager {

    private val itemIDs = hashSetOf<String>()
    private val itemLang = hashMapOf<String, Text>()

    /**
     * Loads the item IDs by querying them from the [GraalShowdown.context].
     * This should be invoked by [PokemonSpecies.reload] as it triggers a reload of the Cobblemon Showdown mod.
     */
    internal fun load() {
        this.itemIDs.clear()
        val script = """
            PokemonShowdown.Dex.mod("${Cobblemon.MODID}")
              .items.all()
              .map(item => item.id);
        """.trimIndent()
        val arrayResult = GraalShowdown.context.eval("js", script)
        for (i in 0 until arrayResult.arraySize) {
            this.itemIDs+= arrayResult.getArrayElement(i).asString()
        }
        Registry.ITEM.forEach { item ->
            val showdownId = this.showdownIdOf(item)
            if (showdownId != null) {
                this.itemLang[showdownId] = item.name
            }
        }
        Cobblemon.LOGGER.info("Imported {} held item IDs from showdown", this.itemIDs.size)
    }

    override fun showdownId(pokemon: BattlePokemon): String? = this.showdownIdOf(pokemon.effectedPokemon.heldItemNoCopy().item)

    override fun nameOf(showdownId: String): Text = this.itemLang[showdownId] ?: Text.of(showdownId)

    override fun consume(pokemon: BattlePokemon) {
        pokemon.effectedPokemon.swapHeldItem(ItemStack.EMPTY)
    }

    override fun handleStartInstruction(pokemon: BattlePokemon, battle: PokemonBattle, battleMessage: BattleMessage) {
        val itemID = battleMessage.argumentAt(1)?.lowercase()?.replace(" ", "") ?: run {
            battle.broadcastChatMessage(Text.literal("Failed to handle '-item' action: ${battleMessage.rawMessage}").red())
            Cobblemon.LOGGER.error("Failed to handle '-item' action: ${battleMessage.rawMessage}")
            return
        }
        val effect = battleMessage.effect()
        val battlerName = pokemon.getName()
        // The only item using the null effect gimmick
        if (effect == null && itemID == "airballoon") {
            battle.broadcastChatMessage(battleLang("item.air_balloon.start", battlerName))
            return
        }
        val source = battleMessage.actorAndActivePokemonFromOptional(battle)?.second?.battlePokemon
        val itemName = this.nameOf(itemID)
        val sourceName = source?.getName() ?: Text.of("UNKNOWN")
        val text = when (effect?.id?.lowercase() ?: "") {
            "pickup", "recycle" -> battleLang("item.recycle_or_pickup.start", battlerName, itemName)
            "frisk" -> battleLang("item.frisk.start", sourceName, battlerName, itemName)
            // The "source" is actually the target here
            "magician", "pickpocket", "thief", "covet" -> battleLang("item.take_item.start", battlerName, sourceName, itemName)
            "harvest" -> battleLang("item.harvest.start", battlerName, itemName)
            "bestow" -> battleLang("item.bestow.start", battlerName, itemName, sourceName)
            "switcheroo", "trick" -> battleLang("item.tricked.start", battlerName)
            else -> Text.literal("Cannot interpret ${battleMessage.rawMessage}").red().also {
                Cobblemon.LOGGER.error("Failed to handle '-item' action: ${battleMessage.rawMessage}")
            }
        }
        battle.broadcastChatMessage(text)
    }

    override fun handleEndInstruction(pokemon: BattlePokemon, battle: PokemonBattle, battleMessage: BattleMessage) {
        // These are sent when showdown wants the client to animate something but not produce any text
        if (battleMessage.hasOptionalArgument("silent")) {
            TODO("Consume item")
            return
        }
        val itemID = battleMessage.argumentAt(1)?.lowercase()?.replace(" ", "") ?: run {
            battle.broadcastChatMessage(Text.literal("Failed to handle '-enditem' action: ${battleMessage.rawMessage}").red())
            Cobblemon.LOGGER.error("Failed to handle '-enditem' action: ${battleMessage.rawMessage}")
            return
        }
        val battlerName = pokemon.getName()
        val itemName = this.nameOf(itemID)
        if (battleMessage.hasOptionalArgument("eat")) {
            battle.broadcastChatMessage(battleLang("item.eat.end", battlerName, itemName))
            TODO("Consume item")
            return
        }
        val source = battleMessage.actorAndActivePokemonFromOptional(battle)?.second?.battlePokemon
        val sourceName = source?.getName() ?: Text.of("UNKNOWN")
        val effect = battleMessage.effect()
        val text = when (effect?.id?.lowercase() ?: "") {
            "fling" -> battleLang("item.fling.end", battlerName, itemName)
            "knockoff" -> battleLang("item.knock_off.end", sourceName, battlerName, itemName)
            "gem" -> battleLang("item.gem.end", itemName, battlerName)
            "incinerate" -> battleLang("item.incinerate.end", battlerName, itemName)
            "stealeat" -> battleLang("item.steal_eat.end", sourceName, battlerName, itemName)
            else -> when (itemID) {
                "airballoon" -> battleLang("item.air_balloon.end", battlerName)
                "focussash" -> battleLang("item.hung_on.end", battlerName, itemName)
                "redcard" -> battleLang("item.red_card.end", battlerName, sourceName)
                "berryjuice" -> battleLang("item.berry_juice.end", battlerName)
                "boosterenergy", "electricseed", "grassyseed", "mistyseed", "psychicseed", "roomservice" -> battleLang("item.item.used_its.end", battlerName, itemName)
                "mentalherb" -> battleLang("item.mental_herb.end", battlerName)
                "powerherb" -> battleLang("item.power_herb.end", battlerName)
                "mirrorherb" -> battleLang("item.mirror_herb.end", battlerName)
                "whiteherb" -> battleLang("item.white_herb.end", battlerName)
                else -> Text.literal("Cannot interpret ${battleMessage.rawMessage}").red().also {
                    Cobblemon.LOGGER.error("Failed to handle '-enditem' action: ${battleMessage.rawMessage}")
                }
            }
        }
        battle.broadcastChatMessage(text)
        TODO("Consume item")
    }

    /**
     * Find the Showdown literal ID of the given [item].
     * This only works for items under the [Cobblemon.MODID] namespace.
     * If you wish to support your own items you need to implement your own [HeldItemManager].
     *
     * @param item The [Item] being queried.
     * @return The literal Showdown ID if any.
     */
    fun showdownIdOf(item: Item): String? {
        val identifier = Registry.ITEM.getId(item)
        if (identifier.namespace != Cobblemon.MODID) {
            return null
        }
        val path = identifier.path.replace("_", "")
        if (this.itemIDs.contains(path)) {
            return path
        }
        return null
    }

}