/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.pokemon.update

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.EvolutionUpdatePacket
class EvolutionUpdatePacketHandler<T : EvolutionUpdatePacket> : ClientPacketHandler<T> {

    override fun invokeOnClient(packet: T, ctx: CobblemonNetwork.NetworkContext) {
        CobblemonClient.storage.locatePokemon(packet.storeID, packet.pokemonID)?.let { pokemon -> packet.applyToPokemon(pokemon) }
    }

}