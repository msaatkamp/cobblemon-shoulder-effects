package com.cablemc.pokemoncobbled.common.api.spawning

import com.cablemc.pokemoncobbled.common.PokemonCobbled
import com.cablemc.pokemoncobbled.common.PokemonCobbled.LOGGER
import com.cablemc.pokemoncobbled.common.PokemonCobbled.config
import com.cablemc.pokemoncobbled.common.api.pokemon.PokemonProperties
import com.cablemc.pokemoncobbled.common.api.spawning.condition.SpawningCondition
import com.cablemc.pokemoncobbled.common.api.spawning.condition.TimeRange
import com.cablemc.pokemoncobbled.common.api.spawning.context.RegisteredSpawningContext
import com.cablemc.pokemoncobbled.common.api.spawning.detail.RegisteredSpawnDetail
import com.cablemc.pokemoncobbled.common.api.spawning.detail.SpawnDetail
import com.cablemc.pokemoncobbled.common.api.spawning.detail.SpawnPool
import com.cablemc.pokemoncobbled.common.util.AssetLoading
import com.cablemc.pokemoncobbled.common.util.AssetLoading.toPath
import com.cablemc.pokemoncobbled.common.util.adapters.*
import com.cablemc.pokemoncobbled.common.util.cobbledResource
import com.cablemc.pokemoncobbled.common.util.fromJson
import com.cablemc.pokemoncobbled.common.util.isHigherVersion
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import net.minecraft.tag.TagKey
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.pathString
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.biome.Biome

/**
 * Object responsible for actually deserializing spawns. You should probably
 * rely on this object for it as it would make your code better future proofed.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
object SpawnLoader {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .setLenient()
        .registerTypeAdapter(BiomeLikeCondition::class.java, BiomeLikeConditionAdapter)
        .registerTypeAdapter(RegisteredSpawningContext::class.java, RegisteredSpawningContextAdapter)
        .registerTypeAdapter(Identifier::class.java, IdentifierAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(TagKey::class.java, Biome::class.java).type, TagKeyAdapter<Biome>(Registry.BIOME_KEY))
        .registerTypeAdapter(SpawnDetail::class.java, SpawnDetailAdapter)
        .registerTypeAdapter(SpawningCondition::class.java, SpawningConditionAdapter)
        .registerTypeAdapter(TimeRange::class.java, TimeRangeAdapter)
        .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
        .registerTypeAdapter(SpawnBucket::class.java, SpawnBucketAdapter)
        .create()

    var deserializingRegisteredSpawnDetail: RegisteredSpawnDetail<*>? = null
    var deserializingConditionClass: Class<out SpawningCondition<*>>? = null

    fun load(folder: String): SpawnPool {
        val pool = SpawnPool()

        val external = getExternalSets(folder)
        val internal = loadInternalFolder(folder)

        external.forEach { set ->
            val matchingInternal = internal.find { it.id == set.id }
            if (set.preventOverwrite) {
                if (matchingInternal != null) {
                    internal.remove(matchingInternal)
                }
                if (set.isEnabled()) {
                    pool.details.addAll(set)
                }
            } else {
                if (matchingInternal != null && matchingInternal.version.isHigherVersion(set.version)) {
                    if (matchingInternal.isEnabled()) {
                        pool.details.addAll(matchingInternal)
                    }
                    internal.remove(matchingInternal)
                    exportSet(folder, matchingInternal)
                }
            }
        }

        internal.forEach { set ->
            if (set.isEnabled()) {
                pool.details.addAll(set)
            }
            if (config.exportSpawnsToConfig) {
                exportSet(folder, set)
            }
        }

        pool.precalculate()

        return pool
    }

    fun exportSet(folder: String, set: SpawnSet) {
        val file = File("./config/pokemoncobbled/spawns/$folder/${set.id.lowercase()}.set.json")
        file.createNewFile()
        val writer = PrintWriter(file)
        val json = gson.toJson(set)
        writer.print(json)
        writer.flush()
        writer.close()
    }

    fun getExternalSets(folder: String): MutableList<SpawnSet> {
        val sets = mutableListOf<SpawnSet>()
        val files = mutableListOf<File>()
        AssetLoading.searchFor(
            dir = "config/spawns/$folder",
            suffix = ".json",
            list = files
        )
        val paths = files.map { it.toPath() }

        sets.addAll(
            paths.mapNotNull {
                try {
                    loadSetFromPath(it)
                } catch (exception : Exception) {
                    LOGGER.error("Unable to load spawn set from ${it.pathString}")
                    exception.printStackTrace()
                    null
                }
            }
        )

        return sets
    }

    fun loadSetFromPath(path: Path): SpawnSet {
        val inputStream = PokemonCobbled::class.java.getResourceAsStream(path.toAbsolutePath().toString())
            ?: throw IllegalArgumentException("Unable to open resource stream for: ${path.pathString}")
        return gson.fromJson<SpawnSet>(InputStreamReader(inputStream))
    }

    fun loadInternalFolder(folder: String): MutableList<SpawnSet> {
        val internalFolder = cobbledResource("spawns/$folder")
        val path = internalFolder.toPath() ?: throw IllegalArgumentException("There is no valid, internal path for $internalFolder")
        val internalPaths = AssetLoading.fileSearch(path, { it.toString().endsWith(".json") }, recursive = true)
        val sets = mutableListOf<SpawnSet>()
        sets.addAll(internalPaths.flatMap { loadInternalFile(it) })
        return sets
    }

    fun loadInternalFile(path: Path): MutableList<SpawnSet> {
        if (path.toString().endsWith(".packed.json")) {
            return loadPackedSets(path)
        } else if (path.toString().endsWith(".set.json")) {
            return mutableListOf(loadSetFromPath(path))
        }

        return mutableListOf()
    }

    fun loadPackedSets(path: Path): MutableList<SpawnSet> {
        val fileStream = PokemonCobbled::class.java.getResourceAsStream(path.toAbsolutePath().toString())
            ?: throw IllegalArgumentException("Unable to create input stream for $path")

        return try {
            loadPackedSetsFromStream(fileStream)
        } catch (e: Exception) {
            LOGGER.error(e.message)
            return mutableListOf()
        }
    }


    fun loadPackedSetsFromStream(inputStream: InputStream): MutableList<SpawnSet> {
        val sets = mutableListOf<SpawnSet>()
        val jsonArray = gson.fromJson(InputStreamReader(inputStream), JsonArray::class.java)
        jsonArray.forEach { jsonElement ->
            try {
                sets.add(gson.fromJson<SpawnSet>(jsonElement))
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
        return sets
    }
}