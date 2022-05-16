package me.bright.townywars.utils.invites;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.War;
import me.bright.townywars.WarTown;
import me.bright.townywars.utils.M;
import org.bukkit.entity.Player;

import java.util.*;

public class WarInvite {

    private static HashMap<UUID, ArrayList<WarInvite>> invites = new HashMap<>();
    private Town town;
    private Resident inviter;
    private int id;

    public WarInvite(Resident inviter, Town town) {
        this.inviter = inviter;
        this.town = town;
        this.id = getAvailableId();
        ArrayList<WarInvite> townInvites = this.invites.get(town.getUUID());
        this.invites.put(town.getUUID(), (townInvites == null || townInvites.isEmpty())
                ? createNewList(this) : addAndReturnList(townInvites,this));
    }

    public WarInvite(Resident inviter, Town town, int id) {
        this.inviter = inviter;
        this.town = town;
        this.id = id;
        ArrayList<WarInvite> townInvites = this.invites.get(town.getUUID());
        this.invites.put(town.getUUID(), (townInvites == null || townInvites.isEmpty())
                ? createNewList(this) : addAndReturnList(townInvites,this));
    }

    private ArrayList<WarInvite> addAndReturnList(ArrayList<WarInvite> list, WarInvite newInvite) {
        list.add(newInvite);
        return list;
    }

    private ArrayList<WarInvite> createNewList(WarInvite invite) {
        ArrayList<WarInvite> list = new ArrayList<>();
        list.add(invite);
        return list;
    }

    public void accept(Player accepter) {
        try {
            WarTown.getWarTownObject(inviter.getTown()).getWar().addMember(town, inviter.getTown());
            M.msg(accepter,M.getMessage("accept_invite"));
        } catch (NotRegisteredException e) {
            M.msg(accepter,M.getMessage("error"));
        }
        invites.get(town.getUUID()).remove(this);
    }

    public void deny(Player denier) {
        M.msg(denier,M.getMessage("deny_invite"));
        invites.get(town.getUUID()).remove(this);
    }

    public void delete() {
        invites.get(town.getUUID()).remove(this);
    }

    private int getAvailableId() {
        int id = 1;
        for(ArrayList<WarInvite> inviteList: getInvites().values()) {
            for(WarInvite invite: inviteList) {
                if(id == invite.getId()) {
                    id++;
                } else {
                    return id;
                }
            }
        }
        return id;
    }

    public static HashMap<UUID,ArrayList<WarInvite>> getInvites() {
        return invites;
    }

    public int getId() {
        return id;
    }

    public Resident getInviter() {
        return inviter;
    }

    public Town getTown() {
        return town;
    }
}
