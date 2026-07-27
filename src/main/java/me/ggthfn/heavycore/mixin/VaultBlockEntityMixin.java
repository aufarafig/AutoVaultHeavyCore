package me.ggthfn.heavycore.mixin;

import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.enums.VaultState;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultSharedData;
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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;

@Mixin(VaultBlockEntity.Server.class)
public class VaultBlockEntityMixin {
    @Inject(method = "updateDisplayItem", at = @At("TAIL"))
    private static void onUpdateDisplayItem(ServerWorld world, VaultState state, VaultConfig config,
                                            VaultSharedData sharedData, BlockPos pos, CallbackInfo ci) {
        // Yarn 1.21.1: getDisplayItem() returns ItemStack directly
        ItemStack display = sharedData.getDisplayItem();
        if (!display.isEmpty() && display.isOf(Items.HEAVY_CORE)) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                HeavyCorePacketHandler.sendToClient(player, new HeavyCorePacket(pos));
            }
        }
    }
}
