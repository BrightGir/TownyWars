package me.bright.townywars;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.utils.invites.WarInvite;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WarTown {

    private static HashMap<UUID,WarTown> warTowns = new HashMap<>();
    private Town town;
    private War war;
    private boolean forcibly;

    public WarTown(Town town) {
        this.town = town;
        warTowns.put(town.getUUID(),this);
    }

    public static WarTown getWarTownObject(Town town) {
        UUID townUuid = town.getUUID();
        if(warTowns.get(townUuid) == null) {
            return new WarTown(town);
        }
        return warTowns.get(townUuid);
    }

    public boolean isForcibleInvite() {
        return forcibly;
    }

    public void setForcibleInvite(boolean forcibly) {
        this.forcibly = forcibly;
    }

    public void startCurrentWarPhase(War war) {
        this.war = war;


        for (Resident resident : this.getTown().getResidents()) {
            if(resident.getPlayer() != null) {
                war.getActivePhase().start(resident.getPlayer());
            }
        }
      // war.getAttackerPlayers().forEach(resident -> {
      //     if(resident.getPlayer() != null) {
      //         war.getActivePhase().start(resident.getPlayer());
      //     }
      // });

      // war.getOpponentPlayers().forEach(resident -> {
      //     if(resident.getPlayer() != null) {
      //         war.getActivePhase().start(resident.getPlayer());
      //     }
      // });

    }

    public War getWar() {
        return war;
    }

    public void endWar() {
        this.war = null;
        this.forcibly = false;
    }

    public Town getTown() {
        return town;
    }

    public boolean inWar() {
        return war != null;
    }

    public List<WarInvite> getWarInvites() {
        return WarInvite.getInvites().get(this.town.getUUID());
    }

    public static List<Town> getForcibleTownsAdded() {
        List<Town> forcibleTownsAdded = new ArrayList();

        for (WarTown warTown : warTowns.values()) {
            if(warTown.isForcibleInvite()) {
                forcibleTownsAdded.add(warTown.getTown());
            }
        }

        return forcibleTownsAdded;
    }
}
