package com.cablemc.pokemoncobbled.common

import com.cablemc.pokemoncobbled.common.command.ShowdownWriteCommand
import com.cablemc.pokemoncobbled.common.command.GivePokemon
import com.cablemc.pokemoncobbled.common.command.PokeSpawn
import com.cablemc.pokemoncobbled.common.command.ShowdownReadCommand
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object CommandRegistrar {
    @SubscribeEvent
    fun on(event: RegisterCommandsEvent) {
        PokeSpawn.register(event.dispatcher)
        GivePokemon.register(event.dispatcher)
        ShowdownWriteCommand.register(event.dispatcher)
        ShowdownReadCommand.register(event.dispatcher)
    }
}