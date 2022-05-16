package me.bright.townywars.gui;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import me.bright.townywars.TownyWars;
import me.bright.townywars.War;
import me.bright.townywars.utils.EndsWar;
import me.bright.townywars.utils.ItemHelper;
import me.bright.townywars.utils.M;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class EndWarMenu extends GUI {

    private Town winnerTown;
    private Town lostTown;
    private War war;

    public EndWarMenu(Player holder, String title, int rows) {
        super(holder, title, rows);
    }

    public EndWarMenu(Player player, War war, Town winnerTown, Town lostTown) {
        super(player,"Выбор судьбы города",3);
        this.war = war;
        this.winnerTown = winnerTown;
        this.lostTown = lostTown;
        for(int i = 0; i < 27; i++) {
            this.setItem(new ItemHelper(Material.BLACK_STAINED_GLASS_PANE).addItemFlags(ItemFlag.HIDE_ATTRIBUTES).setColoredName(" ").create(), i);
        }
        this.setItem(new ItemHelper(Material.GREEN_STAINED_GLASS_PANE).addItemFlags(ItemFlag.HIDE_ATTRIBUTES).setColoredName(" ").create(), 10);
        this.setItem(new ItemHelper(Material.GREEN_STAINED_GLASS_PANE).addItemFlags(ItemFlag.HIDE_ATTRIBUTES).setColoredName(" ").create(), 16);

        this.setItem(new ItemHelper(Material.PAPER).addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                .setColoredName("&cРазрушение")
                .setColoredLore("&7Вы уничтожаете побеждённый город.")
                .create(), 11);
        this.setClickActionForItem(11, this::destroyTown);

        this.setItem(new ItemHelper(Material.PAPER).addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                .setColoredName("&cЗахват")
                .setColoredLore(
                        "&7Побежденный город вступает в вашу нацию,",
                        "&7при этом не может её покинуть.")
                .create(), 12);
        this.setClickActionForItem(12, this::captureTown);

        this.setItem(new ItemHelper(Material.PAPER).addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                .setColoredName("&cОбъединение")
                .setColoredLore(
                        "&7Территории города передаются к вам в город,",
                        "&7cам город удаляется.")
                .create(), 13);
        this.setClickActionForItem(13, this::mergeTown);

        this.setItem(new ItemHelper(Material.PAPER).addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                .setColoredName("&cПощадить")
                .setColoredLore("&7Вы даёте 2 шанс городу и прощаете его.")
                .create(), 14);
        this.setClickActionForItem(14, this::spareTown);

        this.setItem(new ItemHelper(Material.PAPER).addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                .setColoredName("&cСделать марионеткой города")
                .setColoredLore(
                        "&7Права на владения города передаются вам, город будет",
                        "&7жить отдельно, но при этом вы имеете полный доступ",
                        "&7и управление им.")
                .create(), 15);
        this.setClickActionForItem(15, this::enslaveTown);
    }


    private void destroyTown(InventoryClickEvent event) {
        EndsWar.DESTROY.apply(war,winnerTown,lostTown);
    }

    private void captureTown(InventoryClickEvent event) {
        EndsWar.CAPTURE.apply(war,winnerTown,lostTown);
    }

    private void mergeTown(InventoryClickEvent event) {
        EndsWar.MERGE.apply(war,winnerTown,lostTown);
    }

    private void spareTown(InventoryClickEvent event) {
        EndsWar.SPARE.apply(war,winnerTown,lostTown);
    }

    private void enslaveTown(InventoryClickEvent event) {
        EndsWar.ENSLAVE.apply(war,winnerTown,lostTown);
    }

    private Collection<Chunk> getChunksAroundLocation(Location loc) {
        int[] offset = {-1,0,1};

        World world = loc.getWorld();
        int baseX = loc.getChunk().getX();
        int baseZ = loc.getChunk().getZ();

        Collection<Chunk> chunksAroundLoc = new HashSet<>();
        for(int x : offset) {
            for(int z : offset) {
                Chunk chunk = world.getChunkAt(baseX + x, baseZ + z);
                chunksAroundLoc.add(chunk);
            }
        }
        M.log("chunks around = " + chunksAroundLoc.size());
        return chunksAroundLoc;
    }

    private Block getBlockAtChunk(Chunk chunk) {
        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;
        World world = chunk.getWorld();

        for(int xx = x; xx < x + 16; xx++) {
            for(int zz = z; zz < z + 16; zz++) {
                for(int yy = 0; yy < 256; yy++) {
                    return world.getBlockAt(xx, yy, zz);
                }
            }
        }
        return null;
    }


}
