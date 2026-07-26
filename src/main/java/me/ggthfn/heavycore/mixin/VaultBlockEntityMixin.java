package me.ggthfn.heavycore.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
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

@Mixin(VaultBlockEntity.Server.class)
public class VaultBlockEntityMixin {
    @Inject(method = "updateDisplayItem", at = @At("TAIL"))
    private static void onUpdateDisplayItem(ServerWorld world, BlockPos pos, BlockState state, VaultConfig config,
                                            VaultServerData serverData, VaultSharedData sharedData, CallbackInfo ci) {
        ItemStack display = sharedData.getDisplayItem();
        if (!display.isEmpty() && display.getItem() == Items.HEAVY_CORE) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                HeavyCorePacketHandler.sendToClient(player, new HeavyCorePacket(pos));
            }
        }
    }
}
