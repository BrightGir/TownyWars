package me.bright.townywars.gui;

import me.bright.townywars.War;
import me.bright.townywars.utils.ItemHelper;
import me.bright.townywars.utils.M;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class WarListGui extends PagedGui {


    public WarListGui(Player player, int page) {
        super(player,page,"Список войн",War.getWars());
    }

    @Override
    public void setTargetItem() {
        War war = ((War)this.targetObject);
        inv.setItem(slot,
                new ItemHelper(Material.RED_BANNER)
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .setColoredName("&f" + war.getAttacker().getName() + " &cпротив&f " + war.getOpponent().getName())
                        .setColoredLore(" ",
                                "&fID - &7[&f" + war.getId() + "&7]",
                                "&fФаза войны - &7" + war.getActivePhase().getName(),
                                " ",
                                "&fСторона 1:",
                                "&7" + war.getAttacker().getName() + " (&fглавный город&7)",
                                " ",
                                "&fСторона 2:",
                                "&7" + war.getOpponent().getName() + " (&fглавный город&7)")
                        .create());
    }

    @Override
    public void setNextItem() {
        inv.setItem(53, new ItemHelper(Material.ARROW).setColoredName("&eДальше").create());
        this.setClickActionForItem(53, event -> {
            Player p = (Player) event.getWhoClicked();
            new WarListGui(p, page + 1).open(p);
        });
    }

    @Override
    public void setBackItem() {
        inv.setItem(45, new ItemHelper(Material.ARROW).setColoredName("&eНазад").create());
        this.setClickActionForItem(45,event -> {
            Player p = (Player) event.getWhoClicked();
            new WarListGui(p,page - 1).open(p);
        });
    }

}
