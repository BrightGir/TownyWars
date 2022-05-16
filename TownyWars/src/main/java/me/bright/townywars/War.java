package me.bright.townywars;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.configs.Configs;
import me.bright.townywars.phases.Phase;
import me.bright.townywars.phases.PreparationPhase;
import me.bright.townywars.phases.WarPhase;
import me.bright.townywars.utils.M;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class War {

    private int id;
    private static HashMap<UUID, War> townsAndWars = new HashMap<>();
    private static List<War> wars = new ArrayList<>();
    private Town attacker;
    private Town opponent;
    private List<Town> attackerMembersTowns = new ArrayList<>();
    private List<Town> opponentMembersTowns = new ArrayList<>();
    private List<Resident> attackerMembers = new ArrayList<>();
    private List<Resident> opponentMembers = new ArrayList<>();
    private Phase activePhase;
    private boolean isEnd;

    public War(Town attacker, Town opponent) {
        M.log("new war, (not config)");
        this.id = getAviableId();
        wars.add(this);
        townsAndWars.put(attacker.getUUID(),this);
        townsAndWars.put(opponent.getUUID(),this);
        this.attacker = attacker;
        this.opponent = opponent;
        activePhase = new PreparationPhase(this,null,false);
        attackerMembersTowns.add(attacker);
        opponentMembersTowns.add(opponent);
        startWarInWarTowns();
    }

    public War(int id, Town attackerTown, Town opponentTown, List<Town> attackerMembersTowns, List<Town> opponentMembersTowns) {
        M.log("new war, (config)");
        this.id = id;
        this.attacker = attackerTown;
        this.opponent = opponentTown;
        this.attackerMembersTowns = attackerMembersTowns;
        this.opponentMembersTowns = opponentMembersTowns;
        townsAndWars.put(attackerTown.getUUID(),this);
        townsAndWars.put(opponentTown.getUUID(),this);
        startWarInWarTowns();
        wars.add(this);
    }

    private void startWarInWarTowns() {
        try {
            for (Town attackerTown : this.getAttackerMembersTowns()) {
                attackerMembers.addAll(attackerTown.getResidents());
                WarTown.getWarTownObject(attackerTown).startCurrentWarPhase(this);
            }
            for (Town opponentTown : this.getOpponentMembersTowns()) {
                opponentMembers.addAll(opponentTown.getResidents());
                WarTown.getWarTownObject(opponentTown).startCurrentWarPhase(this);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setPhase(Phase phase) {
        this.activePhase = phase;
        for (Town town : this.getAttackerMembersTowns()) {
            WarTown.getWarTownObject(town).startCurrentWarPhase(this);
        }
        for (Town town : this.getOpponentMembersTowns()) {
            WarTown.getWarTownObject(town).startCurrentWarPhase(this);
        }
    }

    public void addMember(Town addedTown, Town allyTown) {
        String allyTownUuid = allyTown.getUUID().toString();
        if(allyTownUuid.equals(attacker.getUUID().toString())) {
            attackerMembersTowns.add(addedTown);
        } else if(allyTownUuid.equals(opponent.getUUID().toString())) {
            opponentMembersTowns.add(addedTown);
        }
        WarTown.getWarTownObject(addedTown).startCurrentWarPhase(this);
    }

    public Town getOpponent(Town town) {
        String townUUID = town.getUUID().toString();
        if(townUUID.equals(attacker.getUUID().toString())) {
            return opponent;
        } else if (townUUID.equals(opponent.getUUID().toString())) {
            return attacker;
        }
        return null;
    }

    public List<Town> getOpponents(Town town) {
        String townUUID = town.getUUID().toString();
        if(townUUID.equals(attacker.getUUID().toString())) {
            return opponentMembersTowns;
        }
        return attackerMembersTowns;
    }

    public void end() {
        this.isEnd = true;
        try {
            for (Town town : this.getAttackerMembersTowns()) {
                WarTown.getWarTownObject(town).endWar();
                townsAndWars.remove(town.getUUID());
            }
            for (Town town : this.getOpponentMembersTowns()) {
                WarTown.getWarTownObject(town).endWar();
                townsAndWars.remove(town.getUUID());
            }
        } catch (ConcurrentModificationException | NoSuchElementException e) {
            e.printStackTrace();
        }
        wars.remove(this);
        Configs.ACTIVEWARS_CONFIG.getConfig().set("wars." + this.getId(),null);
        Configs.ACTIVEWARS_CONFIG.save();
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void end(Town town) {
        if(opponentMembersTowns.contains(town)) {
            opponentMembersTowns.remove(town);
            opponentMembers.removeAll(town.getResidents());
        }
        if(attackerMembersTowns.contains(town)) {
            attackerMembersTowns.remove(town);
            attackerMembers.removeAll(town.getResidents());
        }
        townsAndWars.remove(town.getUUID());
        WarTown.getWarTownObject(town).endWar();
        this.getActivePhase().end();
    }

    public Town getOpponent() {
        return opponent;
    }

    public Town getAttacker() {
        return attacker;
    }

    public List<Town> getAttackerMembersTowns() {
        return attackerMembersTowns;
    }

    public List<Town> getOpponentMembersTowns() {
        return opponentMembersTowns;
    }

    public List<Resident> getOpponentPlayers() {
        return opponentMembers;
    }

    public List<Resident> getAttackerPlayers() {
        return attackerMembers;
    }


    public boolean isAttacker(Town town) {
        return town.getUUID().toString().equalsIgnoreCase(attacker.getUUID().toString());
    }

    public Phase getActivePhase() {
        // M.log("activePhase = " + activePhase.getClass().getName());
        return activePhase;
    }

    public static boolean hasWar(Town town) {
        if(townsAndWars.get(town.getUUID()) != null) {
            return true;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    private int getAviableId() {
        int id = 1;
        for(War war: wars) {
            if(war.getId() == id) {
                id++;
            } else {
                return id;
            }
        }
        return id;
    }

    public static War getWar(Town town) {
        return townsAndWars.get(town.getUUID());
    }

    public static Collection<War> getWars() {
        return wars;
    }




}
