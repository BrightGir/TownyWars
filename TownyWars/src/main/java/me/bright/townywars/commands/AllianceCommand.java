package me.bright.townywars.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import me.bright.townywars.Alliance;
import me.bright.townywars.utils.M;
import me.bright.townywars.utils.invites.AllianceInvite;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class AllianceCommand extends Command {
    public AllianceCommand() {
        super("alliance", "alliance command", "/alliance", Arrays.asList("ally"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
        if (resident == null) {
            M.msg(player, M.getMessage("error"));
            return true;
        }
        if(args.length == 3) {
            if(args[0].equalsIgnoreCase("create")) {
                String nationName = args[1];
                Nation nation = TownyAPI.getInstance().getNation(nationName);
                if(nation == null) {
                    M.msg(player,M.getMessage("nation_not_exists"));
                    return true;
                }
                String allianceName = args[2];
                if(!allianceName.matches("[a-zA-Z]+")) {
                    M.msg(player,M.color("Имя альянса должно содержать только буквы английсского алфавита"));
                    return true;
                }
                if(resident.getNationOrNull() == null) {
                    M.msg(player,M.getMessage("not_nation"));
                    return true;
                }
                if(Alliance.getAlliance(resident.getNationOrNull()) != null) {
                    M.msg(player,M.getMessage("you_in_alliance"));
                    return true;
                }
                if(!resident.getNationOrNull().isKing(resident)) {
                    M.msg(player,M.getMessage("not_king_of_nation"));
                    return true;
                }
                if(AllianceInvite.getInvites().get(nation.getUUID()) != null) {
                    M.msg(player,M.getMessage("nation_has_invite"));
                    return true;
                }
                if(nation.getName().equalsIgnoreCase(resident.getNationOrNull().getName())) {
                    return true;
                }
                if(!nation.getKing().isOnline()) {
                    M.msg(player,M.getMessage("king_of_nation_offline"));
                    return true;
                }
                AllianceInvite creatorNationInvite = AllianceInvite.getInvites().get(resident.getNationOrNull().getUUID());
                if(creatorNationInvite != null) {
                    creatorNationInvite.delete();
                }
                new AllianceInvite(allianceName,resident,nation);
                String inviteSend1 = M.getMessage("invite_send_1");
                inviteSend1 = inviteSend1.replace("[allyName]",allianceName);
                inviteSend1 = inviteSend1.replace("[nationName]",resident.getNationOrNull().getName());
                String inviteSend3 = M.getMessage("invite_send_3");
                String inviteSend4 = M.getMessage("invite_send_4");
                M.msg(nation.getKing().getPlayer(),inviteSend1);
                M.msg(nation.getKing().getPlayer(),M.getMessage("invite_send_2"));
                M.msg(nation.getKing().getPlayer(),inviteSend3);
                M.msg(nation.getKing().getPlayer(),inviteSend4);

                M.msg(player,M.getMessage("you_sent_alliance_invite"));
                return true;
            }

        }

        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("query")) {
                if(resident.getNationOrNull() == null) {
                    M.msg(player,M.getMessage("not_nation"));
                    return true;
                }
                AllianceInvite invite = AllianceInvite.getInvites().get(resident.getNationOrNull().getUUID());
                if(!resident.getNationOrNull().isKing(resident)) {
                    M.msg(player,M.getMessage("not_king_of_nation"));
                    return true;
                }
                if(invite == null) {
                    M.msg(player,M.getMessage("you_havent_alliance_invites"));
                    return true;
                }
                if(args[1].equalsIgnoreCase("accept")) {
                    invite.accept();
                    return true;
                }

                if(args[1].equalsIgnoreCase("deny")) {
                    invite.deny(false);
                    return true;
                }
            }
            if(args[0].equalsIgnoreCase("head")) {
                if(resident.getNationOrNull() == null) {
                    M.msg(player,M.getMessage("not_nation"));
                    return true;
                }
                Alliance alliance = Alliance.getAlliance(resident.getNationOrNull());
                if(alliance == null) {
                    M.msg(player,M.getMessage("not_alliance"));
                    return true;
                }
                if(!alliance.isHeadNation(resident.getNationOrNull())) {
                    M.msg(player,M.getMessage("not_head_of_alliance"));
                    return true;
                }
                if(!resident.getNationOrNull().isKing(resident)) {
                    M.msg(player,M.getMessage("not_king_of_nation"));
                    return true;
                }
                Nation willHeadNation = TownyAPI.getInstance().getNation(args[1]);
                if(willHeadNation == null) {
                    M.msg(player,M.getMessage("nation_not_exists"));
                    return true;
                }
                if(!alliance.getMembers().contains(willHeadNation)) {
                    M.msg(player,M.getMessage("nation_not_in_alliance"));
                    return true;
                }
                if(alliance.getHeadNation().getName().equalsIgnoreCase(willHeadNation.getName())) {
                    return true;
                }
                alliance.setHeadNation(willHeadNation);
                String message = M.getMessage("you_set_head_nation");
                message = message.replace("[nationName]",willHeadNation.getName());
                M.msg(player,M.color(message));
                return true;
            }
            if(args[0].equalsIgnoreCase("add")) {
                if(resident.getNationOrNull() == null) {
                    M.msg(player,M.getMessage("not_nation"));
                    return true;
                }
                Alliance alliance = Alliance.getAlliance(resident.getNationOrNull());
                if(alliance == null) {
                    M.msg(player,M.getMessage("not_alliance"));
                    return true;
                }
                if(!alliance.isHeadNation(resident.getNationOrNull())) {
                    M.msg(player,M.getMessage("not_head_of_alliance"));
                    return true;
                }
                if(!resident.getNationOrNull().isKing(resident)) {
                    M.msg(player,M.getMessage("not_king_of_nation"));
                    return true;
                }
                Nation willNation = TownyAPI.getInstance().getNation(args[1]);
                if(willNation == null) {
                    M.msg(player,M.getMessage("nation_not_exists"));
                    return true;
                }
                if(Alliance.getAlliance(willNation) != null) {
                    if(alliance.getMembers().contains(willNation)) {
                        M.msg(player,M.getMessage("nation_already_in_your_alliance"));
                        return true;
                    }
                    M.msg(player,M.getMessage("nation_already_in_alliance"));
                    return true;
                }
                if(!willNation.getKing().isOnline()) {
                    M.msg(player,M.getMessage("king_of_nation_offline"));
                    return true;
                }
                new AllianceInvite(resident,alliance,willNation);
                String inviteSend1 = M.getMessage("invite_send_11");
                inviteSend1 = inviteSend1.replace("[allyName]",alliance.getName());
                inviteSend1 = inviteSend1.replace("[nationName]",resident.getNationOrNull().getName());
                String inviteSend3 = M.getMessage("invite_send_3");
                String inviteSend4 = M.getMessage("invite_send_4");
                M.msg(willNation.getKing().getPlayer(),inviteSend1);
                M.msg(willNation.getKing().getPlayer(),M.getMessage("invite_send_2"));
                M.msg(willNation.getKing().getPlayer(),inviteSend3);
                M.msg(willNation.getKing().getPlayer(),inviteSend4);

                M.msg(player,M.getMessage("you_sent_alliance_invite"));
                return true;
            }
            if(args[0].equalsIgnoreCase("kick")) {
                if(resident.getNationOrNull() == null) {
                    M.msg(player,M.getMessage("not_nation"));
                    return true;
                }
                Alliance alliance = Alliance.getAlliance(resident.getNationOrNull());
                if(alliance == null) {
                    M.msg(player,M.getMessage("not_alliance"));
                    return true;
                }
                if(!alliance.isHeadNation(resident.getNationOrNull())) {
                    M.msg(player,M.getMessage("not_head_of_alliance"));
                    return true;
                }
                if(!resident.getNationOrNull().isKing(resident)) {
                    M.msg(player,M.getMessage("not_king_of_nation"));
                    return true;
                }
                Nation willNation = TownyAPI.getInstance().getNation(args[1]);
                if(willNation == null) {
                    M.msg(player,M.getMessage("nation_not_exists"));
                    return true;
                }
                Nation kickNation = TownyAPI.getInstance().getNation(args[1]);
                if(kickNation == null) {
                    M.msg(player,M.getMessage("nation_not_exists"));
                    return true;
                }
                if(!alliance.getMembers().contains(kickNation)) {
                    M.msg(player,M.getMessage("nation_not_in_alliance"));
                    return true;
                }
                alliance.kickMember(kickNation);
                String message = M.getMessage("you_kick_nation_out_of_alliance");
                message = message.replace("[nationName]",willNation.getName());
                M.msg(player,message);
                if(alliance.getMembers().size() <= 1) {
                    M.log("size " + alliance.getMembers().size());
                    alliance.delete();
                }
                return true;
            }
        }
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("leave")) {
                if (resident.getNationOrNull() == null) {
                    M.msg(player, M.getMessage("not_nation"));
                    return true;
                }
                Alliance alliance = Alliance.getAlliance(resident.getNationOrNull());
                if (alliance == null) {
                    M.msg(player, M.getMessage("not_alliance"));
                    return true;
                }
                if (!resident.getNationOrNull().isKing(resident)) {
                    M.msg(player, M.getMessage("not_king_of_nation"));
                    return true;
                }
                if (alliance.isHeadNation(resident.getNationOrNull())) {
                    M.msg(player, M.getMessage("you_head_of_alliance"));
                    return true;
                }
                alliance.kickMember(resident.getNationOrNull());
                M.msg(player, M.getMessage("you_left_in_alliance"));
                if (alliance.getMembers().size() <= 1) {
                    alliance.delete();
                }
                return true;
            }
        }
        sendHelpMessage(player);

        return false;
    }


    private void sendHelpMessage(Player player) {
        M.msg(player, "&7----------&4ALLIANCES&7----------");
        M.msg(player, "&4/alliance create <название нации> <название альянса> &7- отправить запрос на создание альянса");
        M.msg(player, "&4/alliance head <название нации> &7- передать руководство альянсом");
        M.msg(player, "&4/alliance query accept &7- принять запрос на создание альянса");
        M.msg(player, "&4/alliance query deny &7- отклонить запрос на создание альянса");
        M.msg(player, "&4/alliance add <название нации> &7- отправить запрос на вступление нации в альянс");
        M.msg(player, "&4/alliance kick <название нации> &7- исключить нацию из альянса");
        M.msg(player, "&4/alliance leave &7- покинуть альянс");
    }
}
