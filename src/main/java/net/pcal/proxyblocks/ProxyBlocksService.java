package net.pcal.proxyblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.pcal.proxyblocks.ProxtBlocksRuntimeConfig.Rule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static java.util.Objects.requireNonNull;


/**
 * Central singleton service.
 */
public class ProxyBlocksService {

    // ===================================================================================
    // Constants

    public static final String LOGGER_NAME = "wallsafe";
    public static final String LOG_PREFIX = "[WallSafe] ";

    // ===================================================================================
    // Singleton

    private static final class SingletonHolder {
        private static final ProxyBlocksService INSTANCE;

        static {
            INSTANCE = new ProxyBlocksService();
        }
    }

    public static ProxyBlocksService getInstance() {
        return SingletonHolder.INSTANCE;
    }


    // ===================================================================================
    // Constructors

    ProxyBlocksService() {

    }

    public void configure(ProxtBlocksRuntimeConfig config) {
        this.config = requireNonNull(config);
    }

    // ===================================================================================
    // Fields

    private final Logger logger = LogManager.getLogger(LOGGER_NAME);
    private ProxtBlocksRuntimeConfig config;

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
        final List<Rule> rules = this.config.getRulesForBlock(clickedBlockId);
        if (rules == null || rules.isEmpty()) return;

        for (final Rule rule : rules) {
            for(final Direction direction : rule.directions()) {
                final BlockPos adjacentBlockPos = pos.offset(direction);
                final Block adjancentBlock = world.getBlockState(adjacentBlockPos).getBlock();
                final Identifier adjacentBlockId = Registry.BLOCK.getId(adjancentBlock);
                if (rule.adjacentBlockIds() != null && !rule.adjacentBlockIds().contains(adjacentBlockId)) {
                    continue;
                }
//                if (rule.adjacentBlockNames() != null && !rule.adjacentBlockNames().contains(adjacentBlockId)) {//FIXME
//                    continue;
//                }
                adjancentBlock.onUse(state, world, adjacentBlockPos, player, hand, hit);
                cir.cancel();
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }
        }
    }

}
