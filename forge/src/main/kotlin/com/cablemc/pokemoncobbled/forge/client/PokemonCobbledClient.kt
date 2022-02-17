package com.cablemc.pokemoncobbled.forge.client

import com.cablemc.pokemoncobbled.forge.client.gui.PartyOverlay
import com.cablemc.pokemoncobbled.forge.client.keybinding.DownShiftPartyBinding
import com.cablemc.pokemoncobbled.forge.client.keybinding.HidePartyBinding
import com.cablemc.pokemoncobbled.forge.client.keybinding.PartySendBinding
import com.cablemc.pokemoncobbled.forge.client.keybinding.PokeNavigatorBinding
import com.cablemc.pokemoncobbled.forge.client.keybinding.SummaryBinding
import com.cablemc.pokemoncobbled.forge.client.keybinding.UpShiftPartyBinding
import com.cablemc.pokemoncobbled.forge.client.listener.ClientSchedulingListener
import com.cablemc.pokemoncobbled.forge.client.net.ClientPacketRegistrar
import com.cablemc.pokemoncobbled.forge.client.render.layer.PokemonOnShoulderLayer
import com.cablemc.pokemoncobbled.forge.client.render.pokeball.PokeBallRenderer
import com.cablemc.pokemoncobbled.forge.client.render.pokemon.PokemonRenderer
import com.cablemc.pokemoncobbled.forge.client.storage.ClientStorageManager
import com.cablemc.pokemoncobbled.forge.common.entity.EntityRegistry
import com.cablemc.pokemoncobbled.forge.mod.PokemonCobbledMod
import net.minecraft.client.KeyMapping
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.EntityRenderers
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraftforge.client.ClientRegistry
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.registries.RegistryObject

object PokemonCobbledClient {
    val storage = ClientStorageManager()

    fun registerKeyBinds() {
        registerKeyBind(PartySendBinding)
        registerKeyBind(DownShiftPartyBinding)
        registerKeyBind(UpShiftPartyBinding)
        registerKeyBind(PokeNavigatorBinding)
        registerKeyBind(SummaryBinding)
        registerKeyBind(HidePartyBinding)
    }

    fun registerKeyBind(binding: KeyMapping) {
        ClientRegistry.registerKeyBinding(binding)
        MinecraftForge.EVENT_BUS.register(binding)
    }

    fun registerRenderers() {
        registerEntityRenderer(EntityRegistry.POKEMON) { PokemonRenderer(it) }
        registerEntityRenderer(EntityRegistry.EMPTY_POKEBALL) { PokeBallRenderer(it) }
    }

    fun initialize() {
        PokemonCobbledMod.EVENT_BUS.register(ClientPacketRegistrar)
        MinecraftForge.EVENT_BUS.register(storage)
        MinecraftForge.EVENT_BUS.register(ClientSchedulingListener)
        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(PartyOverlay())

        ClientPacketRegistrar.registerHandlers()
        registerKeyBinds()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun on(event: PlayerEvent.PlayerLoggedInEvent) {
        storage.checkSelectedPokemon()
    }

    fun onAddLayer(event : EntityRenderersEvent.AddLayers) {
        var renderer: LivingEntityRenderer<Player, PlayerModel<Player>>? = event.getSkin("default")
        renderer?.addLayer(PokemonOnShoulderLayer(renderer))
        renderer = event.getSkin("slim")
        renderer?.addLayer(PokemonOnShoulderLayer(renderer))
    }

    private fun <T : Entity> registerEntityRenderer(
        registry: RegistryObject<EntityType<T>>,
        renderer: (EntityRendererProvider.Context) -> EntityRenderer<T>
    ) {
        EntityRenderers.register(registry.get(), renderer)
    }
}