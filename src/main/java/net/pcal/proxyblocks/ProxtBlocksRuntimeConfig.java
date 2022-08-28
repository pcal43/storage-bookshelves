package net.pcal.proxyblocks;

import com.google.common.collect.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Runtime representation of configuration. FIXME should allow more than one RBC per block bootId
 */
@SuppressWarnings("ClassCanBeRecord")
class ProxtBlocksRuntimeConfig {

    private final List<Rule> rules;
    private final ListMultimap<Identifier, Rule> rulesPerBlock = ArrayListMultimap.create();

    ProxtBlocksRuntimeConfig(List<Rule> rules) {
        this.rules = requireNonNull(rules);
        for (final Rule rule : rules) {
            for(Identifier clickedBlockId : rule.clickedBlockIds) {
                this.rulesPerBlock.put(clickedBlockId, rule);
            }
        }
    }

    List<Rule> getRulesForBlock(Identifier clickedBlockId) {
        return this.rulesPerBlock.get(clickedBlockId);
    }

    record Rule(
            String name,
            Set<Identifier> clickedBlockIds,
            Set<Identifier> adjacentBlockIds,
            Set<String> adjacentBlockNames,
            List<Direction> directions
    ) {

        Rule(String name,
             Set<Identifier> clickedBlockIds,
             Set<Identifier> adjacentBlockIds,
             Set<String> adjacentBlockNames,
             List<Direction> directions) {
            this.name = name != null ? name : "unnamed";
            this.clickedBlockIds = clickedBlockIds;
            this.adjacentBlockIds = adjacentBlockIds;
            this.directions = directions != null ? directions : List.of(Direction.values());
            this.adjacentBlockNames = adjacentBlockNames;
        }
    }
}
