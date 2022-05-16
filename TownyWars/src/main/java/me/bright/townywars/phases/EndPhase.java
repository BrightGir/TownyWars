package me.bright.townywars.phases;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.War;
import me.bright.townywars.gui.EndWarMenu;
import me.bright.townywars.utils.M;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class EndPhase extends Phase {

    private Town winner;
    private Town loser;

    public EndPhase(War war, Date endPhaseTime, boolean config) {
        super(war, endPhaseTime, config);
    }

    public EndPhase(War war, Date endPhaseTime, boolean config, Town winner, Town loser) {
        super(war, endPhaseTime, config);
        this.winner = winner;
        this.loser = loser;

        String endWarMessage = M.getMessage("end_war_message");
        endWarMessage = endWarMessage.replace("[attacker]",war.getAttacker().getName());
        endWarMessage = endWarMessage.replace("[opponent]",war.getOpponent().getName());
        endWarMessage = endWarMessage.replace("[winner]",(winner == null) ? "&7Никто" : winner.getName());
        war.end();
        Bukkit.broadcastMessage(M.color(endWarMessage));
    }

    @Override
    public String getName() {
        return "Выбор исхода войны";
    }

    @Override
    void initializePhase() {

    }

    @Override
    int getPhaseDuration() {
        return 1;
        //return 5;
    }

    @Override
    public void start(Player player) {
        if(winner != null && winner.getMayor().getName().equals(player.getName())) {
            new EndWarMenu(player,war,winner,loser).open(player);
        }
    }


    @Override
    public void end() {
        if(new Date().after(endPhaseTime)) {
            war.end();
        } else {

        }
    }

    @Override
    public void end(Town town) {

    }
}
