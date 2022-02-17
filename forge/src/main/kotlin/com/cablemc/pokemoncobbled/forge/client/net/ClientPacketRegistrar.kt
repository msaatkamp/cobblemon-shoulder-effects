package com.cablemc.pokemoncobbled.forge.client.net

import com.cablemc.pokemoncobbled.forge.client.net.gui.SummaryUIPacketHandler
import com.cablemc.pokemoncobbled.forge.client.net.pokemon.update.SingleUpdatePacketHandler
import com.cablemc.pokemoncobbled.forge.client.net.storage.party.InitializePartyHandler
import com.cablemc.pokemoncobbled.forge.client.net.storage.party.MovePartyPokemonHandler
import com.cablemc.pokemoncobbled.forge.client.net.storage.party.RemovePartyPokemonHandler
import com.cablemc.pokemoncobbled.forge.client.net.storage.party.SetPartyPokemonHandler
import com.cablemc.pokemoncobbled.forge.client.net.storage.party.SetPartyReferenceHandler
import com.cablemc.pokemoncobbled.forge.client.net.storage.party.SwapPartyPokemonHandler
import com.cablemc.pokemoncobbled.forge.common.api.net.SidedPacketRegistrar
import com.cablemc.pokemoncobbled.forge.common.net.messages.client.pokemon.update.LevelUpdatePacket
import com.cablemc.pokemoncobbled.forge.common.net.messages.client.pokemon.update.MoveSetUpdatePacket
import com.cablemc.pokemoncobbled.forge.common.net.messages.client.pokemon.update.NatureUpdatePacket
import com.cablemc.pokemoncobbled.forge.common.net.messages.client.pokemon.update.ShinyUpdatePacket
import com.cablemc.pokemoncobbled.forge.common.net.messages.client.pokemon.update.PokemonStateUpdatePacket
import com.cablemc.pokemoncobbled.forge.common.net.messages.client.pokemon.update.SpeciesUpdatePacket

/**
 * Registers packet handlers that the client will need. This is separated from the server ones
 * not because they have to be, but because it helps us guarantee client access safety in a CI
 * job.
 *
 * @author Hiroku
 * @since November 27th, 2021
 */
object ClientPacketRegistrar : SidedPacketRegistrar() {
    override fun registerHandlers() {
        registerHandler<LevelUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<SpeciesUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<PokemonStateUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<ShinyUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<NatureUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<MoveSetUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler(InitializePartyHandler)
        registerHandler(SetPartyPokemonHandler)
        registerHandler(MovePartyPokemonHandler)
        registerHandler(RemovePartyPokemonHandler)
        registerHandler(SwapPartyPokemonHandler)
        registerHandler(SetPartyReferenceHandler)
        registerHandler(SummaryUIPacketHandler)
    }
}
