/*
 * Copyright (C) 2022 Pokemod Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemod.common.net.messages.server.storage.party

import com.cablemc.pokemod.common.api.net.NetworkPacket
import com.cablemc.pokemod.common.api.storage.party.PartyPosition
import com.cablemc.pokemod.common.api.storage.party.PartyPosition.Companion.readPartyPosition
import com.cablemc.pokemod.common.api.storage.party.PartyPosition.Companion.writePartyPosition
import com.cablemc.pokemod.common.net.serverhandling.storage.pc.ReleasePartyPokemonHandler
import java.util.UUID
import net.minecraft.network.PacketByteBuf

/**
 * Packet sent when the player is releasing one of their Pokémon from their party.
 *
 * Handled by [ReleasePartyPokemonHandler]
 *
 * @author Hiroku
 * @since October 31st, 2022
 */
class ReleasePartyPokemonPacket() : NetworkPacket {
    lateinit var pokemonID: UUID
    lateinit var position: PartyPosition

    constructor(pokemonID: UUID, position: PartyPosition): this() {
        this.pokemonID = pokemonID
        this.position = position
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(pokemonID)
        buffer.writePartyPosition(position)
    }

    override fun decode(buffer: PacketByteBuf) {
        pokemonID = buffer.readUuid()
        position = buffer.readPartyPosition()
    }
}