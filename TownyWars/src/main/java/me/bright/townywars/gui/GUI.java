package me.bright.townywars.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

import java.util.HashMap;

public abstract class GUI {

    protected Inventory inv;
    private Consumer<InventoryClickEvent> clickAction;
    private HashMap<Integer,Consumer<InventoryClickEvent>> itemClickActions = new HashMap<>();

    private static HashMap<String, GUI> guis = new HashMap<>();

    public GUI(Player holder, String title, int rows) {
        this.inv = Bukkit.createInventory(holder,rows*9, Component.text(title));
        guis.put(title,this);
    }

    public void setItem(ItemStack item, int slot) {
        inv.setItem(slot,item);
    }

    public void open(Player player) {
        player.openInventory(inv);
    }

    public void setClickAction(Consumer<InventoryClickEvent> action) {
        this.clickAction = action;
    }

    public void setClickActionForItem(int itemSlot, Consumer<InventoryClickEvent> action) {
        itemClickActions.put(itemSlot,action);
    }

    public static GUI getGuiByTitle(String name) {
        return guis.get(name);
    }

    public void acceptClickAction(InventoryClickEvent event) {
        if(clickAction != null) {
            clickAction.accept(event);
        }
    }
    public void acceptClickActionForItem(InventoryClickEvent event) {
        if(itemClickActions.get(event.getSlot()) != null) {
            itemClickActions.get(event.getSlot()).accept(event);
        }
    }
}
