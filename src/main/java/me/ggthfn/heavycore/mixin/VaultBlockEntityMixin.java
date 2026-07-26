package me.ggthfn.heavycore.mixin;

import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.ggthfn.heavycore.network.HeavyCorePacket;
import me.ggthfn.heavycore.network.HeavyCorePacketHandler;

@Mixin(VaultBlockEntity.class)
public class VaultBlockEntityMixin {
    @Inject(method = "setDisplayItem", at = @At("TAIL"))
    private void onSetDisplayItem(ItemStack stack, CallbackInfo ci) {
        if (!stack.isEmpty() && stack.getItem() == Items.HEAVY_CORE) {
            VaultBlockEntity self = (VaultBlockEntity)(Object)this;
            BlockPos pos = self.getPos();
            if (self.getWorld() instanceof ServerWorld serverWorld) {
                for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                    HeavyCorePacketHandler.sendToClient(player, new HeavyCorePacket(pos));
                }
            }
        }
    }
}
