package com.cablemc.pokemoncobbled.forge.common.api.storage.adapter

import com.cablemc.pokemoncobbled.forge.common.api.storage.PokemonStore
import com.cablemc.pokemoncobbled.forge.common.api.storage.StorePosition
import java.util.UUID

/**
 * Interface for some type of file backend for [PokemonStore] saving and loading.
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
interface FileStoreAdapter<S> {
    /**
     * Attempts to load a store using the specified class and UUID. This would return null if the file does not exist
     * or if this store adapter doesn't know how to load this storage class.
     */
    fun <E : StorePosition, T : PokemonStore<E>> load(storeClass: Class<T>, uuid: UUID): T?
    /** Converts the specified store into a serialized form. This is expected to run on the server thread, and as fast as possible. */
    fun <E : StorePosition, T : PokemonStore<E>> serialize(store: T): S
    /** Writes the serialized form of a store into the appropriate file. This should be threadsafe. */
    fun save(storeClass: Class<out PokemonStore<*>>, uuid: UUID, serialized: S)
}