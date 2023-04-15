/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendToPlayers
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.abilities.Ability
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.CobblemonEvents.FRIENDSHIP_UPDATED
import com.cobblemon.mod.common.api.events.CobblemonEvents.POKEMON_FAINTED
import com.cobblemon.mod.common.api.events.pokemon.*
import com.cobblemon.mod.common.api.moves.BenchedMove
import com.cobblemon.mod.common.api.moves.BenchedMoves
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.aspect.AspectProvider
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionController
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionDisplay
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionProxy
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroup
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceSource
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures
import com.cobblemon.mod.common.api.pokemon.friendship.FriendshipMutationCalculator
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels
import com.cobblemon.mod.common.api.pokemon.moves.LearnsetQuery
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.SettableObservable
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.scheduling.afterOnMain
import com.cobblemon.mod.common.api.storage.StoreCoordinates
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.config.CobblemonConfig
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.PokemonUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.AbilityUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.AspectsUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.BenchedMovesUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.CaughtBallUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.EVsUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.ExperienceUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.FriendshipUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.GenderUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.HealthUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.HeldItemUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.IVsUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.MoveSetUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.NatureUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.PokemonStateUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.ShinyUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.SpeciesUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.StatusUpdatePacket
import com.cobblemon.mod.common.net.serverhandling.storage.SEND_OUT_DURATION
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.InactivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.PokemonState
import com.cobblemon.mod.common.pokemon.activestate.SentOutState
import com.cobblemon.mod.common.pokemon.evolution.CobblemonEvolutionProxy
import com.cobblemon.mod.common.pokemon.evolution.progress.DamageTakenEvolutionProgress
import com.cobblemon.mod.common.pokemon.evolution.progress.RecoilEvolutionProgress
import com.cobblemon.mod.common.pokemon.feature.SeasonFeatureHandler
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.getServer
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.setPositionSafely
import com.cobblemon.mod.common.util.toBlockPos
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.JsonOps
import java.util.concurrent.CompletableFuture
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement.COMPOUND_TYPE
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import net.minecraft.util.math.MathHelper.ceil
import net.minecraft.util.math.MathHelper.clamp
import net.minecraft.util.math.Vec3d
import java.util.UUID
import java.util.Optional

import net.minecraft.entity.effect.StatusEffectInstance

open class Pokemon : ShowdownIdentifiable {
    var uuid = UUID.randomUUID()
    var species = PokemonSpecies.random()
        set(value) {
            if (PokemonSpecies.getByIdentifier(value.resourceIdentifier) == null) {
                throw IllegalArgumentException("Cannot set a species that isn't registered")
            }
            val quotient = clamp(currentHealth / hp.toFloat(), 0F, 1F)
            field = value
            val newFeatures = SpeciesFeatures.getFeaturesFor(species).mapNotNull { it.invoke(this) }
            features.clear()
            features.addAll(newFeatures)
            this.evolutionProxy.current().clear()
            updateAspects()
            updateForm()
            checkGender()
            checkAbility()
            updateHP(quotient)
            /*
            if (ability.template == Abilities.DUMMY && !isClient) {
                ability = form.abilities.select(value, aspects)
            }
             */
            _species.emit(value)
        }

    var form = species.standardForm
        set(value) {
            val old = field
            field = value
            this.sanitizeFormChangeMoves(old)
            // Evo proxy is already cleared on species update but the form may be changed by itself, this is fine and no unnecessary packets will be sent out
            this.evolutionProxy.current().clear()
            // Species updates already update HP but just a form change may require it
            val quotient = clamp(currentHealth / hp.toFloat(), 0F, 1F)
            findAndLearnFormChangeMoves()
            updateHP(quotient)
            checkGender()
            checkAbility()
            _form.emit(value)
        }

    // Need to happen before currentHealth init due to the calc
    var ivs = IVs.createRandomIVs()
    var evs = EVs.createEmpty()

    val displayName: MutableText
        get() = species.translatedName

    var level = 1
        set(value) {
            if (value < 1) {
                throw IllegalArgumentException("Level cannot be negative")
            }
            if (value > Cobblemon.config.maxPokemonLevel) {
                throw IllegalArgumentException("Cannot set level above the configured maximum of ${Cobblemon.config.maxPokemonLevel}")
            }
            val hpRatio = (currentHealth / hp.toFloat()).coerceIn(0F, 1F)
            /*
             * When people set the level programmatically the experience value will become incorrect.
             * Specifically check for when there's a mismatch and update the experience.
             */
            field = value
            if (experienceGroup.getLevel(experience) != value || value == Cobblemon.config.maxPokemonLevel) {
                experience = experienceGroup.getExperience(value)
            }
//            _level.emit(value)

            currentHealth = ceil(hpRatio * hp).coerceIn(0..hp)
        }

    var currentHealth = this.hp
        set(value) {
            if (value == field) {
                return
            }

            if (value <= 0) {
                entity?.health = 0F
            }
            field = max(min(hp, value), 0)
            _currentHealth.emit(field)

            // If the Pokémon is fainted, give it a timer for it to wake back up
            if (this.isFainted()) {
                decrementFriendship(1)
                val faintTime = Cobblemon.config.defaultFaintTimer
                POKEMON_FAINTED.post(PokemonFaintedEvent(this, faintTime)) {
                    this.faintedTimer = it.faintedTimer
                }
                // These are meant to reset on faint
                this.evolutionProxy.current().progress()
                    .filter { it is RecoilEvolutionProgress || it is DamageTakenEvolutionProgress }
                    .forEach { it.reset() }
            }
            this.healTimer = Cobblemon.config.healTimer
        }
    var gender = Gender.GENDERLESS
        set(value) {
            field = value
            if (!isClient) {
                checkGender()
            }
            if (field == value) {
                updateAspects()
                _gender.emit(value)
            }
        }
    var status: PersistentStatusContainer? = null
        set(value) {
            field = value
            this._status.emit(value?.status?.name?.toString() ?: "")
        }
    var experience = 0
        internal set(value) {
            field = value
            if (this.level == Cobblemon.config.maxPokemonLevel) {
                field = this.experienceGroup.getExperience(this.level)
            }
            _experience.emit(field)
        }

