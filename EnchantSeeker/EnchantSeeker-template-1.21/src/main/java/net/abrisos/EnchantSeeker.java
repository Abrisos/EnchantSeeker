package net.abrisos;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EnchantSeeker implements ClientModInitializer {
    public static final Map<String, Integer> targetEnchantments = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("enchantseeker")
                            .then(ClientCommandManager.literal("add")
                                    .then(ClientCommandManager.argument("enchantment", StringArgumentType.word())
                                            .suggests(this::suggestEnchantments)
                                            .then(ClientCommandManager.argument("level", IntegerArgumentType.integer(1))
                                                    .suggests(this::suggestLevels)
                                                    .executes(this::executeAddEnchantment))))
                            .then(ClientCommandManager.literal("list")
                                    .executes(this::executeListEnchantments))
                            .then(ClientCommandManager.literal("clear")
                                    .executes(this::executeClearEnchantments))
                            .then(ClientCommandManager.literal("info")
                                    .executes(this::executeInfo))
            );
        });

        // Инициализация клиентской логики
        EnchantSeekerClient clientInitializer = new EnchantSeekerClient();
        clientInitializer.onInitializeClient();
    }

    private CompletableFuture<Suggestions> suggestEnchantments(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        EnchantmentInfo.getAllEnchantments().stream()
                .filter(enchantment -> enchantment.getName().toLowerCase().startsWith(input))
                .forEach(enchantment -> builder.suggest(enchantment.getName()));
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestLevels(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        String enchantmentName = StringArgumentType.getString(context, "enchantment");
        EnchantmentInfo selectedEnchantment = EnchantmentInfo.getAllEnchantments().stream()
                .filter(e -> e.getName().equalsIgnoreCase(enchantmentName))
                .findFirst()
                .orElse(null);

        if (selectedEnchantment != null) {
            for (int i = 1; i <= selectedEnchantment.getMaxLevel(); i++) {
                builder.suggest(Integer.toString(i));
            }
        }
        return builder.buildFuture();
    }

    private int executeAddEnchantment(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String enchantmentInput = StringArgumentType.getString(context, "enchantment");
        int level = IntegerArgumentType.getInteger(context, "level");

        String formattedEnchantment = "minecraft:" + enchantmentInput.toLowerCase().replace(" ", "_");
        String enchantmentKey = formattedEnchantment + "_" + level;

        targetEnchantments.put(enchantmentKey, level);

        context.getSource().sendFeedback(Text.literal("Enchantment added: " + enchantmentKey));
        return 1;
    }

    private int executeInfo(CommandContext<FabricClientCommandSource> context) {
        MutableText message = Text.literal("To use the mod, assign a key in the settings, then use the command /enchantseeker add to select the spell(s) you want to find and their level(s). Place a lectern in the first slot, an axe in the second slot, and press the assigned key. You can stop the mod by pressing the same key again.\n\nP.S. To ensure the lectern is always collected, you can pour water near the villager to make it roll toward you. Here's an example of the setup (look at the red wool): ");

        MutableText clickableLink = Text.literal("https://imgur.com/a/FoJXVG4")
                .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://imgur.com/a/FoJXVG4"))
                        .withUnderline(true)
                        .withColor(Formatting.BLUE)
                );

        message.append(clickableLink);

        context.getSource().sendFeedback(message);
        return 1;
    }

    private int executeListEnchantments(CommandContext<FabricClientCommandSource> context) {
        StringBuilder list = new StringBuilder("Current target enchantments:\n");
        targetEnchantments.keySet().forEach(enchantment -> list.append(enchantment).append("\n"));
        context.getSource().sendFeedback(Text.literal(list.toString()));
        return 1;
    }
    private int executeClearEnchantments(CommandContext<FabricClientCommandSource> context) {
        targetEnchantments.clear();
        context.getSource().sendFeedback(Text.literal("The list of target enchantments cleared."));
        return 1;
    }
}