package me.bright.townywars.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListener implements Listener {


    @EventHandler
    public void onClick(InventoryClickEvent event) {
        GUI gui = GUI.getGuiByTitle(event.getView().getTitle());
        if(gui != null) {
            gui.acceptClickAction(event);
            gui.acceptClickActionForItem(event);
            event.setCancelled(true);
        }
    }
}
