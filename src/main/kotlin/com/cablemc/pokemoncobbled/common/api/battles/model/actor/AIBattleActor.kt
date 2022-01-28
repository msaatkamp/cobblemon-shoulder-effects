package com.cablemc.pokemoncobbled.common.api.battles.model.actor

import com.cablemc.pokemoncobbled.common.api.battles.model.ai.BattleAI
import com.cablemc.pokemoncobbled.common.api.storage.party.PartyStore
import java.util.*

open class AIBattleActor(
    showdownId: String,
    gameId: UUID,
    party: PartyStore,
    val battleAI: BattleAI
) : BattleActor(showdownId, gameId, party) {

}