package com.cablemc.pokemoncobbled.forge.common.net.messages.client.storage.party

import com.cablemc.pokemoncobbled.common.api.net.NetworkPacket
import net.minecraft.network.FriendlyByteBuf
import java.util.UUID

/**
 * Creates a party on the client side with the given UUID and slot count.
 *
 * This can be used for immediately telling the client that this is their party to use
 * in overlay rendering, but generally is just necessary before sending Pokémon updates
 * targeting this store.
 *
 * Handled by [com.cablemc.pokemoncobbled.client.net.storage.party.InitializePartyHandler]
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
class InitializePartyPacket() : NetworkPacket {
    /** Whether this should be set as the player's party for rendering immediately. */
    var isThisPlayerParty: Boolean = false
    /** The UUID of the party storage. Does not need to be the player's UUID. */
    var uuid = UUID.randomUUID()
    /** The number of slots in the party. Defaults to 6. */
    var slots: Int = 6

    constructor(isThisPlayerParty: Boolean, uuid: UUID, slots: Int): this() {
        this.isThisPlayerParty = isThisPlayerParty
        this.uuid = uuid
        this.slots = slots
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeBoolean(isThisPlayerParty)
        buffer.writeUUID(uuid)
        buffer.writeByte(slots)
    }

    override fun decode(buffer: FriendlyByteBuf) {
        isThisPlayerParty = buffer.readBoolean()
        uuid = buffer.readUUID()
        slots = buffer.readUnsignedByte().toInt()
    }
}