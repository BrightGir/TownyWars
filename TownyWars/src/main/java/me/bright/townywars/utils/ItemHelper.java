package me.bright.townywars.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class ItemHelper {

    private ItemStack item;
    private ItemMeta meta;

    public ItemHelper(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemHelper(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    public ItemHelper setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemHelper setColoredLore(String... strings) {
        meta.setLore(M.color(strings));
        return this;
    }

    public ItemHelper setColoredLore(ArrayList<String> lore) {
        meta.setLore(M.color(lore));
        return this;
    }

    public ItemHelper setColoredName(String name) {
        meta.setDisplayName(M.color(name));
        return this;
    }

    public ItemHelper setUnbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemHelper addItemFlags(ItemFlag... flags) {
        item.addItemFlags(flags);
        return this;
    }

    public ItemStack create() {
        item.setItemMeta(meta);
        return item;
    }


}