    /**
     * The friendship amount on this Pokémon.
     * Use [setFriendship], [incrementFriendship] and [decrementFriendship] for safe mutation with return feedback.
     * See this [page](https://bulbapedia.bulbagarden.net/wiki/Friendship) for more information.
     */
    var friendship = this.form.baseFriendship
        private set(value) {
            // Don't check on client, that way we don't need to actually sync the config value it doesn't affect anything
            if (!this.isClient && !this.isPossibleFriendship(value)) {
                return
            }
            FRIENDSHIP_UPDATED.post(FriendshipUpdatedEvent(this, value)) {
                field = it.newFriendship
                _friendship.emit(it.newFriendship)
            }
        }
    var state: PokemonState = InactivePokemonState()
        set(value) {
            val current = field
            if (current is ActivePokemonState && !isClient) {
                if (value !is ActivePokemonState || value.entity != current.entity) {
                    current.recall()
                }
            }
            field = value
            _state.emit(value)
        }

    val entity: PokemonEntity?
        get() = state.let { if (it is ActivePokemonState) it.entity else null }

    val primaryType: ElementalType
        get() = form.primaryType

    val secondaryType: ElementalType?
        get() = form.secondaryType

    val types: Iterable<ElementalType>
        get() = form.types

    var shiny = false
        set(value) {
            field = value
            updateAspects()
            _shiny.emit(value)
        }

    var nature = Natures.getRandomNature()
        set(value) { field = value ; _nature.emit(value.name.toString()) }
    var mintedNature: Nature? = null
        set(value) { field = value ; _mintedNature.emit(value?.name?.toString() ?: "") }

    val moveSet = MoveSet()

    val experienceGroup: ExperienceGroup
        get() = form.experienceGroup

    var faintedTimer: Int = -1
        set(value) {
            field = value
            anyChangeObservable.emit(this)
        }

    var healTimer: Int = -1
        set(value) {
            field = value
            anyChangeObservable.emit(this)
        }

    /**
     * All moves that the Pokémon has, at some point, known. This is to allow players to
     * swap in moves they've used before at any time, while holding onto the remaining PP
     * that they had last.
     */
    val benchedMoves = BenchedMoves()

    var ability: Ability = Abilities.DUMMY.create()
        set(value) {
            if (field != value) {
                _ability.emit(value)
            }
            field = value
        }

    val hp: Int
        get() = getStat(Stats.HP)
    val attack: Int
        get() = getStat(Stats.ATTACK)
    val defence: Int
        get() = getStat(Stats.DEFENCE)
    val specialAttack: Int
        get() = getStat(Stats.SPECIAL_ATTACK)
    val specialDefence: Int
        get() = getStat(Stats.SPECIAL_DEFENCE)
    val speed: Int
        get() = getStat(Stats.SPEED)

    var scaleModifier = 1F

    var caughtBall: PokeBall = PokeBalls.POKE_BALL
        set(value) { field = value ; _caughtBall.emit(caughtBall.name.toString()) }
    var features = mutableListOf<SpeciesFeature>()

    fun asRenderablePokemon() = RenderablePokemon(species, aspects)
    var aspects = setOf<String>()
        set(value) {
            if (field != value) {
                field = value
                if (!isClient) {
                    updateForm()
                }
                _aspects.emit(value)
            }
        }

    internal var isClient = false
    val storeCoordinates = SettableObservable<StoreCoordinates<*>?>(null)

    // We want non-optional evolutions to trigger first to avoid unnecessary packets and any cost associate with an optional one that would just be lost
    val evolutions: Iterable<Evolution> get() = this.form.evolutions.sortedBy { evolution -> evolution.optional }

    val preEvolution: PreEvolution? get() = this.form.preEvolution

    // Lazy due to leaking this
    /**
     * Provides the sided [EvolutionController]s, these operations can be done safely with a simple side check.
     * This can be done beforehand or using [EvolutionProxy.isClient].
     */
    val evolutionProxy: EvolutionProxy<EvolutionDisplay, Evolution> by lazy { CobblemonEvolutionProxy(this, this.isClient) }

    val customProperties = mutableListOf<CustomPokemonProperty>()

    /**
     * The [ItemStack] this Pokémon is holding.
     */
    private var heldItem: ItemStack = ItemStack.EMPTY

    open fun getStat(stat: Stat) = Cobblemon.statProvider.getStatForPokemon(this, stat)

    /**
     * The literal Showdown ID of this Pokémon.
     * This will either use [Species.showdownId] or [FormData.showdownId] depending on if the [form] is the base one or not.
     *
     * @return The literal Showdown ID of this Pokémon.
     */
    override fun showdownId(): String {
        if (this.form == this.species.standardForm) {
            return this.species.showdownId()
        }
        return this.form.showdownId()
    }

    fun sendOut(level: ServerWorld, position: Vec3d, mutation: (PokemonEntity) -> Unit = {}): PokemonEntity? {
        CobblemonEvents.POKEMON_SENT_PRE.postThen(PokemonSentPreEvent(this, level, position)) {
            SeasonFeatureHandler.updateSeason(this, level, position.toBlockPos())
            val entity = PokemonEntity(level, this)
            entity.setPositionSafely(position)
            mutation(entity)
            level.spawnEntity(entity)
            state = SentOutState(entity)

            // The code below will be used to test on use pokemons effects
            val pokemonNames = arrayOf("absol", "zubat", "weedle")
            val owner: ServerPlayerEntity? = this.getOwnerPlayer() as? ServerPlayerEntity

            if (owner != null && entity.displayName.string.lowercase() in pokemonNames) {
                // do something with the owner entity

                val badOmenEffect = StatusEffectInstance(StatusEffects.BAD_OMEN, 6000, 0)
                owner.addStatusEffect(badOmenEffect)
            }
            return entity
        }
        return null;
    }

    fun sendOutWithAnimation(source: LivingEntity, level: ServerWorld, position: Vec3d, battleId: UUID? = null, mutation: (PokemonEntity) -> Unit = {}): CompletableFuture<PokemonEntity> {
        val future = CompletableFuture<PokemonEntity>()
        sendOut(level, position) {
            level.playSoundServer(position, CobblemonSounds.POKE_BALL_SEND_OUT.get(), volume = 0.2F)
            it.phasingTargetId.set(source.id)
            it.beamModeEmitter.set(1)
            it.battleId.set(Optional.ofNullable(battleId))

            afterOnMain(seconds = SEND_OUT_DURATION) {
                it.phasingTargetId.set(-1)
                it.beamModeEmitter.set(0)
                future.complete(it)
                CobblemonEvents.POKEMON_SENT_POST.post(PokemonSentPostEvent(this, it))
            }

            mutation(it)
        }
        return future
    }
    fun recall() {
        CobblemonEvents.POKEMON_RECALLED.post(PokemonRecalledEvent(this, this.entity))
        this.state = InactivePokemonState()
    }

