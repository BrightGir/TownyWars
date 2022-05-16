package me.bright.townywars.gui;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.War;
import me.bright.townywars.WarTown;
import me.bright.townywars.phases.EndPhase;
import me.bright.townywars.utils.ItemHelper;
import me.bright.townywars.utils.M;
import me.bright.townywars.utils.invites.WarInvite;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Collection;

public class InvitesListGui extends PagedGui {

    private Resident resident;
    private Player player;

    public InvitesListGui(Player player, Town town, int page) {
        super(player,page,"Список приглашений", WarInvite.getInvites().get(town.getUUID()));
        this.resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
        this.player = player;
    }

    public InvitesListGui(Player player, int page, String title, Collection objects) {
        super(player, page, title, objects);
    }


    @Override
    public void setTargetItem() {
        WarInvite invite = ((WarInvite)this.targetObject);
        try {
            Town inviterTown = invite.getInviter().getTownOrNull();
            if(inviterTown == null || (!WarTown.getWarTownObject(inviterTown).inWar() || !(WarTown.getWarTownObject(inviterTown).getWar().getActivePhase() instanceof EndPhase))) {
                invite.delete();
                slot--;
                return;
            }

            inv.setItem(slot,
                    new ItemHelper(Material.PAPER)
                            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                            .setColoredName("&fПриглашение на войну")
                            .setColoredLore(
                                    " ",
                                    "&fПригласил: &7" + invite.getInviter().getName(),
                                    "&fГород: &7" + invite.getInviter().getTown(),
                                    " ",
                                    "&aЛКМ &7для принятия приглашения",
                                    "&cПКМ &7для отказа")
                            .create());
            this.setClickActionForItem(slot,event -> {
                try {
                    if (event.isRightClick()) {
                        invite.deny(player);
                        new InvitesListGui(player, resident.getTown(), page).open(player);
                    }
                    if (event.isLeftClick()) {
                        WarTown town = WarTown.getWarTownObject(resident.getTown());
                        if(town.inWar()) {
                            M.msg(player,M.getMessage("you_in_war"));
                            return;
                        }
                        invite.accept(player);
                        player.closeInventory();
                    }
                } catch (NotRegisteredException e) {
                    M.msg(player,M.getMessage("error"));
                    player.closeInventory();
                }
            });
        } catch (NotRegisteredException e) {
            slot--;
        }
    }

    @Override
    public void setNextItem() {
        inv.setItem(53, new ItemHelper(Material.ARROW).setColoredName("&eДальше").create());
        this.setClickActionForItem(53, event -> {
            Player p = (Player) event.getWhoClicked();
            try {
                new InvitesListGui(p,resident.getTown(), page + 1).open(p);
            } catch (NotRegisteredException e) {
                M.msg(p,M.getMessage("error"));
                p.closeInventory();
            }
        });
    }

    @Override
    public void setBackItem() {
        inv.setItem(45, new ItemHelper(Material.ARROW).setColoredName("&eНазад").create());
        this.setClickActionForItem(45, event -> {
            Player p = (Player) event.getWhoClicked();
            try {
                new InvitesListGui(p,resident.getTown(), page - 1).open(p);
            } catch (NotRegisteredException e) {
                M.msg(p,M.getMessage("error"));
                p.closeInventory();
            }
        });
    }
}
