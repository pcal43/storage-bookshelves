package net.pcal.proxyblocks.mixins;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pcal.proxyblocks.ProxyBlocksService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class BlockClickedMixin {

    //@Shadow
    //private BlockPos blockPos;

    // get notified any time an entity's blockPos is updated
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    void _entity_blockPos_update(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient) return;
        ProxyBlocksService.getInstance().onUseBlock(state, world, pos,player, hand ,hit, cir);
    }
}