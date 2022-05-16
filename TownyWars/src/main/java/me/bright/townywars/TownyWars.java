package me.bright.townywars;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.commands.AllianceCommand;
import me.bright.townywars.commands.ETownCommand;
import me.bright.townywars.commands.WarCommand;
import me.bright.townywars.configs.Configs;
import me.bright.townywars.listeners.WarListener;
import me.bright.townywars.listeners.JoinListener;
import me.bright.townywars.phases.Phase;
import me.bright.townywars.phases.WarPhase;
import me.bright.townywars.gui.GuiListener;
import me.bright.townywars.utils.M;
import me.bright.townywars.utils.invites.WarInvite;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class TownyWars extends JavaPlugin {


    private static TownyWars plugin;
    private Field bukkitCommandMap;
    private CommandMap commandMap;
    private BukkitTask saveRunnable;
    private static HashMap<UUID,Set<String>> slaveTowns = new HashMap<>();
    private static HashSet<UUID> forceInvitedInNationTowns = new HashSet<>();

    @Override
    public void onEnable() {
        plugin = this;
        loadCommandMap();
        loadComponents();
        loadWars();
        saveRunnable = new BukkitRunnable() {

            @Override
            public void run() {
                saveWars();
            }
        }.runTaskTimer(this,0,20L * 60L);
    }


    @Override
    public void onDisable() {
        saveRunnable.cancel();
        saveWars();
    }

    public static TownyWars getPlugin() {
        return plugin;
    }

    private void loadComponents() {
        commandMap.register("war",new WarCommand());
        commandMap.register("etown",new ETownCommand());
        commandMap.register("alliance",new AllianceCommand());
        this.getServer().getPluginManager().registerEvents(new JoinListener(),this);
        this.getServer().getPluginManager().registerEvents(new WarListener(),this);
        this.getServer().getPluginManager().registerEvents(new GuiListener(),this);
    }

    private void loadCommandMap() {
        try {
            bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private void loadWars() {
        try {
            FileConfiguration warConfig = Configs.ACTIVEWARS_CONFIG.getConfig();
            ConfigurationSection warsSection = warConfig.getConfigurationSection("wars");
            Iterator it = warsSection.getKeys(false).iterator();
            while (it.hasNext()) {
                TownyDataSource dataSource = TownyAPI.getInstance().getDataSource();
                ConfigurationSection warSection = warsSection.getConfigurationSection((String) it.next());
                try {
                    int id = Integer.parseInt(warSection.getName());
                    Town attacker = dataSource.getTown(UUID.fromString(warSection.getString("attacker")));
                    Town opponent = dataSource.getTown(UUID.fromString(warSection.getString("opponent")));
                    List<Town> attackerMembers = new ArrayList<>();
                    List<Town> opponentMembers = new ArrayList<>();
                    warSection.getList("attackerMembers").forEach(memberUUID -> {
                        try {
                            attackerMembers.add(dataSource.getTown(UUID.fromString((String) memberUUID)));
                        } catch (NotRegisteredException ex) {
                            ex.printStackTrace();
                        }
                    });
                    warSection.getList("opponentMembers").forEach(memberUUID -> {
                        try {
                            opponentMembers.add(dataSource.getTown(UUID.fromString((String) memberUUID)));
                        } catch (NotRegisteredException ex) {
                            ex.printStackTrace();
                        }
                    });
                    War war = new War(id, attacker, opponent, attackerMembers, opponentMembers);
                    if (warSection.getStringList("forceInvitedTowns") != null) {
                        List<String> forceInvitedTownsUuids = warSection.getStringList("forceInvitedTowns");
                        for (String forceInvitedTownUuid : forceInvitedTownsUuids) {
                            WarTown.getWarTownObject(dataSource.getTown(UUID.fromString(forceInvitedTownUuid))).setForcibleInvite(true);
                        }
                    }
                    String phaseClassName = warSection.getString("phase.name");
                    Phase phase = null;
                    Date endPhaseTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(warSection.getString("phase.endtime"));
                    if (!phaseClassName.contains("WarPhase")) {
                        phase = (Phase) Class.forName(phaseClassName).getConstructor(War.class, Date.class, boolean.class).newInstance(war, endPhaseTime, true);
                    } else {
                        int attackerPoints = warSection.getInt("attackerPoints");
                        int opponentPoints = warSection.getInt("opponentPoints");
                        //   M.log("(load) path == " + warsSection.getCurrentPath());
                        //   M.log("(load) attacker points == " + attackerPoints);
                        //   M.log("(load) opponent points == " + opponentPoints);
                        phase = (WarPhase) Class.forName(phaseClassName).getConstructor(War.class, Date.class, int.class, int.class).newInstance(war, endPhaseTime, attackerPoints, opponentPoints);
                    }
                    war.setPhase(phase);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (NullPointerException ex) {
            //   M.log("&cАктивных войн нет.");
        }

        try {
            FileConfiguration invitesConfig = Configs.WAR_INVITES_CONFIG.getConfig();
            ConfigurationSection invitesSection = invitesConfig.getConfigurationSection("invites");
            Iterator it = invitesSection.getKeys(false).iterator();
            while (it.hasNext()) {

                String stringId = (String) it.next();
                ConfigurationSection inviteSection = invitesSection.getConfigurationSection(stringId);
                Resident inviter = TownyUniverse.getInstance().getResident(Bukkit.getOfflinePlayer(UUID.fromString(inviteSection.getString("inviter"))).getUniqueId());
                Town town = TownyAPI.getInstance().getDataSource().getTown(UUID.fromString(inviteSection.getString("town")));
                new WarInvite(inviter, town, Integer.parseInt(stringId));

            }
        } catch (NotRegisteredException | NullPointerException e) {

        }

        try {
            FileConfiguration config = Configs.END_WAR.getConfig();
            List<UUID> uuidList = new ArrayList<>();
            config.getStringList("forcenationtowns").forEach(stringUUID -> {
                uuidList.add(UUID.fromString(stringUUID));
            });
            forceInvitedInNationTowns.addAll(uuidList);
        } catch (NullPointerException e) {

        }

        try {

            FileConfiguration config = Configs.END_WAR.getConfig();
            ConfigurationSection section = config.getConfigurationSection("slavetowns");
            for (String key : section.getKeys(false)) {
                List<String> list = (List<String>) config.getStringList("slavetowns." + key);
                Set<String> set = new HashSet<>(Collections.emptySet());
                set.addAll(list);
                slaveTowns.put(UUID.fromString(key), set);
            }
        } catch (NullPointerException e) {
        }

        try {
            FileConfiguration allyConfig = Configs.ALLIANCES.getConfig();
            ConfigurationSection section =  allyConfig.getConfigurationSection("alliances");
            Iterator it = section.getKeys(false).iterator();

            while(it.hasNext()) {
                String allianceName = (String) it.next();
                Nation headNation = TownyAPI.getInstance().getNation(UUID.fromString((String) allyConfig.get("alliances." + allianceName + ".headnation")));
                List<String> uuidMembersNation = allyConfig.getStringList("alliances." + allianceName + ".members");
                List<Nation> members = new ArrayList<>();
                for (String uuidMember : uuidMembersNation) {
                    members.add(TownyAPI.getInstance().getNation(UUID.fromString(uuidMember)));
                }
                new Alliance(headNation,members,allianceName);
            }

            for (Alliance aly : Alliance.getAlies()) {
                String path = ("alliances." + aly.getName() + ".");
                allyConfig.set(path + "headnation", aly.getHeadNation().getUUID());
                List<UUID> members = new ArrayList<>();
                for (Nation member : aly.getMembers()) {
                    members.add(member.getUUID());
                }
                allyConfig.set(path + "members", members);
            }
            Configs.ALLIANCES.save();
        } catch (NullPointerException e) {

        }
    }

    public static HashSet<UUID> getForcedInvitedInNationTowns() {
        return forceInvitedInNationTowns;
    }

    public static HashMap<UUID,Set<String>> getSlaveTowns() {
        return slaveTowns;
    }

    public static void addForceInvitedInNationTown(Town town) {
        forceInvitedInNationTowns.add(town.getUUID());
    }

    private void saveWars() {
        if(!War.getWars().isEmpty()) {
            FileConfiguration warConfig = Configs.ACTIVEWARS_CONFIG.getConfig();
            for(War war: War.getWars()) {
                String path = "wars." + war.getId();
                warConfig.set(path + ".attacker", war.getAttacker().getUUID().toString());
                warConfig.set(path + ".opponent", war.getOpponent().getUUID().toString());
                List<String> attackerMembers = new ArrayList<>();
                List<String> forciblyInvitedTowns = new ArrayList<>();
                for (Town forceTown : WarTown.getForcibleTownsAdded()) {
                    forciblyInvitedTowns.add(forceTown.getUUID().toString());
                }
                warConfig.set(path + ".forceInvitedTowns",forciblyInvitedTowns);
                war.getAttackerMembersTowns().forEach(attackMember -> {
                    attackerMembers.add(attackMember.getUUID().toString());
                });
                warConfig.set(path + ".attackerMembers", attackerMembers);
                List<String> opponentMembers = new ArrayList<>();
                war.getOpponentMembersTowns().forEach(opponentMember -> {
                    opponentMembers.add(opponentMember.getUUID().toString());
                });
                warConfig.set(path + ".opponentMembers", opponentMembers);
                String phasePath = path + ".phase";
                warConfig.set(phasePath + ".name", war.getActivePhase().getClass().getName());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                warConfig.set(phasePath + ".endtime", sdf.format(war.getActivePhase().getEndTime()));
                if(war.getActivePhase() instanceof WarPhase) {
                    M.log("(save) attacker points == " + ((WarPhase) war.getActivePhase()).getAttackerPoints());
                    M.log("(save) opponent points == " + ((WarPhase) war.getActivePhase()).getOpponentPoints());
                    warConfig.set(path + ".attackerPoints", ((WarPhase) war.getActivePhase()).getAttackerPoints());
                    warConfig.set(path + ".opponentPoints", ((WarPhase) war.getActivePhase()).getOpponentPoints());
                }


                Configs.ACTIVEWARS_CONFIG.save();
            }
        }

        if(!WarInvite.getInvites().isEmpty()) {
            FileConfiguration invitesConfig = Configs.WAR_INVITES_CONFIG.getConfig();
            WarInvite.getInvites().values().forEach(invites -> {
                if(invites != null) {
                    invites.forEach(invite -> {
                        String path = "invites." + invite.getId();
                        invitesConfig.set(path + ".inviter", invite.getInviter().getUUID().toString());
                        invitesConfig.set(path + ".town", invite.getTown().getUUID().toString());
                    });
                }
            });

            Configs.WAR_INVITES_CONFIG.save();
        }

        if(!forceInvitedInNationTowns.isEmpty()) {
            FileConfiguration endWarConfig = Configs.END_WAR.getConfig();
            List<String> uuids = new ArrayList<>();
            for (UUID uuidTown : forceInvitedInNationTowns) {
                uuids.add(uuidTown.toString());
            }
            endWarConfig.set("forcenationtowns",uuids);
            Configs.END_WAR.save();
        }

        if(!slaveTowns.isEmpty()) {
            FileConfiguration endWarConfig = Configs.END_WAR.getConfig();
            for (Map.Entry<UUID, Set<String>> entry : slaveTowns.entrySet()) {

                Set<String> strings = entry.getValue();
                List<String> names = new ArrayList<>();
                names.addAll(strings);
                endWarConfig.set("slavetowns." + String.valueOf(entry.getKey()),names);
            }


            Configs.END_WAR.save();
        }
        if(!Alliance.getAlies().isEmpty()) {
            FileConfiguration allyConfig = Configs.ALLIANCES.getConfig();
            for (Alliance aly : Alliance.getAlies()) {
                String path = ("alliances." + aly.getName() + ".");
                allyConfig.set(path + "headnation",aly.getHeadNation().getUUID().toString());
                List<String> members = new ArrayList<>();
                for (Nation member : aly.getMembers()) {
                    members.add(member.getUUID().toString());
                }
                allyConfig.set(path + "members",members);
            }
            Configs.ALLIANCES.save();
        }
    }


}
