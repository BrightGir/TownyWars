package me.bright.townywars.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.command.TownCommand;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.TownyWars;
import me.bright.townywars.utils.M;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class ETownCommand extends Command {
    public ETownCommand() {
        super("etown");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        if(args.length < 2) {
            M.msg(player,"&cИспользование команды: /etown <townname> ...");
            M.msg(player,"&cПример: /etown townname toggle open");
            return true;
        }

        Set<String> slaveTowns = TownyWars.getSlaveTowns().get(player.getUniqueId());

        if(slaveTowns == null || slaveTowns.isEmpty()) {
            M.msg(player,M.getMessage("havent_enslaved_towns"));
            return true;
        }

        for (String slaveTown : slaveTowns) {
            Town town = null;
            try {
                town = TownyAPI.getInstance().getDataSource().getTown(slaveTown);
            } catch (NotRegisteredException e) {
                continue;
            }

            if(town.getName().equalsIgnoreCase(args[0])) {
                String command = "";
                for (String arg : args) {
                    command = command + arg + " ";
                }
                command = command.replace(town.getName(),"town");
                command = command.replace(town.getName(),"");
                Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                Town winTown = resident.getTownOrNull();
                if (winTown == null || !winTown.getMayor().getName().equals(player.getName())) {
                    return true;
                }
                try {
                    resident.removeTown();
                    TownCommand.townAddResident(town, resident);
                    town.forceSetMayor(resident);
                    Bukkit.dispatchCommand(player, command);
                    town.removeTrustedResident(resident);
                    resident.setTown(null);
                    if(command.contains("delete")) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    TownCommand.townAddResident(winTown, resident);
                                    winTown.forceSetMayor(resident);
                                } catch (TownyException e) {

                                }
                            }
                        }.runTaskLater(TownyWars.getPlugin(),20L * 21L);
                    } else {
                        TownCommand.townAddResident(winTown, resident);
                        winTown.forceSetMayor(resident);
                    }
                    return true;
                } catch (TownyException e) {
                    e.printStackTrace();
                }
            }

        }
        M.msg(player,M.getMessage("not_enslaved_town"));
        return true;
    }
}
