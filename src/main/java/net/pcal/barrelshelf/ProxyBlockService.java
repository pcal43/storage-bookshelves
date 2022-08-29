package net.pcal.barrelshelf;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static java.util.Objects.requireNonNull;


/**
 * Central singleton service.
 */
public class ProxyBlockService {

    // ===================================================================================
    // Constants

    public static final String LOGGER_NAME = "barrelshelves";
    public static final String LOG_PREFIX = "[BarrelShelves] ";

    // ===================================================================================
    // Singleton

    private static final class SingletonHolder {
        private static final ProxyBlockService INSTANCE;

        static {
            INSTANCE = new ProxyBlockService();
        }
    }

    public static ProxyBlockService getInstance() {
        return SingletonHolder.INSTANCE;
    }


    // ===================================================================================
    // Fields
    private final ListMultimap<Identifier, ProxyBlockRule> rulesPerBlock = ArrayListMultimap.create();

    // ===================================================================================
    // Constructors

    ProxyBlockService() {

    }
    public void addRules(Collection<ProxyBlockRule> rules) {
        for (final ProxyBlockRule rule : rules) {
            for(Identifier clickedBlockId : rule.clickedBlockIds()) {
                this.rulesPerBlock.put(clickedBlockId, rule);
            }
        }
    }

    // ===================================================================================
    // Fields

    /**
     * This will be called whenever a player uses a block.
     */
    public void onUseBlock(BlockState state,
                           World world,
                           BlockPos pos,
                           PlayerEntity player,
                           Hand hand,
                           BlockHitResult hit,
                           CallbackInfoReturnable<ActionResult> cir) {
        final Identifier clickedBlockId = Registry.BLOCK.getId(state.getBlock());
        final List<ProxyBlockRule> rules = this.rulesPerBlock.get(clickedBlockId);
        if (rules == null || rules.isEmpty()) return;
        for (final ProxyBlockRule rule : rules) {
            for(final Direction direction : rule.directions()) {
                final BlockPos adjacentBlockPos = pos.offset(direction);
                final Block adjancentBlock = world.getBlockState(adjacentBlockPos).getBlock();
                final Identifier adjacentBlockId = Registry.BLOCK.getId(adjancentBlock);
                if (rule.adjacentBlockIds() != null && !rule.adjacentBlockIds().contains(adjacentBlockId)) {
                    continue;
                }
                if (rule.adjacentBlockIds() != null && !rule.adjacentBlockNames().isEmpty()) {
                    if (!isNameMatch(rule.adjacentBlockNames(), world, adjacentBlockPos)) return;
                }
                adjancentBlock.onUse(state, world, adjacentBlockPos, player, hand, hit);
                cir.cancel();
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }
        }
    }

    /**
     * Return true if the block at the given position has a name and it matches one of the given strings.
     */
    private static boolean isNameMatch(Collection<String> names, World world, BlockPos pos) {
        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof LootableContainerBlockEntity)) return false;
        final Text nameText = ((LootableContainerBlockEntity)blockEntity).getCustomName();
        if (nameText == null) return false;
        final String name = nameText.getString();
        if (name == null) return false;
        return names.contains(name);
    }
}
