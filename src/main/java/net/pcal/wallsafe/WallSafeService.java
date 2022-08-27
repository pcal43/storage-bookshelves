package net.pcal.wallsafe;

import com.google.common.collect.ImmutableSet;
import com.google.common.math.DoubleMath;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.pcal.wallsafe.WallSafeRuntimeConfig.Rule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static java.util.Objects.requireNonNull;


/**
 * Central singleton service.
 */
public class WallSafeService {

    // ===================================================================================
    // Constants

    public static final String LOGGER_NAME = "wallsafe";
    public static final String LOG_PREFIX = "[WallSafe] ";

    // ===================================================================================
    // Singleton

    private static final class SingletonHolder {
        private static final WallSafeService INSTANCE;

        static {
            INSTANCE = new WallSafeService();
        }
    }

    public static WallSafeService getInstance() {
        return SingletonHolder.INSTANCE;
    }


    // ===================================================================================
    // Constructors

    WallSafeService() {

    }

    public void configure(WallSafeRuntimeConfig config) {
        this.config = requireNonNull(config);
    }

    // ===================================================================================
    // Fields

    private final Logger logger = LogManager.getLogger(LOGGER_NAME);
    private WallSafeRuntimeConfig config;

    /**
     * This will be called whenever a player uses a block.
     */
    public void onUseBlock(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        final Block block = state.getBlock();
        final Identifier blockId = Registry.BLOCK.getId(block);
        System.out.println("used "+blockId);
    }
}