    fun tryRecallWithAnimation() {
        if (this.entity != null) {
            this.entity?.recallWithAnimation()
            return
        }
        this.recall()
    }

    fun heal() {
        this.currentHealth = hp
        this.moveSet.heal()
        this.status = null
        this.faintedTimer = -1
        this.healTimer = -1
        val entity = entity
        entity?.heal(entity.maxHealth - entity.health)
    }

    fun didSleep() {
        this.currentHealth = min((currentHealth + (hp / 2)), hp)
        this.status = null
        this.faintedTimer = -1
        this.healTimer = -1
        val entity = entity
        entity?.heal(entity.maxHealth - entity.health)
    }

    /**
     * Check if this Pokémon can be healed.
     * This verifies if HP is not maxed, any status is present or any move is not full PP.
     *
     * @return If this Pokémon can be healed.
     */
    fun canBeHealed() = this.hp != this.currentHealth || this.status != null || this.moveSet.any { move -> move.currentPp != move.maxPp }

    fun isFainted() = currentHealth <= 0

    private fun updateHP(quotient: Float) {
        currentHealth = (hp * quotient).roundToInt()
    }

    fun applyStatus(status: PersistentStatus) {
        this.status = PersistentStatusContainer(status, status.statusPeriod().random())
        if (this.status != null) {
            this._status.emit(this.status!!.status.name.toString())
        }
    }

    /**
     * A utility method that checks if this Pokémon species or form data contains the [CobblemonPokemonLabels.LEGENDARY] label.
     * This is used in Pokémon officially considered legendary.
     *
     * @return If the Pokémon is legendary.
     */
    fun isLegendary() = this.hasLabels(CobblemonPokemonLabels.LEGENDARY)

    /**
     * A utility method that checks if this Pokémon species or form data contains the [CobblemonPokemonLabels.ULTRA_BEAST] label.
     * This is used in Pokémon officially considered legendary.
     *
     * @return If the Pokémon is an ultra beast.
     */
    fun isUltraBeast() = this.hasLabels(CobblemonPokemonLabels.ULTRA_BEAST)

    /**
     * Checks if a Pokémon has all the given labels.
     * Tags used by the mod can be found in [CobblemonPokemonLabels].
     *
     * @param labels The different tags being queried.
     * @return If the Pokémon has all the given labels.
     */
    fun hasLabels(vararg labels: String) = labels.all { label -> this.form.labels.any { it.equals(label, true) } }

    /**
     * Returns a copy of the held item.
     * In order to change the [ItemStack] use [swapHeldItem].
     *
     * @return A copy of the [ItemStack] held by this Pokémon.
     */
    fun heldItem(): ItemStack = this.heldItem.copy()


    /**
     * Returns the backing held item, this is intended to skip the unnecessary copy operation for our internal use.
     * No mutations should be done to it and expected to synchronize.
     * If you wish to do so remember to set it with [swapHeldItem].
     *
     * @return The [ItemStack] held by this Pokémon.
     */
    internal fun heldItemNoCopy(): ItemStack = this.heldItem

    /**
     * Swaps out the current [heldItem] for the given [stack].
     * The assigned [heldItem] will always have the [ItemStack.count] of 1.
     *
     * @param stack The new [ItemStack] being set as the held item.
     * @param decrement If the given [stack] should have [ItemStack.decrement] invoked with the parameter of 1. Default is true.
     * @return The existing [ItemStack] being held.
     */
    fun swapHeldItem(stack: ItemStack, decrement: Boolean = true): ItemStack {
        val giving = stack.copy().apply { count = 1 }
        if (decrement) {
            stack.decrement(1)
        }
        val existing = this.heldItem()
        this.heldItem = giving
        this._heldItem.emit(giving)
        return existing
    }

    /**
     * Swaps out the current [heldItem] for an [ItemStack.EMPTY].
     *
     * @return The existing [ItemStack] being held.
     */
    fun removeHeldItem(): ItemStack = this.swapHeldItem(ItemStack.EMPTY)

    fun saveToNBT(nbt: NbtCompound): NbtCompound {
        nbt.putString(DataKeys.POKEMON_LAST_SAVED_VERSION, Cobblemon.VERSION)
        nbt.putUuid(DataKeys.POKEMON_UUID, uuid)
        nbt.putString(DataKeys.POKEMON_SPECIES_IDENTIFIER, species.resourceIdentifier.toString())
        nbt.putString(DataKeys.POKEMON_FORM_ID, form.formOnlyShowdownId())
        nbt.putInt(DataKeys.POKEMON_EXPERIENCE, experience)
        nbt.putShort(DataKeys.POKEMON_LEVEL, level.toShort())
        nbt.putShort(DataKeys.POKEMON_FRIENDSHIP, friendship.toShort())
        nbt.putString(DataKeys.POKEMON_GENDER, gender.name)
        nbt.putShort(DataKeys.POKEMON_HEALTH, currentHealth.toShort())
        nbt.put(DataKeys.POKEMON_IVS, ivs.saveToNBT(NbtCompound()))
        nbt.put(DataKeys.POKEMON_EVS, evs.saveToNBT(NbtCompound()))
        nbt.put(DataKeys.POKEMON_MOVESET, moveSet.getNBT())
        nbt.putFloat(DataKeys.POKEMON_SCALE_MODIFIER, scaleModifier)
        nbt.putBoolean(DataKeys.POKEMON_SHINY, shiny)
        val abilityNBT = ability.saveToNBT(NbtCompound())
        nbt.put(DataKeys.POKEMON_ABILITY, abilityNBT)
        state.writeToNBT(NbtCompound())?.let { nbt.put(DataKeys.POKEMON_STATE, it) }
        status?.saveToNBT(NbtCompound())?.let { nbt.put(DataKeys.POKEMON_STATUS, it) }
        nbt.putString(DataKeys.POKEMON_CAUGHT_BALL, caughtBall.name.toString())
        nbt.putInt(DataKeys.POKEMON_FAINTED_TIMER, faintedTimer)
        nbt.putInt(DataKeys.POKEMON_HEALING_TIMER, healTimer)
        nbt.put(DataKeys.BENCHED_MOVES, benchedMoves.saveToNBT(NbtList()))
        nbt.put(DataKeys.POKEMON_EVOLUTIONS, this.evolutionProxy.saveToNBT())
        val propertyList = customProperties.map { it.asString() }.map { NbtString.of(it) }
        nbt.put(DataKeys.POKEMON_DATA, NbtList().also { it.addAll(propertyList) })
        nbt.putString(DataKeys.POKEMON_NATURE, nature.name.toString())
        features.forEach { it.saveToNBT(nbt) }
        if (!this.heldItem.isEmpty) {
            nbt.put(DataKeys.HELD_ITEM, this.heldItem.writeNbt(NbtCompound()))
        }
        return nbt
    }

