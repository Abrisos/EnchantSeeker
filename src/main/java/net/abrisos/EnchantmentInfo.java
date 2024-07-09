package net.abrisos;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentInfo {
    private String name;
    private int maxLevel;

    public EnchantmentInfo(String name, int maxLevel) {
        this.name = capitalizeFully(name);
        this.maxLevel = maxLevel;
    }

    private String capitalizeFully(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.split("_");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append("_");
            result.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1).toLowerCase());
        }
        return result.toString();
    }
    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public static List<EnchantmentInfo> getAllEnchantments() {
        List<EnchantmentInfo> enchantments = new ArrayList<>();
        enchantments.add(new EnchantmentInfo("PROTECTION", 4));
        enchantments.add(new EnchantmentInfo("FIRE_PROTECTION", 4));
        enchantments.add(new EnchantmentInfo("FEATHER_FALLING", 4));
        enchantments.add(new EnchantmentInfo("BLAST_PROTECTION", 4));
        enchantments.add(new EnchantmentInfo("PROJECTILE_PROTECTION", 4));
        enchantments.add(new EnchantmentInfo("RESPIRATION", 3));
        enchantments.add(new EnchantmentInfo("AQUA_AFFINITY", 1));
        enchantments.add(new EnchantmentInfo("THORNS", 3));
        enchantments.add(new EnchantmentInfo("DEPTH_STRIDER", 3));
        enchantments.add(new EnchantmentInfo("FROST_WALKER", 2));
        enchantments.add(new EnchantmentInfo("BINDING_CURSE", 1));
        enchantments.add(new EnchantmentInfo("SOUL_SPEED", 3));
        enchantments.add(new EnchantmentInfo("SHARPNESS", 5));
        enchantments.add(new EnchantmentInfo("SMITE", 5));
        enchantments.add(new EnchantmentInfo("BANE_OF_ARTHROPODS", 5));
        enchantments.add(new EnchantmentInfo("KNOCKBACK", 2));
        enchantments.add(new EnchantmentInfo("FIRE_ASPECT", 2));
        enchantments.add(new EnchantmentInfo("LOOTING", 3));
        enchantments.add(new EnchantmentInfo("SWEEPING_EDGE", 3));
        enchantments.add(new EnchantmentInfo("EFFICIENCY", 5));
        enchantments.add(new EnchantmentInfo("SILK_TOUCH", 1));
        enchantments.add(new EnchantmentInfo("UNBREAKING", 3));
        enchantments.add(new EnchantmentInfo("FORTUNE", 3));
        enchantments.add(new EnchantmentInfo("POWER", 5));
        enchantments.add(new EnchantmentInfo("PUNCH", 2));
        enchantments.add(new EnchantmentInfo("FLAME", 1));
        enchantments.add(new EnchantmentInfo("INFINITY", 1));
        enchantments.add(new EnchantmentInfo("LUCK_OF_THE_SEA", 3));
        enchantments.add(new EnchantmentInfo("LURE", 3));
        enchantments.add(new EnchantmentInfo("LOYALTY", 3));
        enchantments.add(new EnchantmentInfo("IMPALING", 5));
        enchantments.add(new EnchantmentInfo("RIPTIDE", 3));
        enchantments.add(new EnchantmentInfo("CHANNELING", 1));
        enchantments.add(new EnchantmentInfo("MULTISHOT", 1));
        enchantments.add(new EnchantmentInfo("QUICK_CHARGE", 3));
        enchantments.add(new EnchantmentInfo("PIERCING", 4));
        enchantments.add(new EnchantmentInfo("MENDING", 1));
        enchantments.add(new EnchantmentInfo("VANISHING_CURSE", 1));
        enchantments.add(new EnchantmentInfo("WIND_BURST", 3));
        enchantments.add(new EnchantmentInfo("DENSITY", 5));
        enchantments.add(new EnchantmentInfo("BREACH", 4));
        return enchantments;
    }
}