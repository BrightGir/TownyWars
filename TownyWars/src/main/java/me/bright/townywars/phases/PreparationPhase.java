package me.bright.townywars.phases;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.bright.townywars.TownyWars;
import me.bright.townywars.War;
import me.bright.townywars.WarTown;
import me.bright.townywars.utils.M;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PreparationPhase extends Phase {

    private BossBar attackerBosBar;
    private BossBar opponentBosBar;
    private BukkitTask barUpdateTask;
    private boolean canStartWar;
    private Duration timeRemain;

    public PreparationPhase(War war, Date endWarTime, boolean config) {
        super(war,endWarTime,config);

        new BukkitRunnable() {
            @Override
            public void run() {
                int seconds = (int) timeRemain.getSeconds();
                if(seconds <= 172800) {
                    updateBarTaskToCanStartWar();
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(TownyWars.getPlugin(),0,20L * 20L);

    }


    @Override
    public String getName() {
        return "Подготовка";
    }

    private void updateBarTaskToCanStartWar() {
        barUpdateTask.cancel();
        barUpdateTask = new BukkitRunnable() {

            @Override
            public void run() {

                canStartWar = true;
                String time = getRemainingTime();
                attackerBosBar.setTitle(M.color("&fПодготовка к войне против &c" + war.getOpponent().getName() + ". &FОсталось &e" + time +
                        " &aВы можете начать войну командой &c/war start"));
                opponentBosBar.setTitle(M.color("&fПодготовка к войне против &c" + war.getAttacker().getName() + ". &FОсталось &e" + time));


            }
        }.runTaskTimerAsynchronously(TownyWars.getPlugin(), 0, 20L);
    }



    @Override
    void initializePhase() {
        attackerBosBar = Bukkit.createBossBar(M.color("&fПодготовка к войне против &c" + war.getOpponent().getName() + ". &FОсталось &e" + getRemainingTime()),
                BarColor.RED, BarStyle.SOLID);
        attackerBosBar.setVisible(true);
        opponentBosBar = Bukkit.createBossBar(M.color("&fПодготовка к войне против &c" + war.getAttacker().getName() + ". &FОсталось &e" + getRemainingTime()),
                BarColor.RED, BarStyle.SOLID);
        opponentBosBar.setVisible(true);

        barUpdateTask = new BukkitRunnable() {

            @Override
            public void run() {

                String time = getRemainingTime();
                attackerBosBar.setTitle(M.color("&fПодготовка к войне против &c" + war.getOpponent().getName() + ". &FОсталось &e" + time));
                opponentBosBar.setTitle(M.color("&fПодготовка к войне против &c" + war.getAttacker().getName() + ". &FОсталось &e" + time));

            }
        }.runTaskTimerAsynchronously(TownyWars.getPlugin(),0,20L);
    }

    @Override
    int getPhaseDuration() {
      //  return 7200;
        return 1;
    }

    @Override
    public void end(Town town) {
        town.getResidents().forEach(resident -> {
            if(resident.getPlayer() != null) {
                attackerBosBar.removePlayer(resident.getPlayer());
                opponentBosBar.removePlayer(resident.getPlayer());
            }
        });
    }

    @Override
    public void start(Player player) {
        try {
            Town pTown = TownyUniverse.getInstance().getResident(player.getUniqueId()).getTown();
            if(war.isAttacker(pTown)) {
                attackerBosBar.addPlayer(player);
            } else {
             //  opponentBosBar.getPlayers().forEach(p -> {
             //      if(!p.getName().equalsIgnoreCase(player.getName())) {
             //          opponentBosBar.addPlayer(player);
             //      }
             //  });
                opponentBosBar.addPlayer(player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCanStartWar() {
        return canStartWar;
    }

    @Override
    public void end() {
        endPhaseUpdater.cancel();
        attackerBosBar.removeAll();
        opponentBosBar.removeAll();
        this.barUpdateTask.cancel();
        Date nowDate = new Date();
        if(nowDate.after(endPhaseTime)) {
            double attackerOnline = calcPercentageOnline(war.getAttacker());
            double opponentOnline = calcPercentageOnline(war.getOpponent());
            if(attackerOnline < 25D || (attackerOnline < 25D && opponentOnline < 25D)) {
                String message = M.getMessage("war_end_force");
                message = message.replace("[attacker]",war.getAttacker().getName());
                message = message.replace("[opponent]",war.getOpponent().getName());
                Bukkit.broadcastMessage(M.color(message));
            } else if(opponentOnline < 25D) {
                String message = M.getMessage("war_end_force_win_attacker");
                message = message.replace("[attacker]",war.getAttacker().getName());
                message = message.replace("[opponent]",war.getOpponent().getName());
                Bukkit.broadcastMessage(M.color(message));
            }
            war.end();
        } else {
            war.setPhase(new WarPhase(this.war, null, false));
        }
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

    private String getRemainingTime() {
        timeRemain = Duration.between(Instant.now(), new Date(endPhaseTime.getTime()).toInstant());
        int seconds = (int) timeRemain.getSeconds()%60;
        long time = timeRemain.getSeconds()/60;
        int minutes = (int) (time%60);
        time = time/60;
        int hours = (int) (time%24);
        time = time/24;
        int days = (int) (time%24);
        time = time/30;
        int months = (int) (time%30);
        StringBuilder sb = new StringBuilder();
        if(months != 0) {
            sb.append(months + M.correct((int) months," месяц"," месяца"," месяцев") + " ");
        }
        if(!(days <= 0)) {
            sb.append(days + M.correct((int) days," день"," дня"," дней") + " ");
        }
        if(hours != 0) {
            sb.append(hours + M.correct((int) hours," час"," часа"," часов") + " ");
        }
        if(minutes != 0 && !(months > 0)) {
            sb.append(minutes + M.correct((int) minutes," минуту"," минуты"," минут") + " ");
        }
        if(!(days > 0) && !(months > 0)) {
            sb.append(seconds + M.correct((int) seconds, " секунду", " секунды", " секунд"));
        }
        return sb.toString();
    }
}