    fun loadFromNBT(nbt: NbtCompound): Pokemon {
        val version = nbt.getString(DataKeys.POKEMON_LAST_SAVED_VERSION).takeIf { it.isNotBlank() } ?: "1.1.1"
        uuid = nbt.getUuid(DataKeys.POKEMON_UUID)
        try {
            val rawID = nbt.getString(DataKeys.POKEMON_SPECIES_IDENTIFIER).replace("pokemonCobblemon", "cobblemon")
            species = PokemonSpecies.getByIdentifier(Identifier(rawID))
                ?: throw IllegalStateException("Failed to read a species with identifier $rawID")
        } catch (e: InvalidIdentifierException) {
            throw IllegalStateException("Failed to read a species identifier from NBT")
        }
        form = species.forms.find { it.formOnlyShowdownId() == nbt.getString(DataKeys.POKEMON_FORM_ID) } ?: species.standardForm
        level = nbt.getShort(DataKeys.POKEMON_LEVEL).toInt()
        experience = nbt.getInt(DataKeys.POKEMON_EXPERIENCE).takeIf { experienceGroup.getLevel(it) == level } ?: experienceGroup.getExperience(level)
        friendship = nbt.getShort(DataKeys.POKEMON_FRIENDSHIP).toInt().coerceIn(0, if (this.isClient) Int.MAX_VALUE else Cobblemon.config.maxPokemonFriendship)
        gender = Gender.valueOf(nbt.getString(DataKeys.POKEMON_GENDER).takeIf { it.isNotBlank() } ?: Gender.MALE.name)
        currentHealth = nbt.getShort(DataKeys.POKEMON_HEALTH).toInt()
        ivs.loadFromNBT(nbt.getCompound(DataKeys.POKEMON_IVS))
        evs.loadFromNBT(nbt.getCompound(DataKeys.POKEMON_EVS))
        moveSet.loadFromNBT(nbt)
        scaleModifier = nbt.getFloat(DataKeys.POKEMON_SCALE_MODIFIER)
        val abilityNBT = nbt.getCompound(DataKeys.POKEMON_ABILITY) ?: NbtCompound()
        val abilityName = abilityNBT.getString(DataKeys.POKEMON_ABILITY_NAME).takeIf { it.isNotEmpty() } ?: "runaway"
        if (abilityName != "dummy") {
            ability = Abilities.getOrException(abilityName).create(abilityNBT)
        }
        shiny = nbt.getBoolean(DataKeys.POKEMON_SHINY)
        if (nbt.contains(DataKeys.POKEMON_STATE)) {
            val stateNBT = nbt.getCompound(DataKeys.POKEMON_STATE)
            val type = stateNBT.getString(DataKeys.POKEMON_STATE_TYPE)
            val clazz = PokemonState.states[type]
            state = clazz?.getDeclaredConstructor()?.newInstance()?.readFromNBT(stateNBT) ?: InactivePokemonState()
        }
        if (nbt.contains(DataKeys.POKEMON_STATUS)) {
            val statusNBT = nbt.getCompound(DataKeys.POKEMON_STATUS)
            status = PersistentStatusContainer.loadFromNBT(statusNBT)
        }
        faintedTimer = nbt.getInt(DataKeys.POKEMON_FAINTED_TIMER)
        healTimer = nbt.getInt(DataKeys.POKEMON_HEALING_TIMER)
        val ballName = nbt.getString(DataKeys.POKEMON_CAUGHT_BALL)
        caughtBall = PokeBalls.getPokeBall(Identifier(ballName)) ?: PokeBalls.POKE_BALL
        benchedMoves.loadFromNBT(nbt.getList(DataKeys.BENCHED_MOVES, COMPOUND_TYPE.toInt()))
        val propertiesList = nbt.getList(DataKeys.POKEMON_DATA, NbtString.STRING_TYPE.toInt())
        val properties = PokemonProperties.parse(propertiesList.joinToString(separator = " ") { it.asString() }, " ")
        this.customProperties.clear()
        this.customProperties.addAll(properties.customProperties)
        SpeciesFeatures.getFeaturesFor(species).forEach {
            val feature = it(nbt) ?: return@forEach
            features.removeIf { it.name == feature.name }
            features.add(feature)
        }
        this.nature = nbt.getString(DataKeys.POKEMON_NATURE).takeIf { it.isNotBlank() }?.let { Natures.getNature(Identifier(it))!! } ?: Natures.getRandomNature()
        updateAspects()
        checkAbility()
        nbt.get(DataKeys.POKEMON_EVOLUTIONS)?.let { tag -> this.evolutionProxy.loadFromNBT(tag) }
        if (nbt.contains(DataKeys.HELD_ITEM)) {
            this.heldItem = ItemStack.fromNbt(nbt.getCompound(DataKeys.HELD_ITEM))
        }
        return this
    }

