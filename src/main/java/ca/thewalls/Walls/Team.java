package ca.thewalls.Walls;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.Utils;
import ca.thewalls.Commands.WEvents;

import java.util.ArrayList;

public class Team {
    public ArrayList<Player> members = new ArrayList<>();
    public int team; // 0 = red, 1 = blue, 2 = yellow, 3 = green
    public String teamName = "TBD";
    public String teamColor = "<white>";
    public Location teamSpawn;
    public boolean alive = false;
    public Arena arena;

    public Team(int _team, boolean overrideAlive, Arena arena) {
        this.arena = arena;
        new Location(this.arena.getWorld().world, 0, 0, 0);
        team = _team;
        switch (team) {
            case 0:
                teamName = Config.data.getString("teams.zero.name");
                teamColor = Utils.normalizeColor(Config.data.getString("teams.zero.color"));
                int x0 = this.arena.getWorld().positionOne[0] - (this.arena.getGame().size / 2);
                int z0 = this.arena.getWorld().positionOne[1] - (this.arena.getGame().size / 2);
                int y0 = this.arena.getWorld().world.getHighestBlockYAt(x0, z0);
                teamSpawn = new Location(this.arena.getWorld().world, x0, y0, z0);
                break;
            case 1:
                teamName = Config.data.getString("teams.one.name");
                teamColor = Utils.normalizeColor(Config.data.getString("teams.one.color"));
                int x1 = this.arena.getWorld().positionTwo[0] + (this.arena.getGame().size / 2);
                int z1 = this.arena.getWorld().positionTwo[1] + (this.arena.getGame().size / 2);
                int y1 = this.arena.getWorld().world.getHighestBlockYAt(x1, z1);
                teamSpawn = new Location(this.arena.getWorld().world, x1, y1, z1);
                break;
            case 2:
                teamName = Config.data.getString("teams.two.name");
                teamColor = Utils.normalizeColor(Config.data.getString("teams.two.color"));
                int x2 = (int) (this.arena.getWorld().positionOne[0] - (this.arena.getGame().size / 2));
                int z2 = (int) (this.arena.getWorld().positionOne[1] - (this.arena.getGame().size * 1.5));
                int y2 = this.arena.getWorld().world.getHighestBlockYAt(x2, z2);
                teamSpawn = new Location(this.arena.getWorld().world, x2, y2, z2);
                break;
            case 3:
                teamName = Config.data.getString("teams.three.name");
                teamColor = Utils.normalizeColor(Config.data.getString("teams.three.color"));
                int x3 = (int) (this.arena.getWorld().positionTwo[0] + (this.arena.getGame().size / 2));
                int z3 = (int) (this.arena.getWorld().positionTwo[1] + (this.arena.getGame().size * 1.5));
                int y3 = this.arena.getWorld().world.getHighestBlockYAt(x3, z3);
                teamSpawn = new Location(this.arena.getWorld().world, x3, y3, z3);
                break;
        }

        this.arena.getGame().teams.add(this);
        if (!overrideAlive) {
            alive = true;
            this.arena.getGame().aliveTeams.add(this);
        }
    }

    public int getAliveMembers() {
        int count = 0;
        for (Player ply : members) {
            if (Utils.isAlive(ply)) {
                count++;
            }
        }
        return count;
    }

    public void readyPlayer(Player ply) {
        WEvents.annouceEvents(ply);
        ply.setHealth(20);
        ply.setSaturation(20);

        // Handle inventory
        ply.getInventory().clear();
        ply.getInventory().remove(ply.getItemOnCursor());
        if (ply.hasActiveItem()) {
            ply.getInventory().remove(ply.getActiveItem());
            ply.clearActiveItem();
        }
        ply.setItemOnCursor(null);
        ply.updateInventory();

        // Noti
        Utils.sendTitle(ply, Messages.raw("title.match_title"), Messages.raw("title.match_subtitle"), 10, 80, 20);
        ply.playSound(ply.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 255, 1);

        // Game Stuff
        ply.displayName(Utils.componentFromString(teamColor + "[" + teamName + "] " + ply.getName()));
        ply.playerListName(Utils.componentFromString(teamColor + "[" + teamName + "] " + ply.getName()));
        ply.setGameMode(GameMode.SURVIVAL);
        ply.setStatistic(Statistic.DEATHS, 0);
        String kitId = ca.thewalls.Config.getPlayerKit(ply.getUniqueId());
        if (kitId == null || kitId.isEmpty()) {
            kitId = ca.thewalls.Kits.getDefaultKit();
        }
        if (kitId != null && !kitId.isEmpty()) {
            ca.thewalls.Kits.applyKit(ply, kitId);
        }
        ca.thewalls.Perks.applyPerks(ply);
        ply.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, Config.data.getInt("players.spawn.steakAmount")));

        // Calc teamspawn in case of change
        teamSpawn.setY(this.arena.getWorld().world.getHighestBlockYAt(teamSpawn.getBlockX(), teamSpawn.getBlockZ()));
        ply.teleport(teamSpawn.add(0, 2, 0));
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                TempBlock temp = new TempBlock(new Location(this.arena.getWorld().world, teamSpawn.getBlockX() + x, teamSpawn.getBlockY() - 1, teamSpawn.getBlockZ() + z),
                    this.arena.getWorld().world.getBlockAt(teamSpawn.getBlockX() + x, teamSpawn.getBlockY() - 1, teamSpawn.getBlockZ() + z).getType());
                this.arena.getWorld().originalBlocks.add(temp);
                if (Config.data.getBoolean("teams.clearProtectionBlocksAfterDrop", true)) {
                    this.arena.getWorld().spawnProtectionBlocks.add(temp.loc);
                }
                this.arena.getWorld().world.getBlockAt(temp.loc).setType(Material.BEDROCK);
            }
        }
    }

    public static Team getPlayerTeam(Player p, ArrayList<Team> teams) {
        for (Team team : teams) {
            for (Player ply : team.members) {
                if (p == ply) {
                    return team;
                }
            }
        }

        return null;
    }

}
