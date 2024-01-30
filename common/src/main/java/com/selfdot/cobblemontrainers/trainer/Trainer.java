package com.selfdot.cobblemontrainers.trainer;

import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.selfdot.cobblemontrainers.CobblemonTrainers;
import com.selfdot.cobblemontrainers.util.DataKeys;
import com.selfdot.cobblemontrainers.util.JsonFile;
import kotlin.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Trainer extends JsonFile {

    private String name;
    private String group;
    private List<TrainerPokemon> team;
    private List<String> winCommandList;
    private List<String> lossCommandList;
    private boolean canOnlyBeatOnce;

    public Trainer(CobblemonTrainers mod, String name, String group) {
        super(mod);
        setDefaults();
        this.name = name;
        this.group = group;
    }

    public Trainer(CobblemonTrainers mod, JsonElement jsonElement) {
        super(mod);
        setDefaults();
        loadFromJson(jsonElement);
    }

    public void addSpecies(Species species) {
        Pokemon pokemon = new Pokemon();
        pokemon.setSpecies(species);
        pokemon.initializeMoveset(true);
        pokemon.checkAbility();
        pokemon.setGender(Math.random() > 0.5 ? Gender.FEMALE : Gender.MALE);
        team.add(TrainerPokemon.fromPokemon(pokemon));
    }

    public List<TrainerPokemon> getTeam() {
        return team;
    }

    public List<BattlePokemon> getBattleTeam() {
        return team.stream()
            .map(TrainerPokemon::toPokemon)
            .peek(Pokemon::heal)
            .map(pokemon -> new BattlePokemon(pokemon, pokemon, (pokemonEntity -> Unit.INSTANCE)))
            .collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getWinCommandList() {
        return winCommandList;
    }

    public void setWinCommandList(List<String> winCommandList) {
        this.winCommandList = winCommandList;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<String> getLossCommandList() {
        return lossCommandList;
    }

    public void setLossCommandList(List<String> lossCommandList) {
        this.lossCommandList = lossCommandList;
    }

    public boolean canOnlyBeatOnce() {
        return canOnlyBeatOnce;
    }

    public void setCanOnlyBeatOnce(boolean canOnlyBeatOnce) {
        this.canOnlyBeatOnce = canOnlyBeatOnce;
    }

    @Override
    protected String filename() {
        return "config/trainers/groups/" + group + "/" + name + ".json";
    }

    @Override
    protected void setDefaults() {
        team = new ArrayList<>();
        winCommandList = new ArrayList<>();
        lossCommandList = new ArrayList<>();
        canOnlyBeatOnce = false;
    }

    @Override
    protected void loadFromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.has(DataKeys.TRAINER_NAME)) {
            name = jsonObject.get(DataKeys.TRAINER_NAME).getAsString();
            if (name.isEmpty()) throw new IllegalStateException("Trainer name cannot be empty");
        }
        team = new ArrayList<>();
        jsonObject.getAsJsonArray(DataKeys.TRAINER_TEAM)
            .forEach(pokemonJson -> team.add(new TrainerPokemon(pokemonJson)));
        if (jsonObject.has(DataKeys.TRAINER_WIN_COMMAND_LIST)) {
            jsonObject.getAsJsonArray(DataKeys.TRAINER_WIN_COMMAND_LIST).forEach(
                commandJson -> winCommandList.add(commandJson.getAsString())
            );
        } else if (jsonObject.has(DataKeys.TRAINER_WIN_COMMAND)) {
            String winCommand = jsonObject.get(DataKeys.TRAINER_WIN_COMMAND).getAsString();
            if (!winCommand.isEmpty()) winCommandList = List.of(winCommand);
        } else if (jsonObject.has(DataKeys.TRAINER_MONEY_REWARD)) {
            winCommandList = List.of(
                "eco give %player% " + jsonObject.get(DataKeys.TRAINER_MONEY_REWARD).getAsInt()
            );
        }
        if (jsonObject.has(DataKeys.TRAINER_GROUP)) {
            group = jsonObject.get(DataKeys.TRAINER_GROUP).getAsString();
        }
        if (jsonObject.has(DataKeys.TRAINER_LOSS_COMMAND_LIST)) {
            jsonObject.getAsJsonArray(DataKeys.TRAINER_LOSS_COMMAND_LIST).forEach(
                commandJson -> lossCommandList.add(commandJson.getAsString())
            );
        } else if (jsonObject.has(DataKeys.TRAINER_LOSS_COMMAND)) {
            String lossCommand = jsonObject.get(DataKeys.TRAINER_LOSS_COMMAND).getAsString();
            if (!lossCommand.isEmpty()) lossCommandList = List.of(lossCommand);
        }
        if (jsonObject.has(DataKeys.TRAINER_CAN_ONLY_BEAT_ONCE)) {
            canOnlyBeatOnce = jsonObject.get(DataKeys.TRAINER_CAN_ONLY_BEAT_ONCE).getAsBoolean();
        }
    }

    @Override
    public JsonElement toJson() {
        JsonArray teamArray = new JsonArray(team.size());
        team.forEach(pokemon -> teamArray.add(pokemon.toJson()));
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(DataKeys.TRAINER_TEAM, teamArray);
        JsonArray winCommandArray = new JsonArray();
        JsonArray lossCommandArray = new JsonArray();
        winCommandList.forEach(winCommandArray::add);
        lossCommandList.forEach(lossCommandArray::add);
        jsonObject.add(DataKeys.TRAINER_WIN_COMMAND_LIST, winCommandArray);
        jsonObject.add(DataKeys.TRAINER_LOSS_COMMAND_LIST, lossCommandArray);
        jsonObject.addProperty(DataKeys.TRAINER_CAN_ONLY_BEAT_ONCE, canOnlyBeatOnce);
        return jsonObject;
    }

}
