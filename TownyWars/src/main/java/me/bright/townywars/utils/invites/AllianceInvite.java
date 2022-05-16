package me.bright.townywars.utils.invites;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import me.bright.townywars.Alliance;
import me.bright.townywars.TownyWars;
import me.bright.townywars.utils.M;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class AllianceInvite {

    private static HashMap<UUID, AllianceInvite> invites = new HashMap<>();
    private String allianceName;
    private Resident inviter;
    private Nation inviterNation;
    private Nation nation;
    private Resident nationPlayer;
    private BukkitTask denyTimer;
    private boolean isCreateAlliance;
    private Alliance alliance;

    public AllianceInvite(String allianceName, Resident inviter, Nation nation) {
        this.nationPlayer = nation.getKing();
        this.allianceName = allianceName;
        this.inviter = inviter;
        this.inviterNation = inviter.getNationOrNull();
        this.nation = nation;
        invites.put(nation.getUUID(),this);
        denyTimer = new BukkitRunnable() {
            @Override
            public void run() {
                AllianceInvite.this.deny(true);
            }
        }.runTaskLaterAsynchronously(TownyWars.getPlugin(),20L * 60L * 3);
        this.isCreateAlliance = true;
    }

    public AllianceInvite(Resident inviter, Alliance alliance, Nation nation) {
        this.alliance = alliance;
        this.nationPlayer = nation.getKing();
        this.allianceName = alliance.getName();
        this.inviter = inviter;
        this.inviterNation = inviter.getNationOrNull();
        this.nation = nation;
        invites.put(nation.getUUID(),this);
        denyTimer = new BukkitRunnable() {
            @Override
            public void run() {
                AllianceInvite.this.deny(true);
            }
        }.runTaskLaterAsynchronously(TownyWars.getPlugin(),20L * 60L * 3);
        this.isCreateAlliance = false;
    }


    public static HashMap<UUID, AllianceInvite> getInvites() {
        return invites;
    }

    public void delete() {
        denyTimer.cancel();
        invites.remove(nation.getUUID());
    }

    public void accept() {
        if(inviter.isOnline()) {
            inviter.getPlayer().sendMessage(M.color(M.getMessage("alliance_invite_accepted")));
        }
        if(nationPlayer.isOnline()) {
            nationPlayer.getPlayer().sendMessage(M.color(M.getMessage("you_accept_alliance_invite")));
        }
        invites.remove(nation.getUUID());
        if(isCreateAlliance) {
            new Alliance(inviterNation, nation, allianceName);
        } else {
            alliance.addMember(nation);
        }
    }

    public void deny(boolean timeDeny) {
        if(!timeDeny) {
            if (inviter.isOnline()) {
                inviter.getPlayer().sendMessage(M.color(M.getMessage("alliance_invite_denied")));
            }
            if (nationPlayer.isOnline()) {
                nationPlayer.getPlayer().sendMessage(M.color(M.getMessage("you_deny_alliance_invite")));
            }
        }
        invites.remove(nation.getUUID());
    }




}
