package net.pcal.barrelshelf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ProxyBlockConfigLoader {

    // ===================================================================================
    // Public methods

    public static List<ProxyBlockRule> loadRulesFromConfig(final String modName) throws IOException {
        final Path customConfigPath = Paths.get("config", modName + ".json5");
        final Path defaultConfigPath = Paths.get("config", modName + "-default.json5");
        final String configResourceName = modName + "-default.json5";

        //
        // Load the default configuration from resources and write it as the -default in the installation
        //
        final String defaultConfigResourceRaw;
        try (InputStream in = ProxyBlockConfigLoader.class.getClassLoader().getResourceAsStream(configResourceName)) {
            if (in == null) {
                throw new FileNotFoundException("Unable to load resource " + configResourceName); // wat
            }
            defaultConfigResourceRaw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        defaultConfigPath.getParent().toFile().mkdirs();
        Files.writeString(defaultConfigPath, defaultConfigResourceRaw);
        //
        // Figure out whether to use custom or default config
        //
        final boolean isCustomConfig;
        final String effectiveConfigRaw;
        if (customConfigPath.toFile().exists()) {
            effectiveConfigRaw = Files.readString(customConfigPath);
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
        final ImmutableList.Builder<ProxyBlockRule> builder = ImmutableList.builder();
        for (int i = 0; i < gsonConfig.rules.size(); i++) {
            final GsonRuleConfig gsonRule = gsonConfig.rules.get(i);
            final ProxyBlockRule rule = new ProxyBlockRule(
                    gsonRule.name != null ? gsonRule.name : "rule-" + i,
                    toIdentifierSetOrNull(gsonRule.blockIds),
                    toIdentifierSetOrNull(gsonRule.adjacentBlockIds),
                    toStringSetOrNull(gsonRule.adjacentBlockNames),
                    toDirectionListOrNull(gsonRule.directions)
            );
            builder.add(rule);
        }
        LogManager.getLogger().info("[" + modName + "] Initialized" + (isCustomConfig ? " with custom configuration." : "."));
        return builder.build();
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

    @SuppressWarnings("ConstantConditions")
    private static List<Direction> toDirectionListOrNull(List<String> rawIds) {
        if (rawIds == null || rawIds.isEmpty()) return null;
        final ImmutableList.Builder<Direction> builder = ImmutableList.builder();
        rawIds.forEach(d -> builder.add(Direction.byName(requireNonNull(d))));
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