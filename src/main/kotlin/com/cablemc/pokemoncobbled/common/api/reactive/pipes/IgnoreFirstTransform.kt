package com.cablemc.pokemoncobbled.common.api.reactive.pipes

import com.cablemc.pokemoncobbled.common.api.reactive.Transform

/**
 * A transform that will ignore some number of initial emissions before it will continue as usual.
 *
 * @author Hiroku
 * @since November 27th, 2021
 */
class IgnoreFirstTransform<T>(var amount: Int = 1) : Transform<T, T> {
    override fun invoke(input: T): T {
        if (amount > 0) {
            amount--
            noTransform(terminate = false)
        } else {
            return input
        }
    }
}