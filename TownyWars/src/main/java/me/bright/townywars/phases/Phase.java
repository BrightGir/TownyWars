package me.bright.townywars.phases;

import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.TownyWars;
import me.bright.townywars.War;
import me.bright.townywars.utils.M;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Date;

public abstract class Phase {

    protected Date endPhaseTime;
    protected War war;
    protected BukkitTask endPhaseUpdater;

    public Phase(War war, Date endPhaseTime, boolean config) {
        this.war = war;
        if(config) {
            this.endPhaseTime = endPhaseTime;
        } else {
            this.endPhaseTime = new Date(System.currentTimeMillis() + getPhaseDuration() * 60L * 1000L);
        }
        endPhaseUpdater = new BukkitRunnable() {

            @Override
            public void run() {
                if(new Date().after(Phase.this.endPhaseTime)) {
                    end();
                    this.cancel();
                }
            }

        }.runTaskTimer(TownyWars.getPlugin(),0,20L);
        initializePhase();
    }

    public abstract void end(Town town);

    public abstract String getName();

    public Date getEndTime() {
        return endPhaseTime;
    }

    abstract void initializePhase();

    // В минутах
    abstract int getPhaseDuration();

    public abstract void start(Player player);

    public abstract void end();
}