    fun saveToJSON(json: JsonObject): JsonObject {
        json.addProperty(DataKeys.POKEMON_LAST_SAVED_VERSION, Cobblemon.VERSION)
        json.addProperty(DataKeys.POKEMON_UUID, uuid.toString())
        json.addProperty(DataKeys.POKEMON_SPECIES_IDENTIFIER, species.resourceIdentifier.toString())
        json.addProperty(DataKeys.POKEMON_FORM_ID, form.formOnlyShowdownId())
        json.addProperty(DataKeys.POKEMON_EXPERIENCE, experience)
        json.addProperty(DataKeys.POKEMON_LEVEL, level)
        json.addProperty(DataKeys.POKEMON_FRIENDSHIP, friendship)
        json.addProperty(DataKeys.POKEMON_HEALTH, currentHealth)
        json.addProperty(DataKeys.POKEMON_GENDER, gender.name)
        json.add(DataKeys.POKEMON_IVS, ivs.saveToJSON(JsonObject()))
        json.add(DataKeys.POKEMON_EVS, evs.saveToJSON(JsonObject()))
        json.add(DataKeys.POKEMON_MOVESET, moveSet.saveToJSON(JsonObject()))
        json.addProperty(DataKeys.POKEMON_SCALE_MODIFIER, scaleModifier)
        json.add(DataKeys.POKEMON_ABILITY, ability.saveToJSON(JsonObject()))
        json.addProperty(DataKeys.POKEMON_SHINY, shiny)
        state.writeToJSON(JsonObject())?.let { json.add(DataKeys.POKEMON_STATE, it) }
        status?.saveToJSON(JsonObject())?.let { json.add(DataKeys.POKEMON_STATUS, it) }
        json.addProperty(DataKeys.POKEMON_CAUGHT_BALL, caughtBall.name.toString())
        json.add(DataKeys.BENCHED_MOVES, benchedMoves.saveToJSON(JsonArray()))
        json.addProperty(DataKeys.POKEMON_FAINTED_TIMER, faintedTimer)
        json.addProperty(DataKeys.POKEMON_HEALING_TIMER, healTimer)
        json.add(DataKeys.POKEMON_EVOLUTIONS, this.evolutionProxy.saveToJson())
        val propertyList = customProperties.map { it.asString() }.map { JsonPrimitive(it) }
        json.add(DataKeys.POKEMON_DATA, JsonArray().also { propertyList.forEach(it::add) })
        json.addProperty(DataKeys.POKEMON_NATURE, nature.name.toString())
        features.forEach { it.saveToJSON(json) }
        if (!this.heldItem.isEmpty) {
            ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, this.heldItem).result().ifPresent { json.add(DataKeys.HELD_ITEM, it) }
        }
        return json
    }

    fun loadFromJSON(json: JsonObject): Pokemon {
        val version = json.get(DataKeys.POKEMON_LAST_SAVED_VERSION)?.asString ?: "1.1.1"
        uuid = UUID.fromString(json.get(DataKeys.POKEMON_UUID).asString)
        try {
            val rawID = json.get(DataKeys.POKEMON_SPECIES_IDENTIFIER).asString.replace("pokemonCobblemon", "cobblemon")
            species = PokemonSpecies.getByIdentifier(Identifier(rawID))
                ?: throw IllegalStateException("Failed to read a species with identifier $rawID")
        } catch (e: InvalidIdentifierException) {
            throw IllegalStateException("Failed to deserialize a species identifier")
        }
        form = species.forms.find { it.formOnlyShowdownId() == json.get(DataKeys.POKEMON_FORM_ID).asString } ?: species.standardForm
        level = json.get(DataKeys.POKEMON_LEVEL).asInt
        experience = json.get(DataKeys.POKEMON_EXPERIENCE).asInt.takeIf { experienceGroup.getLevel(it) == level } ?: experienceGroup.getExperience(level)
        friendship = json.get(DataKeys.POKEMON_FRIENDSHIP).asInt.coerceIn(0, if (this.isClient) Int.MAX_VALUE else Cobblemon.config.maxPokemonFriendship)
        currentHealth = json.get(DataKeys.POKEMON_HEALTH).asInt
        gender = Gender.valueOf(json.get(DataKeys.POKEMON_GENDER)?.asString ?: "male")
        ivs.loadFromJSON(json.getAsJsonObject(DataKeys.POKEMON_IVS))
        evs.loadFromJSON(json.getAsJsonObject(DataKeys.POKEMON_EVS))
        moveSet.loadFromJSON(json.get(DataKeys.POKEMON_MOVESET).asJsonObject)
        scaleModifier = json.get(DataKeys.POKEMON_SCALE_MODIFIER).asFloat
        val abilityJSON = json.get(DataKeys.POKEMON_ABILITY)?.asJsonObject ?: JsonObject()
        val abilityName = abilityJSON.get(DataKeys.POKEMON_ABILITY_NAME)?.asString
        if (abilityName != "dummy" && abilityName != null) {
            ability = Abilities.getOrException(abilityName).create(abilityJSON)
        }
        shiny = json.get(DataKeys.POKEMON_SHINY).asBoolean
        if (json.has(DataKeys.POKEMON_STATE)) {
            val stateJson = json.get(DataKeys.POKEMON_STATE).asJsonObject
            val type = stateJson.get(DataKeys.POKEMON_STATE_TYPE)?.asString
            val clazz = type?.let { PokemonState.states[it] }
            state = clazz?.getDeclaredConstructor()?.newInstance()?.readFromJSON(stateJson) ?: InactivePokemonState()
        }
        if (json.has(DataKeys.POKEMON_STATUS)) {
            val statusJson = json.get(DataKeys.POKEMON_STATUS).asJsonObject
            status = PersistentStatusContainer.loadFromJSON(statusJson)
        }
        val ballName = json.get(DataKeys.POKEMON_CAUGHT_BALL).asString
        caughtBall = PokeBalls.getPokeBall(Identifier(ballName)) ?: PokeBalls.POKE_BALL
        benchedMoves.loadFromJSON(json.get(DataKeys.BENCHED_MOVES)?.asJsonArray ?: JsonArray())
        faintedTimer = json.get(DataKeys.POKEMON_FAINTED_TIMER).asInt
        healTimer = json.get(DataKeys.POKEMON_HEALING_TIMER).asInt
        val propertyList = json.getAsJsonArray(DataKeys.POKEMON_DATA)?.map { it.asString } ?: emptyList()
        val properties = PokemonProperties.parse(propertyList.joinToString(" "), " ")
        this.customProperties.clear()
        this.customProperties.addAll(properties.customProperties)
        SpeciesFeatures.getFeaturesFor(species).forEach {
            val feature = it(json) ?: return@forEach
            features.removeIf { it.name == feature.name }
            features.add(feature)
        }
        this.nature = json.get(DataKeys.POKEMON_NATURE).asString?.let { Natures.getNature(Identifier(it))!! } ?: Natures.getRandomNature()
        updateAspects()
        checkAbility()
        json.get(DataKeys.POKEMON_EVOLUTIONS)?.let { this.evolutionProxy.loadFromJson(it) }
        if (json.has(DataKeys.HELD_ITEM)) {
            ItemStack.CODEC.decode(JsonOps.INSTANCE, json.get(DataKeys.HELD_ITEM)).result().ifPresent {
                this.heldItem = it.first
            }
        }
        return this
    }

    fun clone(useJSON: Boolean = true, newUUID: Boolean = true): Pokemon {
        val pokemon = if (useJSON) {
            Pokemon().loadFromJSON(saveToJSON(JsonObject()).also { it.remove(DataKeys.POKEMON_EVOLUTIONS) })
        } else {
            Pokemon().loadFromNBT(saveToNBT(NbtCompound()).also { it.remove(DataKeys.POKEMON_EVOLUTIONS) })
        }
        if (newUUID) {
            pokemon.uuid = UUID.randomUUID()
        }
        return pokemon
    }

    fun getOwnerPlayer() : ServerPlayerEntity? {
        storeCoordinates.get().let {
            if (isPlayerOwned()) {
                return getServer()?.playerManager?.getPlayer(it!!.store.uuid)
            }
        }
        return null
    }

    fun getOwnerUUID() : UUID? {
        storeCoordinates.get().let {
            if (isPlayerOwned()) return it!!.store.uuid
        }
        return null
    }

    fun belongsTo(player: PlayerEntity) = storeCoordinates.get()?.let { it.store.uuid == player.uuid } == true
    fun isPlayerOwned() = storeCoordinates.get()?.let { it.store is PlayerPartyStore /* || it.store is PCStore */ } == true
    fun isWild() = storeCoordinates.get() == null

    /**
     * Set the [friendship] to the given value.
     * This has some restrictions on mutation, the value must never be outside the bounds of 0 to [CobblemonConfig.maxPokemonFriendship].
     * See this [page](https://bulbapedia.bulbagarden.net/wiki/Friendship) for more information.
     *
     * @param value The value to set, this value will forcefully be absolute.
     * @param coerceSafe Forcefully coerce the maximum possible value. Default is true.
     * @return True if mutation was successful
     */
    fun setFriendship(value: Int, coerceSafe: Boolean = true): Boolean {
        val sanitizedAmount = if (coerceSafe) value.absoluteValue.coerceAtMost(Cobblemon.config.maxPokemonFriendship) else value.absoluteValue
        if (!this.isClient && !this.isPossibleFriendship(sanitizedAmount)) {
            return false
        }
        this.friendship = sanitizedAmount
        return true
    }

    /**
     * Increment the [friendship] with by the given amount.
     * This has some restrictions on mutation, the value must never be outside the bounds of 0 to [CobblemonConfig.maxPokemonFriendship].
     * See this [page](https://bulbapedia.bulbagarden.net/wiki/Friendship) for more information.
     *
     * @param amount The amount to increment, this value will forcefully be absolute.
     * @param coerceSafe Forcefully coerce the maximum possible value. Default is true.
     * @return True if mutation was successful
     */
    fun incrementFriendship(amount : Int, coerceSafe: Boolean = true): Boolean {
        val sanitizedAmount = if (coerceSafe) amount.absoluteValue.coerceAtMost(Cobblemon.config.maxPokemonFriendship - this.friendship) else amount.absoluteValue
        val newValue = this.friendship + sanitizedAmount
        if (this.isPossibleFriendship(newValue)) {
            this.friendship = newValue
        }
        return this.friendship == newValue
    }

    /**
     * Decrement the [friendship] with by the given amount.
     * This has some restrictions on mutation, the value must never be outside the bounds of 0 to [CobblemonConfig.maxPokemonFriendship].
     * See this [page](https://bulbapedia.bulbagarden.net/wiki/Friendship) for more information.
     *
     * @param amount The amount to decrement, this value will forcefully be absolute.
     * @param coerceSafe Forcefully coerce the maximum possible value. Default is true.
     * @return True if mutation was successful
     */
    fun decrementFriendship(amount : Int, coerceSafe: Boolean = true): Boolean {
        val sanitizedAmount = if (coerceSafe) amount.absoluteValue.coerceAtMost(this.friendship) else amount.absoluteValue
        val newValue = this.friendship - sanitizedAmount
        if (this.isPossibleFriendship(newValue)) {
            this.friendship = newValue
        }
        return this.friendship == newValue
    }

    /**
     * Checks if the given value is withing the legal bounds for friendship.
     *
     * @param value The value being queried
     * @return If the value is within legal bounds.
     */
    fun isPossibleFriendship(value: Int) = value >= 0 && value <= Cobblemon.config.maxPokemonFriendship

    val allAccessibleMoves: Set<MoveTemplate>
        get() = form.moves.getLevelUpMovesUpTo(level) + benchedMoves.map { it.moveTemplate } + form.moves.evolutionMoves

    fun updateAspects() {
        /*
         * We don't want to run this for client representations of Pokémon as they won't always have the same
         * aspect providers, and we want the server side to entirely manage them anyway.
         */
        if (!isClient) {
            aspects = AspectProvider.providers.flatMap { it.provide(this) }.toSet()
        }
    }

    fun updateForm() {
        val newForm = species.getForm(aspects)
        if (form != newForm) {
            // Form updated!
            form = newForm
        }
    }

    fun initialize(): Pokemon {
        species = species
        checkGender()
        checkAbility()
        initializeMoveset()
        return this
    }

    fun checkGender() {
        var reassess = false
        if (form.maleRatio !in 0F..1F && gender != Gender.GENDERLESS) {
            reassess = true
        } else if (form.maleRatio == 0F && gender != Gender.FEMALE) {
            reassess = true
        } else if (form.maleRatio == 1F && gender != Gender.MALE) {
            reassess = true
        } else if (form.maleRatio in 0F..1F && gender == Gender.GENDERLESS) {
            reassess = true
        }

        if (reassess) {
            gender = if (form.maleRatio !in 0F..1F) {
                Gender.GENDERLESS
            } else if (form.maleRatio == 1F || Random.nextFloat() <= form.maleRatio) {
                Gender.MALE
            } else {
                Gender.FEMALE
            }
        }
    }

    fun checkAbility() {
        if (isClient) {
            return
        }
        val hasForcedAbility = this.ability.forced
        val hasLegalAbility = this.form.abilities.mapping.values.any { list ->
            list.any { potential ->
                potential.template == this.ability.template
            }
        }
        val isDummy = this.ability.template == Abilities.DUMMY

        // EXPLANATION
        // This is used to keep the same intended ability between evolution stages
        // Between species updates if an original indexed data is attached it will be honored next time that it's possible
        // This is still not a perfect system but it will now only break if players are constantly adding/removing data edits which at that point it's on them
        if (isDummy || (!hasLegalAbility && !hasForcedAbility)) {
            var needsSelection = true
            var needsUpdate = true
            if (this.ability.index == -1 && !isDummy) {
                base@ for ((priority, list) in this.form.abilities.mapping) {
                    for ((index, potential) in list.withIndex()) {
                        if (potential.template == this.ability.template) {
                            this.ability.priority = priority
                            this.ability.index = index
                            needsUpdate = false
                            needsSelection = false
                            break@base
                        }
                    }
                }
            }
            else if (this.ability.index >= 0) {
                needsUpdate = false
                val potentialAbility = this.form.abilities.mapping[this.ability.priority]?.getOrNull(this.ability.index)
                if (potentialAbility != null) {
                    // Don't update index nor priority
                    val newAbility = potentialAbility.template.create()
                    newAbility.index = this.ability.index
                    newAbility.priority = this.ability.priority
                    this.ability = newAbility
                    needsSelection = false
                }
            }
            if (needsSelection) {
                val (ability, priority) = this.form.abilities.select(this.species, this.aspects)
                ability.index = this.ability.index
                ability.priority = this.ability.priority
                this.ability = ability
                if (needsUpdate) {
                    // This may sometimes happen when both species and form update as well as if AbilityPool#select throws a graceful exception, we return to prevent a crash.
                    val mapped = this.form.abilities.mapping[priority] ?: return
                    this.ability.index = mapped.indexOfFirst { potential -> potential.template == this.ability.template }
                    this.ability.priority = priority
                }
            }
        }
    }

    fun initializeMoveset(preferLatest: Boolean = true) {
        val possibleMoves = form.moves.getLevelUpMovesUpTo(level).toMutableList()
        moveSet.doWithoutEmitting {
            moveSet.clear()
            if (possibleMoves.isEmpty()) {
                moveSet.add(Moves.getExceptional().create())
                return@doWithoutEmitting
            }

            val selector: () -> MoveTemplate? = {
                if (preferLatest) {
                    possibleMoves.removeLastOrNull()
                } else {
                    val random = possibleMoves.randomOrNull()
                    if (random != null) {
                        possibleMoves.remove(random)
                    }
                    random
                }
            }

            for (i in 0 until 4) {
                val move = selector() ?: break
                moveSet.setMove(i, move.create())
            }
        }
        moveSet.update()
    }

    fun getExperienceToNextLevel() = getExperienceToLevel(level + 1)
    fun getExperienceToLevel(level: Int) = if (level <= this.level) 0 else experienceGroup.getExperience(level) - experience

    fun setExperienceAndUpdateLevel(xp: Int) {
        experience = xp
        val newLevel = experienceGroup.getLevel(xp)
        if (newLevel != level && newLevel <= Cobblemon.config.maxPokemonLevel) {
            level = newLevel
        }
    }

    fun addExperienceWithPlayer(player: ServerPlayerEntity, source: ExperienceSource, xp: Int): AddExperienceResult {
        val result = addExperience(source, xp)
        if (result.experienceAdded <= 0) {
            return result
        }
        player.sendMessage(lang("experience.gained", species.translatedName, xp))
        if (result.oldLevel != result.newLevel) {
            player.sendMessage(lang("experience.level_up", species.translatedName, result.newLevel))
            val repeats = result.newLevel - result.oldLevel
            // Someone can technically trigger a "delevel"
            if (repeats >= 1) {
                repeat(repeats) {
                    this.incrementFriendship(LEVEL_UP_FRIENDSHIP_CALCULATOR.calculate(this))
                }
            }
            result.newMoves.forEach {
                player.sendMessage(lang("experience.learned_move", species.translatedName, it.displayName))
            }
        }
        return result
    }

    fun <T : SpeciesFeature> getFeature(name: String) = features.find { it.name == name } as? T

    /**
     * Copies the specified properties from this Pokémon into a new [PokemonProperties] instance.
     *
     * You can find a bunch of built-in extractors inside [PokemonPropertyExtractor] statically.
     */
    fun createPokemonProperties(vararg extractors: PokemonPropertyExtractor): PokemonProperties {
        val properties = PokemonProperties()
        extractors.forEach { it(this, properties) }
        return properties
    }

    fun addExperience(source: ExperienceSource, xp: Int): AddExperienceResult {
        if (xp < 0 || !this.canLevelUpFurther()) {
            return AddExperienceResult(level, level, emptySet(), 0) // no negatives!
        }
        val oldLevel = level
        val previousLevelUpMoves = form.moves.getLevelUpMovesUpTo(oldLevel)
        var appliedXP = xp
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.postThen(
            event = ExperienceGainedPreEvent(this, source, appliedXP),
            ifSucceeded = { appliedXP = it.experience},
            ifCanceled = {
                return AddExperienceResult(level, level, emptySet(), 0)
            }
        )

        experience += appliedXP
        // Bound is needed here as we can still be allowed to level up but go over the current cap
        var newLevel = experienceGroup.getLevel(experience).coerceAtMost(Cobblemon.config.maxPokemonLevel)
        if (newLevel != oldLevel) {
            CobblemonEvents.LEVEL_UP_EVENT.post(
                LevelUpEvent(this, oldLevel, newLevel),
                then = { newLevel = it.newLevel }
            )
            level = newLevel
        }

        val newLevelUpMoves = form.moves.getLevelUpMovesUpTo(newLevel)
        val differences = (newLevelUpMoves - previousLevelUpMoves).filter { this.moveSet.none { move -> move.template == it } }.toMutableSet()
        differences.forEach {
            if (moveSet.hasSpace()) {
                moveSet.add(it.create())
            }
        }

        CobblemonEvents.EXPERIENCE_GAINED_EVENT_POST.post(
            ExperienceGainedPostEvent(this, source, appliedXP, oldLevel, newLevel, differences),
            then = { return AddExperienceResult(oldLevel, newLevel, it.learnedMoves, appliedXP) }
        )

        // This probably will never run, Kotlin just doesn't realize the inline function always runs the `then` block
        return AddExperienceResult(oldLevel, newLevel, differences, appliedXP)
    }

    fun canLevelUpFurther() = this.level < Cobblemon.config.maxPokemonLevel

    fun levelUp(source: ExperienceSource) = addExperience(source, getExperienceToNextLevel())

    /**
     * Exchanges an existing move set move with a benched or otherwise accessible move that is not in the move set.
     *
     * PP is transferred onto the new move using the % of PP that the original move had and applying it to the new one.
     *
     * @return true if it succeeded, false if it failed to exchange the moves. Failure can occur if the oldMove is not
     * a move set move.
     */
    fun exchangeMove(oldMove: MoveTemplate, newMove: MoveTemplate): Boolean {
        val benchedNewMove = benchedMoves.find { it.moveTemplate == newMove } ?: BenchedMove(newMove, 0)

        if (moveSet.hasSpace()) {
            benchedMoves.remove(newMove)
            val move = newMove.create()
            move.raisedPpStages = benchedNewMove.ppRaisedStages
            move.currentPp = move.maxPp
            moveSet.add(move)
            return true
        }

        val currentMove = moveSet.find { it.template == oldMove } ?: return false
        val currentPPRatio = currentMove.let { it.currentPp / it.maxPp.toFloat() }
        benchedMoves.doThenEmit {
            benchedMoves.remove(newMove)
            benchedMoves.add(BenchedMove(currentMove.template, currentMove.raisedPpStages))
        }

        val move = newMove.create()
        move.raisedPpStages = benchedNewMove.ppRaisedStages
        move.currentPp = (currentPPRatio * move.maxPp).toInt()
        moveSet.setMove(moveSet.indexOf(currentMove), move)

        return true
    }

    fun notify(packet: PokemonUpdatePacket) {
        storeCoordinates.get()?.run { sendToPlayers(store.getObservingPlayers(), packet) }
    }

    fun <T> registerObservable(observable: SimpleObservable<T>, notifyPacket: ((T) -> PokemonUpdatePacket)? = null): SimpleObservable<T> {
        observables.add(observable)
        observable.subscribe {
            if (notifyPacket != null && storeCoordinates.get() != null) {
                notify(notifyPacket(it))
            }
            anyChangeObservable.emit(this)
        }
        return observable
    }

    private val observables = mutableListOf<Observable<*>>()
    private val anyChangeObservable = SimpleObservable<Pokemon>()

    fun markFeatureDirty(feature: SpeciesFeature) {
        _features.emit(feature)
    }

    fun getAllObservables() = observables.asIterable()
    /** Returns an [Observable] that emits Unit whenever any change is made to this Pokémon. The change itself is not included. */
    fun getChangeObservable(): Observable<Pokemon> = anyChangeObservable

    private fun findAndLearnFormChangeMoves() {
        this.form.moves.formChangeMoves.forEach { move ->
            if (this.benchedMoves.none { it.moveTemplate == move }) {
                this.benchedMoves.add(BenchedMove(move, 0))
            }
        }
    }

    private fun sanitizeFormChangeMoves(old: FormData) {
        for (i in 0 until MoveSet.MOVE_COUNT) {
            val move = this.moveSet[i]
            if (move != null && LearnsetQuery.FORM_CHANGE.canLearn(move.template, old.moves) && !LearnsetQuery.ANY.canLearn(move.template, this.form.moves)) {
                this.moveSet.setMove(i, null)
            }
        }
        val benchedIterator = this.benchedMoves.iterator()
        while (benchedIterator.hasNext()) {
            val benchedMove = benchedIterator.next()
            if (LearnsetQuery.FORM_CHANGE.canLearn(benchedMove.moveTemplate, old.moves) && !LearnsetQuery.ANY.canLearn(benchedMove.moveTemplate, this.form.moves)) {
                benchedIterator.remove()
            }
        }
        if (this.moveSet.filterNotNull().isEmpty()) {
            val benchedMove = this.benchedMoves.firstOrNull()
            if (benchedMove != null) {
                this.moveSet.setMove(0, Move(benchedMove.moveTemplate, benchedMove.ppRaisedStages))
                return
            }
            this.initializeMoveset()
        }
    }

    private val _form = SimpleObservable<FormData>()
    private val _species = registerObservable(SimpleObservable<Species>()) { SpeciesUpdatePacket(this, it) }
    private val _experience = registerObservable(SimpleObservable<Int>()) { ExperienceUpdatePacket(this, it) }
    private val _friendship = registerObservable(SimpleObservable<Int>()) { FriendshipUpdatePacket(this, it) }
    private val _currentHealth = registerObservable(SimpleObservable<Int>()) { HealthUpdatePacket(this, it) }
    private val _shiny = registerObservable(SimpleObservable<Boolean>()) { ShinyUpdatePacket(this, it) }
    private val _nature = registerObservable(SimpleObservable<String>()) { NatureUpdatePacket(this, it, false) }
    private val _mintedNature = registerObservable(SimpleObservable<String>()) { NatureUpdatePacket(this, it, true) }
    private val _moveSet = registerObservable(moveSet.observable) { MoveSetUpdatePacket(this, moveSet) }
    private val _state = registerObservable(SimpleObservable<PokemonState>()) { PokemonStateUpdatePacket(this, it) }
    private val _status = registerObservable(SimpleObservable<String>()) { StatusUpdatePacket(this, it) }
    private val _caughtBall = registerObservable(SimpleObservable<String>()) { CaughtBallUpdatePacket(this, it) }
    private val _benchedMoves = registerObservable(benchedMoves.observable) { BenchedMovesUpdatePacket(this, it) }
    private val _ivs = registerObservable(ivs.observable) { IVsUpdatePacket(this, it as IVs) }
    private val _evs = registerObservable(evs.observable) { EVsUpdatePacket(this, it as EVs) }
    private val _aspects = registerObservable(SimpleObservable<Set<String>>()) { AspectsUpdatePacket(this, it) }
    private val _gender = registerObservable(SimpleObservable<Gender>()) { GenderUpdatePacket(this, it) }
    private val _ability = registerObservable(SimpleObservable<Ability>()) { AbilityUpdatePacket(this, it.template) }
    private val _heldItem = registerObservable(SimpleObservable<ItemStack>()) { HeldItemUpdatePacket(this, it) }

    private val _features = registerObservable(SimpleObservable<SpeciesFeature>())

    companion object {

        /**
         * The [FriendshipMutationCalculator] used when a Pokémon levels up.
         */
        var LEVEL_UP_FRIENDSHIP_CALCULATOR = FriendshipMutationCalculator.SWORD_AND_SHIELD_LEVEL_UP
        internal val SHEDINJA = cobblemonResource("shedinja")

    }

}
