package me.bright.townywars.utils;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import me.bright.townywars.TownyWars;
import me.bright.townywars.War;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiConsumer;

public enum EndsWar {

    DESTROY((war,winnerTown,lostTown) -> {
        Player player = winnerTown.getMayor().getPlayer();
        Bukkit.broadcastMessage(M.color(M.getMessage("destroy_town").replace("[town]",lostTown.getName())));
        TownyUniverse.getInstance().getDataSource().deleteTown(lostTown);
        war.end();
        player.closeInventory();
    }),
    CAPTURE((war,winnerTown,lostTown) -> {
        Player player = winnerTown.getMayor().getPlayer();
        if(winnerTown.getNationOrNull() == null) {
            M.msg(player,M.getMessage("not_nation"));
            return;
        } else {
            Nation nation = winnerTown.getNationOrNull();
            lostTown.removeNation();
            try {
                lostTown.setNation(nation);
                TownyWars.addForceInvitedInNationTown(lostTown);
            } catch (AlreadyRegisteredException e) {
                M.msg(player,M.getMessage("error"));
                return;
            }
            String message = M.getMessage("town_captured").replace("[town]",lostTown.getName());
            message = message.replace("[nation]",nation.getName());
            Bukkit.broadcastMessage(message);
            war.end();
        }
    }),
    MERGE((war,winnerTown,lostTown) -> {
        boolean canMerge = false;
        Location loc = null;
        Player player = winnerTown.getMayor().getPlayer();
        try {

            loc = winnerTown.getSpawn();
            if(checkCanMerge(loc,winnerTown,lostTown)) {
                Collection<TownBlock> townBlocks = lostTown.getTownBlocks();
                TownyUniverse.getInstance().getDataSource().deleteTown(lostTown);
                townBlocks.forEach(townBlock -> {
                    try {
                        winnerTown.addTownBlock(townBlock);
                    } catch (AlreadyRegisteredException e) {
                        M.msg(player,M.getMessage("error"));
                        return;
                    }
                });
                String message = M.color(M.getMessage("merge_town"));
                message = message.replace("[loser]",lostTown.getName());
                message = message.replace("[winner]",winnerTown.getName());
                player.closeInventory();
                Bukkit.broadcastMessage(message);
                war.end();
            } else {
                M.msg(player,M.getMessage("town_too_far"));
                return;
            }

        } catch (TownyException e) {
            M.msg(player,M.getMessage("error"));
            return;
        }
    }),
    SPARE((war,winnerTown,lostTown) -> {
        Player player = winnerTown.getMayor().getPlayer();
        Bukkit.broadcastMessage(M.color(M.getMessage("town_spared").replace("[town]",lostTown.getName())));
        war.end();
        player.closeInventory();
    }),
    ENSLAVE((war,winnerTown,lostTown) -> {
        Player player = winnerTown.getMayor().getPlayer();
        Set<String> slaveTowns = TownyWars.getSlaveTowns().get(player.getUniqueId());
        if(slaveTowns == null) {
            slaveTowns = new HashSet<>();
        }
        slaveTowns.add(lostTown.getName());
        TownyWars.getSlaveTowns().put(player.getUniqueId(),slaveTowns);
        String message = M.color(M.getMessage("town_enslaved").replace("[winner]",winnerTown.getName()));
        message = message.replace("[loser]",lostTown.getName());
        Bukkit.broadcastMessage(message);
        war.end();
        player.closeInventory();
    });

    private TriConsumer<War,Town,Town> func;
    EndsWar(TriConsumer<War, Town, Town> func) {
        this.func = func;
    }

    public void apply(War war, Town winner, Town loser) {
        func.accept(war,winner,loser);
    }

    private static Collection<Chunk> getChunksAroundLocation(Location loc) {
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

    private static Block getBlockAtChunk(Chunk chunk) {
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

    private static boolean checkCanMerge(Location loc, Town winnerTown, Town lostTown) {

        boolean canMerge = false;

        for (TownBlock townBlock : winnerTown.getTownBlocks()) {
            Coord coord = townBlock.getWorldCoord().getCoord();
            Block block = loc.getWorld().getBlockAt(coord.getX(),50,coord.getZ());
            for(Chunk chunk: getChunksAroundLocation(block.getLocation())) {
                TownBlock checkTownBlock = TownyUniverse.getInstance().getTownBlockOrNull(WorldCoord.parseWorldCoord(getBlockAtChunk(chunk)));

                if(checkTownBlock == null || !checkTownBlock.hasTown()) {
                    continue;
                }

                if(checkTownBlock.getTownOrNull().getUUID().equals(lostTown.getUUID())) {
                    canMerge = true;
                    return true;
                }
            }
        }
        return canMerge;
    }


}
