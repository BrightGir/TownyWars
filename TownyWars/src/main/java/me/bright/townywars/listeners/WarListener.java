package me.bright.townywars.listeners;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.event.nation.NationTownLeaveEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.EmptyTownException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import me.bright.townywars.TownyWars;
import me.bright.townywars.War;
import me.bright.townywars.WarTown;
import me.bright.townywars.configs.Configs;
import me.bright.townywars.phases.PreparationPhase;
import me.bright.townywars.phases.WarPhase;
import me.bright.townywars.utils.M;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class WarListener implements Listener {

    private HashSet<String> denyWarStrings = new HashSet<>();
    private HashSet<String> denyPreparationStrings = new HashSet<>();
    private HashSet<String> mayorCommands = new HashSet<>();


    @EventHandler
    public void onTowny(TownyBuildEvent event) {
        if(event.getBlock().getBlockData().getMaterial() == Material.RED_BANNER) {

            Resident resident = TownyUniverse.getInstance().getResident(event.getPlayer().getUniqueId());
            if(!resident.hasTown()) {
                return;
            }
            if(!WarTown.getWarTownObject(resident.getTownOrNull()).inWar()) {
                return;
            }

            if(event.getTownBlock() != null && event.getTownBlock().hasTown()) {

                Town buildTown = event.getTownBlock().getTownOrNull();
                Town playerTown = resident.getTownOrNull();
                if(WarTown.getWarTownObject(buildTown).inWar()) {

                    War war = WarTown.getWarTownObject(buildTown).getWar();
                    if(!(war.getActivePhase() instanceof WarPhase)) {
                        return;
                    }

                    war.getOpponents(buildTown).forEach(opponent -> {
                        if(opponent.getUUID().equals(playerTown.getUUID())) {

                            boolean canBuild = false;
                            for (Chunk chunk : getChunksAroundLocation(event.getLocation())) {
                                TownBlock townBlock = TownyUniverse.getInstance().getTownBlockOrNull(WorldCoord.parseWorldCoord(getBlockAtChunk(chunk).getLocation()));
                                if(townBlock == null || !townBlock.getTownOrNull().getUUID().equals(buildTown.getUUID())) {
                                    canBuild = true;
                                }
                            }

                            if(!canBuild) {
                                M.msg(event.getPlayer(),M.getMessage("cant_set_flag"));
                                event.setCancelled(true);
                                return;
                            }

                        //   if(!(Economy.getBalance(event.getPlayer()) >= 20)) {
                        //       M.msg(event.getPlayer(),M.getMessage("not_enough_money"));
                        //       event.setCancelled(true);
                        //       return;
                        //   }

                            Block block = event.getPlayer().getWorld().getBlockAt(event.getBlock().getLocation());
                            Hologram hologram = DHAPI.createHologram("capture" + getAviableId(), block.getLocation().add(0,2.5,0));
                            Date endTime = new Date(System.currentTimeMillis() + 60L * 1000L);
                            Duration timeRemain = Duration.between(Instant.now(), new Date(endTime.getTime()).toInstant());
                            DHAPI.addHologramLine(hologram,M.color("&cФлаг города " + playerTown.getName()));
                            HologramLine line = DHAPI.addHologramLine(hologram,M.color("&7Осталось до захвата: &e " + timeRemain.getSeconds() + "сек."));
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Duration timeRemain = Duration.between(Instant.now(), new Date(endTime.getTime()).toInstant());
                                    line.setText(M.color("&7Осталось до захвата: &e " + timeRemain.getSeconds() + "сек."));

                                    Block block = event.getPlayer().getWorld().getBlockAt(event.getBlock().getLocation());
                                    if(block == null || block.getBlockData().getMaterial() != Material.RED_BANNER) {
                                        hologram.delete();
                                        this.cancel();
                                        return;
                                    }


                                    if (new Date().after(endTime)) {

                                        try {
                                            ((WarPhase) war.getActivePhase()).addPoints(war.getOpponent(buildTown), Configs.POINTS_CONFIG.getConfig().getInt("points.get_chunk"));
                                            ((WarPhase) war.getActivePhase()).removePoints(buildTown, Configs.POINTS_CONFIG.getConfig().getInt("points.lost_chunk"));

                                            for (Block blockLoop : getBlocksAtChunk(event.getBlock().getChunk())) {
                                                TownBlock townBlock = TownyUniverse.getInstance().getTownBlockOrNull(WorldCoord.parseWorldCoord(blockLoop.getLocation()));
                                                if (townBlock != null && townBlock.getTownOrNull().getUUID().equals(buildTown.getUUID())) {
                                                    try {
                                                        buildTown.removeTownBlock(townBlock);
                                                        playerTown.addTownBlock(townBlock);
                                                    } catch (AlreadyRegisteredException e) {
                                                        continue;
                                                    }
                                                }
                                            }

                                        } catch (Exception e) {

                                        }
                                        event.getPlayer().getWorld().getBlockAt(event.getBlock().getLocation()).setType(Material.AIR);
                                        hologram.delete();
                                        this.cancel();
                                    }
                                }
                            }.runTaskTimer(TownyWars.getPlugin(),0,20L);
                            event.setCancelled(false);
                            return;
                        }
                    });

                }
            }
        }
    }

    private int getAviableId() {
        for (int i = 0; i < 10000; i++) {
            if(DHAPI.getHologram("capture" + i) == null) {
                return i;
            }
        }
        return 10000;
    }

    public Collection<Chunk> getChunksAroundLocation(Location loc) {
        int[] offset = {-1,0,1};

        World world = loc.getWorld();
        int baseX = loc.getChunk().getX();
        int baseZ = loc.getChunk().getZ();

        Collection<Chunk> chunksAroundLoc = new HashSet<>();
        for(int x : offset) {
            for(int z : offset) {
                Chunk chunk = world.getChunkAt(baseX + x, baseZ + z);
                chunksAroundLoc.add(chunk);
            }
        }
        return chunksAroundLoc;
    }

    private List<Block> getBlocksAtChunk(Chunk chunk) {
        List<Block> blocks = new ArrayList<>();
        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;
        World world = chunk.getWorld();

        for(int xx = x; xx < x + 16; xx++) {
            for(int zz = z; zz < z + 16; zz++) {
                for(int yy = 0; yy < 256; yy++) {
                    blocks.add(world.getBlockAt(xx, yy, zz));
                }
            }
        }
        return blocks;
    }

    private Block getBlockAtChunk(Chunk chunk) {
        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;
        World world = chunk.getWorld();

        for(int xx = x; xx < x + 16; xx++) {
            for(int zz = z; zz < z + 16; zz++) {
                for(int yy = 0; yy < 256; yy++) {
                    return world.getBlockAt(xx, yy, zz);
                }
            }
        }
        return null;
    }

    @EventHandler
    public void onTownyBreak(TownyDestroyEvent event) {
        String materialName = event.getBlock().getBlockData().getMaterial().name();
        if((materialName.contains("DOOR") || materialName.contains("TORCH"))) {
            Player player = event.getPlayer();
            if(isPlayerInWarPhase(player)) {
                try {

                    Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                    WarTown warTown = WarTown.getWarTownObject(resident.getTown());
                    Town opponentTown = warTown.getWar().getOpponent(warTown.getTown());
                    if(event.getTownBlock().hasTown() && event.getTownBlock().getTown().getUUID().equals(opponentTown.getUUID())) {
                        event.setMessage(null);
                        event.setCancelled(false);
                    }

                } catch (NotRegisteredException e) {

                }
            }
        }
    }

    @EventHandler
    public void onNationLeave(NationTownLeaveEvent event) {
        if(TownyWars.getForcedInvitedInNationTowns().contains(event.getTown().getUUID())) {
            try {
                event.getTown().setNation(event.getNation());
                M.msg(event.getTown().getMayor().getPlayer(),M.getMessage("cant_leave_nation"));
            } catch (AlreadyRegisteredException e) {

            }
        }
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        if(event.getPlayer().getKiller() == null) {
            return;
        }
        Resident victim = TownyUniverse.getInstance().getResident(event.getPlayer().getUniqueId());
        Resident killer = TownyUniverse.getInstance().getResident(event.getPlayer().getKiller().getUniqueId());
        if((victim.hasTown() && killer.hasTown()) && (isPlayerInWarPhase(victim.getPlayer()) && isPlayerInWarPhase(killer.getPlayer()))) {
            try {

                WarTown killerTown = WarTown.getWarTownObject(killer.getTown());
                Collection<Town> opponentTowns = killerTown.getWar().getOpponents(killerTown.getTown());

                for (Town opponentTown : opponentTowns) {
                    if (opponentTown.getUUID().equals(victim.getTown().getUUID())) {
                        int points = Configs.POINTS_CONFIG.getConfig().getInt("points.player_kill");
                        ((WarPhase) killerTown.getWar().getActivePhase()).addPoints(killerTown.getTown(), points);
                    }
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }

        }
    }


    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        pullSet();

        if(denyWarStrings.contains(event.getMessage()) && isPlayerInWarPhase(event.getPlayer())) {
            M.msg(event.getPlayer(),M.getMessage("dont_use_command_in_war"));
            event.setCancelled(true);
            return;
        }


        if(denyPreparationStrings.contains(event.getMessage()) && isPlayerInPreparationPhase(event.getPlayer())) {
            M.msg(event.getPlayer(),M.getMessage("dont_use_command_in_preparation"));
            event.setCancelled(true);
            return;
        }

    }

    @EventHandler
    public void onRename(RenameTownEvent event) {
        for (Map.Entry<UUID, Set<String>> entry : TownyWars.getSlaveTowns().entrySet()) {
            Set<String> names = entry.getValue();
            if(names.contains(event.getOldName())) {
                names.remove(event.getOldName());
                names.add(event.getTown().getName());
                TownyWars.getSlaveTowns().put(entry.getKey(),names);
            }
        }
    }


    private boolean isPlayerInWarPhase(Player player) {
        try {
            Town pTown = TownyUniverse.getInstance().getResident(player.getUniqueId()).getTown();
            if(WarTown.getWarTownObject(pTown).inWar() && WarTown.getWarTownObject(pTown).getWar().getActivePhase() instanceof WarPhase) {
                return true;
            }
        } catch (TownyException e) {

        }
        return false;
    }

    private boolean isPlayerInPreparationPhase(Player player) {
        try {
            Town pTown = TownyUniverse.getInstance().getResident(player.getUniqueId()).getTown();
            if(WarTown.getWarTownObject(pTown).inWar() && WarTown.getWarTownObject(pTown).getWar().getActivePhase() instanceof PreparationPhase) {
                return true;
            }
        } catch (TownyException e) {

        }
        return false;
    }

    private void pullSet() {
        if(denyWarStrings.isEmpty()) {
            denyWarStrings.add("/plot toggle");
            denyWarStrings.add("/town toggle pvp");
            denyWarStrings.add("/town toggle public");
            denyWarStrings.add("/town toggle explosion");
            denyWarStrings.add("/town toggle fire");
            denyWarStrings.add("/town toggle mobs");
            denyWarStrings.add("/town toggle taxpercent");
            denyWarStrings.add("/town toggle open");
        }
        if(denyPreparationStrings.isEmpty()) {
            denyPreparationStrings.add("/t unclaim");
            denyPreparationStrings.add("/town unclaim");
            denyPreparationStrings.add("/t withdraw");
            denyPreparationStrings.add("/town withdraw");
            denyPreparationStrings.add("/t delete");
            denyPreparationStrings.add("/town delete");
            denyPreparationStrings.add("/n withdraw");
            denyPreparationStrings.add("/nation withdraw");
            denyPreparationStrings.add("/n delete");
            denyPreparationStrings.add("/n delete delete");
        }
        if(mayorCommands.isEmpty()) {
            mayorCommands.add("/town withdraw");
            mayorCommands.add("/town claim");
            mayorCommands.add("/town unclaim");
            mayorCommands.add("/town add");
            mayorCommands.add("/town kick");
            mayorCommands.add("/town set");
            mayorCommands.add("/town buy");
            mayorCommands.add("/town plots");
            mayorCommands.add("/town toggle");
            mayorCommands.add("/town rank add");
            mayorCommands.add("/town rank remove");
            mayorCommands.add("/town delete");

            mayorCommands.add("/t withdraw");
            mayorCommands.add("/t claim");
            mayorCommands.add("/t unclaim");
            mayorCommands.add("/t add");
            mayorCommands.add("/t kick");
            mayorCommands.add("/t set");
            mayorCommands.add("/t buy");
            mayorCommands.add("/t plots");
            mayorCommands.add("/t toggle");
            mayorCommands.add("/t rank add");
            mayorCommands.add("/t rank remove");
            mayorCommands.add("/t delete");
        }
    }
}
