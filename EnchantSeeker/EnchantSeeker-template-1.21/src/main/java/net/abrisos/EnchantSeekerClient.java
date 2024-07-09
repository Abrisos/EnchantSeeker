package net.abrisos;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.village.TradeOffer;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
public class EnchantSeekerClient {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static KeyBinding tradeKeyBinding;
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);

    public void onInitializeClient() {
        tradeKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "EnchantSeeker",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F10,
                "EnchantSeeker"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (tradeKeyBinding.wasPressed()) {
                toggleCycle();
            }
        });
    }

    private void toggleCycle() {
        if (isRunning.get()) {
            stopCycle();
        } else {
            startCycle();
        }
    }

    private void startCycle() {
        if (isRunning.compareAndSet(false, true)) {
            CLIENT.player.sendMessage(Text.literal("Mod enabled"), false);
            CLIENT.execute(this::placeBlockInCrosshair);
        }
    }

    private void stopCycle() {
        if (isRunning.compareAndSet(true, false)) {
            CLIENT.player.sendMessage(Text.literal("Mod disabled"), false);
        }
    }


    private void placeBlockInCrosshair() {
        if (!isRunning.get() || CLIENT.player == null || CLIENT.world == null || CLIENT.interactionManager == null) {
            return;
        }

        pressKey(GLFW.GLFW_KEY_1);

        CompletableFuture.delayedExecutor(200, TimeUnit.MILLISECONDS).execute(() -> {
            CLIENT.execute(() -> {
                HitResult hitResult = CLIENT.crosshairTarget;
                if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos targetPos = blockHitResult.getBlockPos();
                    Direction side = blockHitResult.getSide();
                    BlockPos placePos = targetPos.offset(side);

                    CLIENT.interactionManager.interactBlock(CLIENT.player, Hand.MAIN_HAND, blockHitResult);
                    CLIENT.player.swingHand(Hand.MAIN_HAND);

                    CompletableFuture.delayedExecutor(700, TimeUnit.MILLISECONDS)
                            .execute(() -> tryOpenVillagerMenu(2));
                } else {
                    CLIENT.player.sendMessage(Text.literal("Unable to find a block for placement"), false);
                }
            });
        });
    }

    private void tryOpenVillagerMenu(int attemptsLeft) {
        if (!isRunning.get()) {
            return;
        }

        Optional<VillagerEntity> nearestVillager = CLIENT.world.getEntitiesByClass(
                VillagerEntity.class,
                CLIENT.player.getBoundingBox().expand(5),
                villager -> true
        ).stream().min(Comparator.comparingDouble(CLIENT.player::squaredDistanceTo));

        if (nearestVillager.isPresent()) {
            VillagerEntity villager = nearestVillager.get();

            CLIENT.execute(() -> {
                CLIENT.interactionManager.interactEntity(CLIENT.player, villager, Hand.MAIN_HAND);
                CLIENT.player.swingHand(Hand.MAIN_HAND);

                CompletableFuture.delayedExecutor(200, TimeUnit.MILLISECONDS).execute(() -> {
                    if (CLIENT.currentScreen instanceof MerchantScreen) {
                        handleTradeScreen((MerchantScreen) CLIENT.currentScreen);
                    } else if (attemptsLeft > 0) {
                        long delay = (attemptsLeft == 2) ? 500 : 700;
                        CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS)
                                .execute(() -> tryOpenVillagerMenu(attemptsLeft - 1));
                    } else {
                        CLIENT.player.sendMessage(Text.literal("Unable to open the menu after multiple attempts. The cycle is being restarted."), false);
                        breakBlockAndRestart();
                    }
                });
            });
        } else {
            CLIENT.player.sendMessage(Text.literal("No villagers found nearby."), false);
            breakBlockAndRestart();
        }
    }

    private void handleTradeScreen(MerchantScreen merchantScreen) {
        boolean hasTargetEnchantedBook = checkForEnchantedBook(merchantScreen.getScreenHandler());
        if (hasTargetEnchantedBook) {
            CLIENT.player.sendMessage(Text.literal("Enchanted book found."), false);
            stopCycle();
        } else {
            CLIENT.execute(() -> {
                CLIENT.player.closeHandledScreen();
                pressKey(GLFW.GLFW_KEY_2);
                CompletableFuture.delayedExecutor(300, TimeUnit.MILLISECONDS).execute(this::breakBlockAndRestart);
            });
        }
    }

    private boolean checkForEnchantedBook(MerchantScreenHandler handler) {
        if (EnchantSeeker.targetEnchantments.isEmpty()) {
            CLIENT.player.sendMessage(Text.literal("The list of target enchantments is empty."), false);
            return false;
        }

        for (TradeOffer offer : handler.getRecipes()) {
            ItemStack sellItem = offer.getSellItem();
            if (sellItem.getItem() instanceof EnchantedBookItem) {
                String bookEnchantment = getEnchantmentsFromItem(sellItem);
                if (EnchantSeeker.targetEnchantments.containsKey(bookEnchantment)) {
                    CLIENT.player.sendMessage(Text.literal("The target enchantment has been found: " + bookEnchantment), false);
                    return true;
                }
                CLIENT.player.sendMessage(Text.literal("The target enchantment not found. The villager had: " + bookEnchantment), false);
            }
        }

        return false;
    }

    private String getEnchantmentsFromItem(ItemStack itemStack) {
        List<String> enchantments = new ArrayList<>();
        var enchantmentComponent = EnchantmentHelper.getEnchantments(itemStack);
        int level = 1;

        String componentString = enchantmentComponent.toString();
        int startIndex = componentString.indexOf("minecraft:enchantment /");
        String enchantmentId = null;
        if (startIndex != -1) {
            startIndex = componentString.indexOf('/', startIndex) + 1;
            int endIndex = componentString.indexOf(']', startIndex);
            if (endIndex != -1) {
                enchantmentId = componentString.substring(startIndex, endIndex).trim();
                int levelIndex = componentString.indexOf("=>", endIndex);
                if (levelIndex != -1) {
                    int levelEndIndex = componentString.indexOf('}', levelIndex);
                    if (levelEndIndex != -1) {
                        try {
                            level = Integer.parseInt(componentString.substring(levelIndex + 2, levelEndIndex).trim());
                        } catch (NumberFormatException ignored) {

                        }
                    }
                }

                enchantments.add(enchantmentId + "_" + level);
            }
        }

        return enchantmentId + "_" + level;
    }

    private void breakBlockAndRestart() {
        if (!isRunning.get()) {
            return;
        }

        assert CLIENT.player != null;

        HitResult hitResult = CLIENT.crosshairTarget;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction side = blockHitResult.getSide();

            CLIENT.execute(() -> {
                assert CLIENT.interactionManager != null;
                CLIENT.interactionManager.attackBlock(blockPos, side);

                CLIENT.options.attackKey.setPressed(true);

                CompletableFuture.runAsync(() -> {
                    try {
                        while (!CLIENT.world.getBlockState(blockPos).isAir() && isRunning.get()) {
                            Thread.sleep(50);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        CLIENT.execute(() -> {
                            CLIENT.options.attackKey.setPressed(false);

                            if (isRunning.get()) {
                                CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS).execute(this::placeBlockInCrosshair);
                            }
                        });
                    }
                });
            });
        } else {
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS).execute(this::placeBlockInCrosshair);
        }
    }

    private void pressKey(int keyCode) {
        CLIENT.execute(() -> {
            CLIENT.keyboard.onKey(CLIENT.getWindow().getHandle(), keyCode, 0, GLFW.GLFW_PRESS, 0);
            CLIENT.keyboard.onKey(CLIENT.getWindow().getHandle(), keyCode, 0, GLFW.GLFW_RELEASE, 0);
        });
    }
}
