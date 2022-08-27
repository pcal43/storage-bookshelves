package net.pcal.wallsafe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.pcal.wallsafe.WallSafeRuntimeConfig.Rule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static net.pcal.wallsafe.WallSafeService.LOGGER_NAME;
import static net.pcal.wallsafe.WallSafeService.LOG_PREFIX;

public class WallSafeInitializer implements ModInitializer {

    // ===================================================================================
    // Constants

    private static final Path CUSTOM_CONFIG_PATH = Paths.get("config", "wallsafe.json5");
    private static final Path DEFAULT_CONFIG_PATH = Paths.get("config", "wallsafe-default.json5");
    private static final String CONFIG_RESOURCE_NAME = "wallsafe-default.json5";

    // ===================================================================================
    // ModInitializer implementation

    @Override
    public void onInitialize() {
        try {
            initialize();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    // ===================================================================================
    // Private methods

    private void initialize() throws IOException {
        final Logger logger = LogManager.getLogger(LOGGER_NAME);
        //
        // Load the default configuration from resources and write it as the -default in the installation
        //
        final String defaultConfigResourceRaw;
        try (InputStream in = WallSafeInitializer.class.getClassLoader().getResourceAsStream(CONFIG_RESOURCE_NAME)) {
            if (in == null) {
                throw new FileNotFoundException("Unable to load resource " + CONFIG_RESOURCE_NAME); // wat
            }
            defaultConfigResourceRaw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        DEFAULT_CONFIG_PATH.getParent().toFile().mkdirs();
        Files.writeString(DEFAULT_CONFIG_PATH, defaultConfigResourceRaw);
        //
        // Figure out whether to use custom or default config
        //
        final boolean isCustomConfig;
        final String effectiveConfigRaw;
        if (CUSTOM_CONFIG_PATH.toFile().exists()) {
            logger.info(LOG_PREFIX + "Using custom configuration.");
            effectiveConfigRaw = Files.readString(CUSTOM_CONFIG_PATH);
            isCustomConfig = true;
        } else {
            effectiveConfigRaw = defaultConfigResourceRaw;
            isCustomConfig = false;
        }
        //
        // Apply the config
        //
        final Gson gson = new Gson();
        final GsonModConfig gsonConfig = gson.fromJson(stripComments(effectiveConfigRaw), GsonModConfig.class);
        WallSafeService.getInstance().configure(loadConfig(gsonConfig));
        //
        // All done
        //
        logger.info(LOG_PREFIX + "Initialized" + (isCustomConfig ? " with custom configuration." : "."));
    }

    private static WallSafeRuntimeConfig loadConfig(GsonModConfig config) {
        requireNonNull(config);
        final ImmutableList.Builder<Rule> builder = ImmutableList.builder();
        for (int i=0; i < config.rules.size(); i++) {
            final GsonRuleConfig gsonRule = config.rules.get(i);
            final Rule rule = new Rule(
                    gsonRule.name != null ? gsonRule.name : "rule-"+i,
                    toIdentifierSetOrNull(gsonRule.blockIds),
                    toIdentifierSetOrNull(gsonRule.adjacentBlockIds),
                    toStringSetOrNull(gsonRule.adjacentBlockNames),
                    toDirectionListOrNull(gsonRule.directions)
            );
            builder.add(rule);
        }
        return new WallSafeRuntimeConfig(builder.build());
    }

    private static Set<Identifier> toIdentifierSetOrNull(List<String> rawIds) {
        if (rawIds == null || rawIds.isEmpty()) return null;
        final ImmutableSet.Builder<Identifier> builder = ImmutableSet.builder();
        for (String rawId : rawIds) {
            builder.add(new Identifier(rawId));
        }
        return builder.build();
    }

    private static Set<String> toStringSetOrNull(List<String> rawStrings) {
        if (rawStrings == null || rawStrings.isEmpty()) return null;
        return ImmutableSet.copyOf(rawStrings);
    }

    private static List<Direction> toDirectionListOrNull(List<String> rawIds) {
        if (rawIds == null || rawIds.isEmpty()) return null;
        final ImmutableList.Builder<Direction> builder = ImmutableList.builder();
        rawIds.forEach(d -> builder.add(Direction.byName(d)));
        return builder.build();
    }

    private static String stripComments(String json) throws IOException {
        final StringBuilder out = new StringBuilder();
        final BufferedReader br = new BufferedReader(new StringReader(json));
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.strip().startsWith(("//"))) out.append(line).append('\n');
        }
        return out.toString();
    }

    // ===================================================================================
    // Gson bindings

    public static class GsonModConfig {
        List<GsonRuleConfig> rules;
    }

    public static class GsonRuleConfig {
        String name;
        List<String> blockIds;
        List<String> adjacentBlockIds;
        List<String> adjacentBlockNames;
        List<String> directions;
    }
}