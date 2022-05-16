package me.bright.townywars.phases;

import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.TownyWars;
import me.bright.townywars.War;
import me.bright.townywars.utils.M;
import me.bright.townywars.utils.invites.WarInvite;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class WarPhase extends Phase {

    private BossBar warBossBar;
    private BukkitTask bossBarUpdater;
    private int attackerPoints;
    private int opponentPoints;
    private Duration timeRemain;
    private Town loser;
    private Town winner;

    public WarPhase(War war, Date endWarTime, boolean config) {
        super(war,endWarTime,config);
        this.attackerPoints = 0;
        this.opponentPoints = 0;
    }

    public WarPhase(War war, Date endWarTime, int attackerPoints, int opponentPoints) {
        super(war,endWarTime,true);
        this.attackerPoints = attackerPoints;
        this.opponentPoints = opponentPoints;
    }

    @Override
    public String getName() {
        return "Война";
    }

    @Override
    void initializePhase() {
        warBossBar = Bukkit.createBossBar(M.color(war.getAttacker().getName() + " &7(&e" + attackerPoints + "&7) &c⚔ &f"
                + war.getOpponent().getName() + " &7(&e" + opponentPoints + "&7)"), BarColor.RED, BarStyle.SOLID);
        warBossBar.setVisible(true);
        bossBarUpdater = new BukkitRunnable() {
            @Override
            public void run() {
                timeRemain = Duration.between(Instant.now(), new Date(endPhaseTime.getTime()).toInstant());
                double secondsRemain = timeRemain.getSeconds();
                double fullSeconds = WarPhase.this.getPhaseDuration()*60;
                double percentage = (secondsRemain/fullSeconds);
                try {
                    warBossBar.setProgress(percentage);
                    warBossBar.setTitle(M.color(war.getAttacker().getName() + " &7(&e" + attackerPoints + "&7) &c⚔ &f"
                            + war.getOpponent().getName() + " &7(&e" + opponentPoints + "&7)"));
                } catch (IllegalArgumentException | ConcurrentModificationException e) {

                }
            }
        }.runTaskTimerAsynchronously(TownyWars.getPlugin(),0,20L);
    }

    @Override
    int getPhaseDuration() {
        // return 60;
        return 3;
    }

    public int getAttackerPoints() {
        return attackerPoints;
    }

    public int getOpponentPoints() {
        return opponentPoints;
    }

    @Override
    public void start(Player player) {
        warBossBar.addPlayer(player);
        war.getAttacker().setPVP(true);
        war.getOpponent().setPVP(true);
    }

    @Override
    public void end(Town town) {

    }

    public void addPoints(Town town, int points) {
        war.getAttackerMembersTowns().forEach(attackerTown -> {
            if(town.getUUID().equals(attackerTown.getUUID())) {
                attackerPoints = attackerPoints + points;
                return;
            }
        });
        war.getOpponentMembersTowns().forEach(opponentTown -> {
            if(town.getUUID().equals(opponentTown.getUUID())) {
                opponentPoints = opponentPoints + points;
                return;
            }
        });
    }

    public void removePoints(Town town, int points) {
        war.getAttackerMembersTowns().forEach(attackerTown -> {
            if(town.getUUID().equals(attackerTown.getUUID())) {
                int finalPoints = attackerPoints - points;
                attackerPoints = Math.max(finalPoints, 0);
                return;
            }
        });
        war.getOpponentMembersTowns().forEach(opponentTown -> {
            if(town.getUUID().equals(opponentTown.getUUID())) {
                int finalPoints = opponentPoints - points;
                opponentPoints = Math.max(finalPoints, 0);
                return;
            }
        });


    }

    @Override
    public void end() {
        war.getAttacker().setPVP(false);
        war.getOpponent().setPVP(false);
        bossBarUpdater.cancel();
        warBossBar.removeAll();
        warBossBar.setVisible(false);
        setWinnerAndLoser();
        war.setPhase(new EndPhase(this.war, null, false,this.winner,this.loser));
    }

    private void setWinnerAndLoser() {
        if(getAttackerPoints() > getOpponentPoints()) {
            this.winner = war.getAttacker();
            this.loser = war.getOpponent();
        } else if(getOpponentPoints() > getAttackerPoints()) {
            this.winner = war.getOpponent();
            this.loser = war.getAttacker();
        } else {

        }
    }
}
