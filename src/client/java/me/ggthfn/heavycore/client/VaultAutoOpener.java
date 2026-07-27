package me.ggthfn.heavycore.client;

import net.minecraft.block.BlockState;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.enums.VaultState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public final class VaultAutoOpener {
    private static int cooldown = 0;

    // Throttle: suppress repeated SKIP logs for same vault+item
    private static BlockPos lastSkipPos = null;
    private static String lastSkipItemId = null;

    // Track vaults already triggered (avoid wasting keys on looted vaults)
    private static final Set<BlockPos> triggeredThisSession = new HashSet<>();

    public static int getCooldown() { return cooldown; }
    public static boolean isAlreadyTriggered(BlockPos pos) { return triggeredThisSession.contains(pos); }

    private VaultAutoOpener() {}

    public static void onClientTick(MinecraftClient client) {
        if (cooldown > 0) cooldown--;

        if (!HeavyCoreConfig.get().enabled) return;
        ClientPlayerEntity player = client.player;
        World world = client.world;
        if (player == null || world == null) {
            clearSkipThrottle();
            return;
        }

        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHit)) {
            clearSkipThrottle();
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof VaultBlock)) {
            clearSkipThrottle();
            return;
        }

        VaultState vs = state.get(VaultBlock.VAULT_STATE);
        if (vs != VaultState.ACTIVE) {
            clearSkipThrottle();
            return;
        }

        boolean ominous = state.get(VaultBlock.OMINOUS);
        String vaultType = ominous ? "OMINOUS" : "REGULAR";

        if (ominous && !HeavyCoreConfig.get().openOminous) return;
        if (!ominous && !HeavyCoreConfig.get().openNormal) return;

        if (cooldown > 0) return;

        // Skip if this vault was already triggered this session
        if (triggeredThisSession.contains(pos)) return;

        Hand keyHand = findKeyHand(player);
        if (keyHand == null) return;

        ItemStack stack = player.getStackInHand(keyHand);
        if (keyMatchesVault(stack, ominous)) {
            if (displayItemPassesFilter(world, pos, vaultType)) {
                String displayName = getItemDisplayName(world, pos);
                String itemId = getDisplayItemId(world, pos);
                System.out.println("[VaultAutoOpener] Triggered! vault=" + vaultType + " item=" + displayName + " id=" + itemId);
                dumpEnchantments(world, pos);
                if (client.interactionManager != null) {
                    ActionResult actionResult = client.interactionManager.interactBlock(player, keyHand, blockHit);
                    if (actionResult.isAccepted()) {
                        player.swingHand(keyHand);
                        cooldown = 8;
                        triggeredThisSession.add(pos);
                        System.out.println("[VaultAutoOpener] Block accepted, cooldown=" + cooldown + " frames");
                    }
                }
            }
        }
    }

    private static void clearSkipThrottle() {
        lastSkipPos = null;
        lastSkipItemId = null;
    }

    private static Hand findKeyHand(PlayerEntity player) {
        if (isAnyTrialKey(player.getMainHandStack())) return Hand.MAIN_HAND;
        if (isAnyTrialKey(player.getOffHandStack()))  return Hand.OFF_HAND;
        return null;
    }

    private static boolean isAnyTrialKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.isOf(Items.TRIAL_KEY) || stack.isOf(Items.OMINOUS_TRIAL_KEY);
    }

    private static boolean keyMatchesVault(ItemStack stack, boolean ominousVault) {
        if (stack == null || stack.isEmpty()) return false;
        if (ominousVault) return stack.isOf(Items.OMINOUS_TRIAL_KEY);
        return stack.isOf(Items.TRIAL_KEY);
    }

    private static String getDisplayItemId(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof VaultBlockEntity vault)) return null;

        DynamicRegistryManager drm = world.getRegistryManager();
        NbtCompound nbt;
        try {
            nbt = vault.createNbtWithIdentifyingData(drm);
        } catch (Throwable t) {
            return null;
        }

        if (!nbt.contains("shared_data")) return null;
        NbtCompound shared = nbt.getCompound("shared_data");

        if (!shared.contains("display_item")) return null;
        NbtCompound item = shared.getCompound("display_item");

        return item.getString("id");
    }

    private static String getItemDisplayName(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof VaultBlockEntity vault)) return "?";

        ItemStack display = vault.getSharedData().getDisplayItem();
        if (display == null || display.isEmpty()) return "?";
        return display.getName().getString();
    }

    private static boolean displayItemPassesFilter(World world, BlockPos pos, String vaultType) {
        String itemId = getDisplayItemId(world, pos);
        if (itemId == null || itemId.isEmpty()) {
            skipLog(pos, itemId, vaultType, "no item id");
            return false;
        }

        Identifier id = Identifier.tryParse(itemId);
        if (id == null || !Registries.ITEM.containsId(id)) {
            skipLog(pos, itemId, vaultType, "invalid id: " + itemId);
            return false;
        }

        if (!HeavyCoreConfig.get().filter.contains(itemId)) {
            skipLog(pos, itemId, vaultType, "not in filter");
            return false;
        }

        if ("minecraft:enchanted_book".equals(itemId) && HeavyCoreConfig.get().requireWindBurstOnBook) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof VaultBlockEntity vault) {
                DynamicRegistryManager drm = world.getRegistryManager();
                try {
                    NbtCompound nbt = vault.createNbtWithIdentifyingData(drm);
                    if (nbt.contains("shared_data")) {
                        NbtCompound shared = nbt.getCompound("shared_data");
                        if (shared.contains("display_item")) {
                            NbtCompound item = shared.getCompound("display_item");
                            boolean hasWB = hasWindBurst(item);
                            // Always log WindBurst decisions (not throttled)
                            System.out.println("[VaultAutoOpener] enchanted_book vault=" + vaultType + " WindBurst=" + hasWB);
                            return hasWB;
                        }
                    }
                } catch (Throwable ignored) {}
            }
            skipLog(pos, itemId, vaultType, "no WindBurst enchantment");
            return false;
        }

        return true;
    }

    private static void skipLog(BlockPos pos, String itemId, String vaultType, String reason) {
        if (pos.equals(lastSkipPos) && itemId != null && itemId.equals(lastSkipItemId)) return;
        lastSkipPos = pos;
        lastSkipItemId = itemId;
        System.out.println("[VaultAutoOpener] SKIP vault=" + vaultType + " item=" + itemId + " reason=" + reason);
    }

    private static void dumpEnchantments(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof VaultBlockEntity vault)) return;
        try {
            NbtCompound nbt = vault.createNbtWithIdentifyingData(world.getRegistryManager());
            if (!nbt.contains("shared_data")) return;
            NbtCompound shared = nbt.getCompound("shared_data");
            if (!shared.contains("display_item")) return;
            NbtCompound item = shared.getCompound("display_item");
            if (!item.contains("components")) return;
            NbtCompound comps = item.getCompound("components");
            if (!comps.contains("minecraft:stored_enchantments")) return;
            NbtCompound stored = comps.getCompound("minecraft:stored_enchantments");
            NbtCompound levels = stored.contains("levels") ? stored.getCompound("levels") : stored;
            Set<String> keys = levels.getKeys();
            if (keys.isEmpty()) {
                System.out.println("[VaultAutoOpener]   enchantments: (none)");
            } else {
                for (String ench : keys) {
                    int lvl = levels.getInt(ench);
                    System.out.println("[VaultAutoOpener]   enchantment: " + ench + " level=" + lvl);
                }
            }
        } catch (Throwable ignored) {}
    }

    private static boolean hasWindBurst(NbtCompound item) {
        if (!item.contains("components")) return false;
        NbtCompound comps = item.getCompound("components");

        if (!comps.contains("minecraft:stored_enchantments")) return false;
        NbtCompound stored = comps.getCompound("minecraft:stored_enchantments");

        NbtCompound levels = stored.contains("levels") ? stored.getCompound("levels") : stored;
        return levels.contains("minecraft:wind_burst");
    }
}
