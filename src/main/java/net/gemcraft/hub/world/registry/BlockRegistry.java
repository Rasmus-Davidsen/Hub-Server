package net.gemcraft.hub.world.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gemcraft.hub.Hub;
import net.gemcraft.hub.utils.ConversionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class BlockRegistry {
    private static final String PATH = "/generated/blocks.json";

    private final Map<@NotNull String, @NotNull Integer> ids = new HashMap<>();
    private final Map<@NotNull Integer, @NotNull Integer> emissions = new HashMap<>();
    private final Set<@NotNull Integer> transparent = new HashSet<>();

    public static @NotNull BlockRegistry load() throws IOException {
        final BlockRegistry registry = new BlockRegistry();
        final JsonObject blocks = readRegistry();

        for(final Map.Entry<String, JsonElement> block : blocks.entrySet()) {
            final JsonObject dataObj = block.getValue().getAsJsonObject();

            for(final JsonElement stateElement : dataObj.getAsJsonArray("states")) {
                final JsonObject stateObj = stateElement.getAsJsonObject();
                final int id = stateObj.get("id").getAsInt();

                final Map<String, String> properties = getProperties(stateObj);
                final String typeKey = block.getKey();

                final String formattedProperties = formatProperties(properties);
                registry.ids.put(typeKey + formattedProperties, id);

                // Emission
                final Integer emission = emissionOf(
                        typeKey, properties
                );

                if(emission != null && emission > 0) {
                    registry.emissions.put(id, emission);
                }

                // Transparent
                if(isTransparent(typeKey)) {
                    registry.transparent.add(id);
                }
            }
        }

        return registry;
    }

    private static @NotNull Map<@NotNull String, @NotNull String> getProperties(final @NotNull JsonObject state) {
        final Map<String, String> properties = new TreeMap<>();
        final JsonObject propertyObj = state.getAsJsonObject("properties");

        if(propertyObj != null) {
            for(final Map.Entry<String, JsonElement> property : propertyObj.entrySet()) {
                final String value = property.getValue().getAsString();
                properties.put(property.getKey(), value);
            }
        }

        return properties;
    }

    private static @NotNull JsonObject readRegistry() throws IOException{
        try(final InputStream in = Hub.class.getResourceAsStream(PATH)) {
            if(in == null) {
                throw new FileNotFoundException(
                        "Block registry missing in resources at " + PATH
                );
            }

            final InputStreamReader reader = new InputStreamReader(
                    in, StandardCharsets.UTF_8
            );

            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static @NotNull String formatProperties(final @NotNull Map<@NotNull String, @NotNull String> properties) {
        if(properties.isEmpty()) {
            return "";
        }

        return properties.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(",", "[", "]"));
    }

    public int get(final @NotNull String key) {
        return ids.getOrDefault(key, 0);
    }

    public int getEmission(final int id) {
        return emissions.getOrDefault(id, 0);
    }

    public boolean isOpaque(final int id) {
        return id != 0 && !transparent.contains(id);
    }

    private static @Nullable Integer emissionOf(@NotNull String key, final @NotNull Map<@NotNull String, @NotNull String> properties) {
        key = key.replace(
                "minecraft:", ""
        );

        if(key.endsWith("_candle") || key.equals("candle")) {
            return getCandleEmission(properties);
        }

        if(key.endsWith("_copper_lantern") || key.equals("copper_lantern")) {
            return 15;
        }

        if(key.endsWith("_froglight")) {
            return 15;
        }

        return switch(key) {
            case "glowstone", "sea_lantern", "shroomlight", "jack_o_lantern", "lantern", "beacon",
                 "fire", "end_portal", "lava", "lava_cauldron", "conduit", "end_gateway" -> 15;
            case "redstone_lamp", "campfire", "copper_bulb", "waxed_copper_bulb" -> isLit(properties) ? 15 : null;
            case "torch", "wall_torch", "copper_torch", "copper_wall_torch", "end_rod" -> 14;
            case "furnace", "smoker", "blast_furnace" -> isLit(properties) ? 13 : null;
            case "exposed_copper_bulb", "waxed_exposed_copper_bulb" -> isLit(properties) ? 12 : null;
            case "nether_portal" -> 11;
            case "soul_torch", "soul_wall_torch", "soul_lantern", "crying_obsidian", "soul_fire" -> 10;
            case "soul_campfire" -> isLit(properties) ? 10 : null;
            case "redstone_ore", "deepslate_redstone_ore" -> isLit(properties) ? 9 : null;
            case "weathered_copper_bulb", "waxed_weathered_copper_bulb" -> isLit(properties) ? 8 : null;
            case "enchanting_table", "ender_chest", "glow_lichen" -> 7;
            case "redstone_torch", "redstone_wall_torch" -> isLit(properties) ? 7 : null;
            case "sculk_catalyst" -> 6;
            case "amethyst_cluster" -> 5;
            case "large_amethyst_bud" -> 4;
            case "oxidized_copper_bulb", "waxed_oxidized_copper_bulb" -> isLit(properties) ? 4 : null;
            case "magma_block" -> 3;
            case "medium_amethyst_bud" -> 2;
            case "small_amethyst_bud", "brewing_stand", "brown_mushroom", "calibrated_sculk_sensor",
                 "dragon_egg", "end_portal_frame", "sculk_sensor" -> 1;
            case "sea_pickle" -> getSeaPickleEmission(properties);
            case "respawn_anchor" -> getRespawnAnchorEmission(properties);
            case "trial_spawner" -> getTrialSpawnerEmission(properties);
            case "vault" -> getVaultEmission(properties);
            case "cave_vines", "cave_vines_plant" -> getCaveVinesEmission(properties);
            case "light" -> getLightEmission(properties);
            default -> null;
        };

    }

    private static boolean isLit(final @NotNull Map<@NotNull String, @NotNull String> properties) {
        final String value = properties.get("lit");

        if(value == null) {
            return false;
        }

        return value.equals("true");
    }

    private static @Nullable Integer getCandleEmission(final @NotNull Map<@NotNull String, @NotNull String> properties) {
        if(!isLit(properties)) {
            return null;
        }

        final String candles = properties.get("candles");

        if(candles == null) {
            return null;
        }

        return switch(candles) {
            case "1" -> 3;
            case "2" -> 6;
            case "3" -> 9;
            case "4" -> 12;
            default -> null;
        };
    }

    private static @Nullable Integer getSeaPickleEmission(final @NotNull Map<@NotNull String, @NotNull String> properties) {
        final String waterlogged = properties.get("waterlogged");

        if(waterlogged == null || !waterlogged.equals("true")) {
            return null;
        }

        final String pickles =  properties.get("pickles");

        if(pickles == null) {
            return null;
        }

        return switch(pickles) {
            case "1" -> 6;
            case "2" -> 9;
            case "3" -> 12;
            case "4" -> 15;
            default -> null;
        };
    }

    private static @Nullable Integer getRespawnAnchorEmission(final @NotNull Map<@NotNull String, @NotNull String> properties) {
        final String charges = properties.get("charges");

        if(charges == null) {
            return null;
        }

        return switch(charges) {
            case "1" -> 3;
            case "2" -> 7;
            case "3" -> 11;
            case "4" -> 15;
            default -> null;
        };
    }

    private static @Nullable Integer getTrialSpawnerEmission(final @NotNull Map<@NotNull String, @NotNull String> properties) {
        final String state = properties.get(
                "trial_spawner_state"
        );

        return switch(state) {
            case "waiting_for_players" -> 4;
            case "active" -> 8;
            default -> null;
        };
    }

    private static @Nullable Integer getVaultEmission(final @NotNull Map<@NotNull String, @NotNull String> properties) {
        final String state = properties.get(
                "vault_state"
        );

        return switch(state) {
            case "inactive" -> 6;
            case "active", "ejecting" -> 12;
            default -> null;
        };
    }

    private static @Nullable Integer getCaveVinesEmission(final @NotNull Map<@NotNull String, @NotNull String> properties) {
        final String berries = properties.get("berries");

        if(berries == null) {
            return null;
        }

        return berries.equals("true") ? 14 : 0;
    }

    private static @Nullable Integer getLightEmission(final @NotNull Map<@NotNull String, @NotNull String> properties) {
        final String level = properties.get("level");

        if(level == null) {
            return null;
        }

        return ConversionUtils.getInteger(level);
    }

    private static boolean isTransparent(@NotNull String key) {
        key = key.replace("minecraft:", "");

        if(key.startsWith("potted_")) {
            return true;
        }

        if(key.endsWith("_glass") || key.equals("glass")
                || key.endsWith("_glass_pane") || key.equals("glass_pane")
                || key.endsWith("_lantern") || key.equals("lantern")
                || key.endsWith("_torch") || key.equals("torch")
                || key.endsWith("_anvil") || key.equals("anvil")
                || key.endsWith("_lightning_rod") || key.equals("lightning_rod")
                || key.endsWith("_rail") || key.equals("rail")
                || key.endsWith("_chest") || key.equals("chest")
                || key.endsWith("_copper_golem_statue") || key.equals("copper_golem_statue")
                || key.endsWith("_candle") || key.equals("candle")
                || key.endsWith("_leaves")
                || key.endsWith("_sign")
                || key.endsWith("_trapdoor")
                || key.endsWith("_door")
                || key.endsWith("_bars")
                || key.endsWith("_stairs")
                || key.endsWith("_slab")
                || key.endsWith("_wall")
                || key.endsWith("_fence")
                || key.endsWith("_fence_gate")
                || key.endsWith("_button")
                || key.endsWith("_pressure_plate")
                || key.endsWith("_chain")
                || key.endsWith("_carpet")
                || key.endsWith("_bed")
                || key.endsWith("_banner")
                || key.endsWith("_amethyst_bud")
                || key.endsWith("_sapling")
                || key.endsWith("_tulip")
                || key.endsWith("_coral")
                || key.endsWith("_coral_fan")
                || key.endsWith("_mushroom")
                || key.endsWith("_grass")
                || key.endsWith("_shelf")
                || key.endsWith("_head")
                || key.endsWith("_skull")
                || key.endsWith("_vines")
                || key.endsWith("_vines_plant")
                || key.endsWith("_stem")
                || key.endsWith("_dripleaf"))
        {
            return true;
        }

        return switch(key) {
            case "air", "cave_air", "void_air", "water", "lava", "ladder", "barrier", "campfire", "cauldron",
                 "soul_campfire", "seagrass", "tall_grass", "amethyst_cluster", "glow_lichen", "conduit", "poppy",
                 "stonecutter", "end_rod", "grindstone", "bell", "tripwire", "tripwire_hook", "brewing_stand",
                 "redstone_wire", "comparator", "repeater", "lever", "daylight_detector", "hopper", "heavy_core",
                 "sculk_sensor", "calibrated_sculk_sensor", "sculk_shrieker", "decorated_pot", "light", "wheat",
                 "flower_pot", "lectern", "dragon_egg", "vault", "spawner", "trial_spawner", "structure_void",
                 "pointed_dripstone", "sulfur_spike", "sculk_vein", "cobweb", "fire", "soul_fire", "sugar_cane",
                 "cactus", "cactus_flower", "hanging_roots", "pale_hanging_moss", "azalea", "flowering_azalea",
                 "dead_bush", "bush", "red_shrub", "kelp", "kelp_plant", "ice", "snow", "dried_ghast", "rose_bush",
                 "sunflower", "peony", "pitcher_crop", "pitcher_plant", "vine", "turtle_egg", "sniffer_egg", "beacon",
                 "chorus_flower", "chorus_plant", "wither_rose", "open_eyeblossom", "sweet_berry_bush", "cocoa",
                 "dandelion", "golden_dandelion", "frogspawn", "mangrove_propagule", "firefly_bush", "nether_wart",
                 "nether_sprouts", "nether_portal", "end_portal", "torchflower", "torchflower_crop", "beetroots",
                 "warped_roots", "crimson_roots", "lily_pad", "cornflower", "oxeye_daisy", "azure_bluet", "allium",
                 "blue_orchid", "lily_of_the_valley", "spore_blossom", "pink_petals", "wildflowers", "leaf_litter" -> true;
            default -> false;
        };
    }
}
