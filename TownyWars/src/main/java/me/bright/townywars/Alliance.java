package me.bright.townywars;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.configs.Configs;

import java.util.*;

public class Alliance {

    private Nation headNation;
    private HashSet<Nation> members;
    private String name;
    private static List<Alliance> alies = new ArrayList<>();
    private static HashMap<UUID,Alliance> nationAlies = new HashMap<>();


    public Alliance(Nation creator, Nation nation, String allianceName) {
        this.headNation = creator;
        this.name = allianceName;
        this.members = new HashSet<>();
        this.members.add(nation);
        nationAlies.put(nation.getUUID(),this);
        nationAlies.put(creator.getUUID(),this);
        alies.add(this);
    }

    public Alliance(Nation headNation, List<Nation> members, String allianceName) {
        this.headNation = headNation;
        this.name = allianceName;
        this.members = new HashSet<>();
        this.members.addAll(members);
        for (Nation member : members) {
            nationAlies.put(member.getUUID(),this);
        }
        nationAlies.put(headNation.getUUID(),this);
        alies.add(this);
    }


    public static Alliance getAlliance(Nation nation) {
        return nationAlies.get(nation.getUUID());
    }

    public void addMember(Nation nation) {
        members.add(nation);
        nationAlies.put(nation.getUUID(),this);
    }

    public void kickMember(Nation nation) {
        members.remove(nation);
        nationAlies.remove(nation.getUUID());
    }

    public void setHeadNation(Nation nation) {
        members.add(headNation);
        this.headNation = nation;
    }

    public void delete() {
        alies.remove(this);
        for (Nation member : members) {
            kickMember(member);
        }
        Configs.ALLIANCES.getConfig().set("alliances." + this.name,null);
        Configs.ALLIANCES.save();
    }

    public boolean isHeadNation(Nation nation) {
        return nation.getName().equalsIgnoreCase(this.headNation.getName());
    }

    public String getName() {
        return name;
    }

    public Nation getHeadNation() {
        return headNation;
    }

    public HashSet<Nation> getMembers() {
        return members;
    }

    public static List<Alliance> getAlies() {
        return alies;
    }
}
