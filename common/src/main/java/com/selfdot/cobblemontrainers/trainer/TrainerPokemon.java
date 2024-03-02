package com.selfdot.cobblemontrainers.trainer;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.*;
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.selfdot.cobblemontrainers.util.DataKeys;
import kotlin.Unit;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TrainerPokemon {

    public static final Set<UUID> IS_TRAINER_OWNED = new HashSet<>();
    public static final Set<UUID> MUST_REENABLE_LOOT_GAMERULE = new HashSet<>();

    private Species species;
    private Gender gender;
    private int level;
    private Nature nature;
    private Ability ability;
    private MoveSet moveset;
    private IVs ivs;
    private EVs evs;
    private boolean isShiny = false;

    private final UUID uuid = UUID.randomUUID();

    public TrainerPokemon() { }

    public TrainerPokemon(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        species = PokemonSpecies.INSTANCE.getByIdentifier(
            new Identifier(jsonObject.get(DataKeys.POKEMON_SPECIES).getAsString())
        );
        gender = Gender.valueOf(jsonObject.get(DataKeys.POKEMON_GENDER).getAsString());
        level = jsonObject.get(DataKeys.POKEMON_LEVEL).getAsInt();
        nature = Natures.INSTANCE.getNature(
            new Identifier(jsonObject.get(DataKeys.POKEMON_NATURE).getAsString())
        );
        ability = new Ability(Abilities.INSTANCE.getOrException(
            jsonObject.get(DataKeys.POKEMON_ABILITY).getAsString()
        ), false);
        ivs = (IVs) new IVs().loadFromJSON(jsonObject.get(DataKeys.POKEMON_IVS).getAsJsonObject());
        evs = (EVs) new EVs().loadFromJSON(jsonObject.get(DataKeys.POKEMON_EVS).getAsJsonObject());
        moveset = new MoveSet();
        JsonArray movesetJson = jsonObject.get(DataKeys.POKEMON_MOVESET).getAsJsonArray();
        for (int i = 0; i < Math.min(4, movesetJson.size()); i++) {
            moveset.setMove(i, Moves.INSTANCE.getByName(movesetJson.get(i).getAsString()).create());
        }
        if (jsonObject.has(DataKeys.POKEMON_SHINY)) isShiny = jsonObject.get(DataKeys.POKEMON_SHINY).getAsBoolean();
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(DataKeys.POKEMON_SPECIES, species.getResourceIdentifier().toString());
        jsonObject.addProperty(DataKeys.POKEMON_GENDER, gender.name());
        jsonObject.addProperty(DataKeys.POKEMON_LEVEL, level);
        jsonObject.addProperty(DataKeys.POKEMON_NATURE, nature.getName().toString());
        jsonObject.addProperty(DataKeys.POKEMON_ABILITY, ability.getName());
        JsonArray movesetJson = new JsonArray();
        moveset.forEach(move -> movesetJson.add(move.getName()));
        jsonObject.add(DataKeys.POKEMON_MOVESET, movesetJson);
        jsonObject.add(DataKeys.POKEMON_IVS, ivs.saveToJSON(new JsonObject()));
        jsonObject.add(DataKeys.POKEMON_EVS, evs.saveToJSON(new JsonObject()));
        jsonObject.addProperty(DataKeys.POKEMON_SHINY, isShiny);
        return jsonObject;
    }

    public Pokemon toPokemon() {
        Pokemon pokemon = new Pokemon();
        pokemon.initializeMoveset(true);
        pokemon.setSpecies(species);
        pokemon.setGender(gender);
        pokemon.setLevel(level);
        pokemon.setNature(nature);
        pokemon.setAbility(ability);
        pokemon.getMoveSet().copyFrom(moveset);
        ivs.spliterator().forEachRemaining(entry -> pokemon.setIV(entry.getKey(), entry.getValue()));
        evs.spliterator().forEachRemaining(entry -> pokemon.setEV(entry.getKey(), entry.getValue()));
        pokemon.setShiny(isShiny);
        pokemon.setUuid(uuid);
        pokemon.getCustomProperties().add(UncatchableProperty.INSTANCE.uncatchable());
        IS_TRAINER_OWNED.add(uuid);
        return pokemon;
    }

    public static TrainerPokemon fromPokemon(Pokemon pokemon) {
        TrainerPokemon trainerPokemon = new TrainerPokemon();
        trainerPokemon.species = pokemon.getSpecies();
        trainerPokemon.gender = pokemon.getGender();
        trainerPokemon.level = pokemon.getLevel();
        trainerPokemon.nature = pokemon.getNature();
        trainerPokemon.ability = pokemon.getAbility();
        trainerPokemon.moveset = pokemon.getMoveSet();
        trainerPokemon.ivs = pokemon.getIvs();
        trainerPokemon.evs = pokemon.getEvs();
        trainerPokemon.isShiny = pokemon.getShiny();
        return trainerPokemon;
    }

    public String getName() {
        return species.getTranslatedName().getString();
    }

    public MoveSet getMoveset() {
        return moveset;
    }

    public IVs getIvs() {
        return ivs;
    }

    public EVs getEvs() {
        return evs;
    }

    public void setAbility(Ability ability) {
        this.ability = ability;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setNature(Nature nature) {
        this.nature = nature;
    }

    public void toggleShiny() {
        isShiny = !isShiny;
    }

    public static void registerPokemonSendOutListener() {
        CobblemonEvents.POKEMON_SENT_POST.subscribe(Priority.NORMAL, event -> {
            if (IS_TRAINER_OWNED.contains(event.getPokemon().getUuid())) {
                event.getPokemonEntity().getUnbattleable().set(true);
            }
            return Unit.INSTANCE;
        });
    }

}
