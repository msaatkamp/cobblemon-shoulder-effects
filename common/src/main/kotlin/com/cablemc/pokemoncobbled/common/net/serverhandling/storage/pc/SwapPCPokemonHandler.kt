package com.cablemc.pokemoncobbled.common.net.serverhandling.storage.pc

import com.cablemc.pokemoncobbled.common.CobbledNetwork.NetworkContext
import com.cablemc.pokemoncobbled.common.api.storage.pc.link.PCLinkManager
import com.cablemc.pokemoncobbled.common.net.messages.client.storage.pc.ClosePCPacket
import com.cablemc.pokemoncobbled.common.net.messages.server.storage.pc.SwapPCPokemonPacket
import com.cablemc.pokemoncobbled.common.net.serverhandling.ServerPacketHandler
import net.minecraft.server.network.ServerPlayerEntity

object SwapPCPokemonHandler : ServerPacketHandler<SwapPCPokemonPacket> {
    override fun invokeOnServer(packet: SwapPCPokemonPacket, ctx: NetworkContext, player: ServerPlayerEntity) {
        val pc = PCLinkManager.getPC(player) ?: return run { ClosePCPacket().sendToPlayer(player) }
        if (pc[packet.position1]?.uuid != packet.pokemon1ID || pc[packet.position2]?.uuid != packet.pokemon2ID) {
            return
        }
        pc.swap(packet.position1, packet.position2)
    }
}