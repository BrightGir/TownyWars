package me.bright.townywars.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.Alliance;
import me.bright.townywars.War;
import me.bright.townywars.WarTown;
import me.bright.townywars.gui.InvitesListGui;
import me.bright.townywars.phases.PreparationPhase;
import me.bright.townywars.phases.WarPhase;
import me.bright.townywars.utils.M;
import me.bright.townywars.gui.WarListGui;
import me.bright.townywars.utils.invites.WarInvite;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarCommand extends Command {
    public WarCommand() {
        super("war");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if(args.length == 2) {

            if(args[0].equalsIgnoreCase("declare")) {

                Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());

                if (!resident.hasTown()) {
                    M.msg(resident.getPlayer(), M.getMessage("havenot_town"));
                    return true;
                }

                if (!resident.isMayor()) {
                    M.msg(resident.getPlayer(), M.getMessage("not_mayor"));
                    return true;
                }

                Town attackerTown = resident.getTownOrNull();
                Town opponentTown = null;
                try {
                    opponentTown = TownyAPI.getInstance().getDataSource().getTown(args[1]);
                } catch (NotRegisteredException e) {
                    M.msg(sender, M.getMessage("town_not_exist"));
                    return true;
                }

                if(opponentTown.getUUID().equals(attackerTown.getUUID())) {
                    M.msg(sender, M.getMessage("your_town"));
                    return true;
                }

                if(checkIfTownInWar(opponentTown,sender,M.getMessage("opponent_in_war"),null)) {
                    return true;
                };

                new War(attackerTown,opponentTown);
                String warDeclareMessage = M.getMessage("war_declare").replace("[opponent]",opponentTown.getName());
                Bukkit.broadcastMessage(M.color(warDeclareMessage.replace("[attacker]",attackerTown.getName())));
                return true;
            }


            if(args[0].equalsIgnoreCase("invite")) {
                if(args[1].equalsIgnoreCase("nation")) {

                    Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                    if(!canRunCommand(resident)) {
                        return true;
                    }

                    WarTown town = WarTown.getWarTownObject(resident.getTownOrNull());

                    if(!isGeneralTownOfWar(town)) {
                        M.msg(sender, M.getMessage("not_general_town"));
                        return true;
                    }

                    if(!resident.hasNation()) {
                        M.msg(sender, M.getMessage("not_nation"));
                        return true;
                    }

                    if(!resident.getNationOrNull().isCapital(town.getTown())) {
                        M.msg(sender, M.getMessage("town_not_capital_nation"));
                        return true;
                    }

                    resident.getNationOrNull().getTowns().forEach(nationTown -> {
                        town.getWar().addMember(nationTown, town.getTown());
                        WarTown.getWarTownObject(nationTown).setForcibleInvite(true);
                    });
                    M.msg(sender, M.getMessage("nation_towns_invited"));
                    return true;
                }

                if(args[1].equalsIgnoreCase("alliance")) {

                    Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                    if (!resident.hasTown()) {
                        M.msg(resident.getPlayer(), M.getMessage("havenot_town"));
                        return false;
                    }

                    if(!WarTown.getWarTownObject(resident.getTownOrNull()).inWar()) {
                        M.msg(resident.getPlayer(),M.getMessage("you_not_in_war"));
                        return false;
                    }

                    if(!resident.hasNation()) {
                        M.msg(sender, M.getMessage("not_nation"));
                        return true;
                    }

                    WarTown town = WarTown.getWarTownObject(resident.getTownOrNull());
                    Alliance alliance = Alliance.getAlliance(resident.getNationOrNull());
                    if(alliance == null) {
                        M.msg(player,M.getMessage("not_alliance"));
                        return true;
                    }

                    if(!alliance.isHeadNation(resident.getNationOrNull())) {
                        M.msg(player,M.getMessage("not_head_of_alliance"));
                        return true;
                    }

                    alliance.getMembers().forEach(nation -> {
                        town.getWar().addMember(nation.getCapital(),town.getTown());
                        WarTown.getWarTownObject(nation.getCapital()).setForcibleInvite(true);
                    });

                    M.msg(sender, M.getMessage("nation_alliances_invited"));
                    return true;
                }
            }
        }

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("start")) {
                Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                if (!resident.hasTown()) {
                    M.msg(sender, M.getMessage("havenot_town"));
                    return true;
                }
                if (!resident.isMayor()) {
                    M.msg(sender, M.getMessage("not_mayor"));
                    return true;
                }
                WarTown attackerTown = null;
                WarTown opponentTown = null;
                try {
                    attackerTown = WarTown.getWarTownObject(resident.getTown());
                    opponentTown = WarTown.getWarTownObject(attackerTown.getWar().getOpponent());
                } catch (NotRegisteredException e) {
                    M.msg(player,M.getMessage("error"));
                    return true;
                }

                if(!attackerTown.inWar() || !(attackerTown.getWar().getActivePhase() instanceof PreparationPhase) ||
                        !((PreparationPhase) attackerTown.getWar().getActivePhase()).isCanStartWar()) {
                    M.msg(player,M.getMessage("cant_start_war"));
                    return true;
                }
                if(calcPercentageOnline(attackerTown.getTown()) < 25D) {
                    M.msg(player,M.getMessage("attacker_low_online"));
                    return true;
                }

                if(calcPercentageOnline(opponentTown.getTown()) < 25D) {
                    M.msg(player,M.getMessage("opponent_low_online"));
                    return true;
                }

                if(!opponentTown.getTown().getMayor().isOnline()) {
                    M.msg(player,M.getMessage("opponent_mayor_offline"));
                    return true;
                }

                attackerTown.getWar().getActivePhase().end();
                M.msg(player,M.getMessage("start_war"));
                return true;
            }

            if(args[0].equalsIgnoreCase("leave")) {
                Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                if(!canRunCommand(resident)) {
                    return true;
                }
                WarTown town = WarTown.getWarTownObject(resident.getTownOrNull());
                if(town.getWar().getActivePhase() instanceof WarPhase) {
                    M.msg(player,M.getMessage("cant_leave_in_warphase"));
                    return true;
                }
                UUID townUuid = town.getTown().getUUID();
                if(townUuid.equals(town.getWar().getAttacker().getUUID()) || townUuid.equals(town.getWar().getOpponent().getUUID())) {
                    M.msg(player,M.getMessage("cant_leave_as_general_town"));
                    return true;
                }
                town.getWar().end(town.getTown());
                M.msg(player,M.getMessage("leave_war"));
                return true;
            }

            if(args[0].equalsIgnoreCase("list")) {
                new WarListGui(player,1).open(player);
                return true;
            }

            if(args[0].equalsIgnoreCase("invites")) {
                Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                if (!resident.hasTown()) {
                    M.msg(sender, M.getMessage("havenot_town"));
                    return true;
                }
                if (!resident.isMayor()) {
                    M.msg(sender, M.getMessage("not_mayor"));
                    return true;
                }
                Town residentTown = null;
               try {
                   residentTown = resident.getTown();
                   new InvitesListGui(player, residentTown, 1).open(player);
               } catch (NotRegisteredException e) {
                   M.msg(player,M.getMessage("error"));
                   return true;
               }
                return true;
            }
        }

        if(args.length == 3) {
            if(args[0].equalsIgnoreCase("invite")) {

                Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                if(args[1].equalsIgnoreCase("town")) {

                    if(!canRunCommand(resident)) {
                        return true;
                    }

                    Town inviteTown = null;
                    try {
                        inviteTown = TownyAPI.getInstance().getDataSource().getTown(args[2]);
                    } catch (NotRegisteredException e) {

                    }
                    if(inviteTown == null) {
                        M.msg(sender, M.getMessage("town_not_exist"));
                        return true;
                    }

                    new WarInvite(resident,inviteTown);
                    M.msg(player,M.getMessage("invite_sent").replace("[town]",inviteTown.getName()));
                    return true;
                }
            }
        }
        sendHelpMessage((Player) sender);
        return true;
    }

    public boolean canRunCommand(Resident resident) {
        if (!resident.hasTown()) {
            M.msg(resident.getPlayer(), M.getMessage("havenot_town"));
            return false;
        }

        if(!WarTown.getWarTownObject(resident.getTownOrNull()).inWar()) {
            M.msg(resident.getPlayer(),M.getMessage("you_not_in_war"));
            return false;
        }

        if (!resident.isMayor()) {
            M.msg(resident.getPlayer(), M.getMessage("not_mayor"));
            return false;
        }
        return true;
    }

    public boolean isGeneralTownOfWar(WarTown town) {
        War war = town.getWar();
        if(war.getAttacker().getUUID().equals(town.getTown().getUUID()) || war.getOpponent().getUUID().equals(town.getTown().getUUID())) {
            return true;
        }
        return false;
    }

    public boolean checkIfTownInWar(Town town, CommandSender sender, String messageIfIn, String messageIfNot) {
        boolean inWar = WarTown.getWarTownObject(town).inWar();
        if(inWar && messageIfIn != null) {
            M.msg(sender, messageIfIn);
        }
        if(!inWar && messageIfNot != null) {
            M.msg(sender, messageIfNot);
        }
        return inWar;
    }


    private void sendHelpMessage(Player player) {
        M.msg(player, "&7----------&4WARS&7----------");
        M.msg(player, "&c/war declare <город> &7- объявить войну городу");
        M.msg(player, "&c/war start &7- начать войну");
        M.msg(player, "&c/war list &7- посмотреть список войн");
        M.msg(player, "&c/war invite town <город> &7- пригласить город участвовать в войне");
        M.msg(player, "&c/war invite nation &7- призвать города нации в войну");
        M.msg(player, "&c/war invite alliance &7- призвать столицы наций в альянсе");
        M.msg(player, "&c/war invites &7- посмотреть приглашения на войну");
        M.msg(player, "&c/war leave &7- покинуть войну");
        M.msg(player, "&c/etown <город> command &7- управлять городом-марионеткой");
        M.msg(player, "&c/alliance &7- команды альянсов");
    }

    private double calcPercentageOnline(Town town) {
        List<String> onlinePlayersList = new ArrayList<>();
        for (Resident resident : town.getResidents()) {
            if(resident.getPlayer() != null) {
                onlinePlayersList.add(resident.getPlayer().getName());
            }
        }

        try {
            return (onlinePlayersList.size() / town.getResidents().size()) * 100;
        } catch (ArithmeticException e) {
            return 0;
        }
    }
}
