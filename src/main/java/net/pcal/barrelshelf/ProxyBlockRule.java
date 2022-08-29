package net.pcal.barrelshelf;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Set;

record ProxyBlockRule(
        String name,
        Set<Identifier> clickedBlockIds,
        Set<Identifier> adjacentBlockIds,
        Set<String> adjacentBlockNames,
        List<Direction> directions
) {

    ProxyBlockRule(String name,
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
