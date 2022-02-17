package com.cablemc.pokemoncobbled.forge.common.api.pokemon.stats

import com.cablemc.pokemoncobbled.forge.common.pokemon.stats.Stat

object Stats {
    private val allStats = mutableListOf<Stat>()

    var HP = registerStat("hp")
    var ATTACK = registerStat("attack")
    var DEFENCE = registerStat("defence")
    var SPECIAL_ATTACK = registerStat("special_attack")
    var SPECIAL_DEFENCE = registerStat("special_defence")
    var SPEED = registerStat("speed")

    val mainStats = listOf(HP, ATTACK, DEFENCE, SPECIAL_ATTACK, SPECIAL_DEFENCE, SPEED)

    fun registerStat(id: String): Stat {
        val stat = object : Stat {
            override val id = id
        }
        allStats.add(stat)
        return stat
    }

    fun getStat(id: String, ignoreCase: Boolean = false): Stat? = allStats.find { it.id.equals(id, ignoreCase) }
}