package me.bright.townywars.listeners;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import me.bright.townywars.War;
import me.bright.townywars.utils.M;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) throws NotRegisteredException {
        Player player = event.getPlayer();
        Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
        if(resident == null || !resident.hasTown()) {
            return;
        }
        War war = War.getWar(resident.getTown());
        if(war != null) {
            war.getActivePhase().start(player);
        }
    }


}
