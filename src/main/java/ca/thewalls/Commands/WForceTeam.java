package ca.thewalls.Commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import ca.thewalls.Utils;
import ca.thewalls.Walls.Team;

public class WForceTeam implements CommandExecutor {
    public TheWalls walls;
    public WForceTeam(TheWalls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("thewalls.forceteam") && !sender.isOp()) {
            sender.sendMessage(Messages.msg("admin.no_permission"));
            return false;
        }
        try {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Utils.format("&cCouldn't find player with the name of " + args[0] + "!"));
                return false;
            }
            ca.thewalls.Arena arena = this.walls.getArenaByPlayer(target);
            if (arena == null) {
                arena = (sender instanceof Player)
                        ? this.walls.getArenaByPlayer((Player) sender)
                        : null;
            }
            if (arena == null) {
                sender.sendMessage(Messages.msg("wstart.arena_required"));
                return false;
            }
            if (!arena.getGame().started) {
                sender.sendMessage(Utils.format("&cThere is no game currently going on!"));
                return false;
            }
            if (arena.getGame().borderClosing) {
                sender.sendMessage(Utils.format("&cThis command cannot be performed when the borders are closing!"));
                return false;
            }
            StringBuilder newTeamBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                newTeamBuilder.append(args[i]);
            }
            String newTeam = newTeamBuilder.toString();
            boolean flag = false;

            for (int i = 0; i < arena.getGame().teams.size(); i++) {
                Team temp = arena.getGame().teams.get(i);
                if (temp.teamName.toLowerCase().equalsIgnoreCase(newTeam)) {
                    Team prev = Team.getPlayerTeam(target, arena.getGame().teams);

                    if (!temp.alive) {
                        temp.alive = true;
                        arena.getGame().aliveTeams.add(temp);
                    }

                    temp.members.add(target);
                    target.teleport(temp.teamSpawn);
                    target.sendMessage(Utils.format(temp.teamColor + "You have been swapped to " + temp.teamName + " team!"));

                    if (prev != null) {
                        prev.members.remove(target);
                        if (prev.members.size() == 0) {
                            arena.getGame().aliveTeams.remove(prev);
                            arena.getGame().teams.remove(prev);
                        }
                    }

                    flag = true;
                    break;
                }
            }
            // create relevant team;
            if (!flag) {
                Team prev = Team.getPlayerTeam(target, arena.getGame().teams);

                int teamID = 0;
                if (newTeam.equalsIgnoreCase(Config.data.getString("teams.one.name").toLowerCase())) {
                    teamID = 1;
                }
                if (newTeam.equalsIgnoreCase(Config.data.getString("teams.two.name").toLowerCase())) {
                    teamID = 2;
                }
                if (newTeam.equalsIgnoreCase(Config.data.getString("teams.three.name").toLowerCase())) {
                    teamID = 3;
                }

                Team newT = new Team(teamID, false, arena);
                newT.members.add(target);
                target.teleport(newT.teamSpawn);
                target.sendMessage(Utils.format(newT.teamColor + "You have been swapped to " + newT.teamName + " team!"));

                if (prev != null) {
                    prev.members.remove(target);
                    if (prev.members.size() == 0) {
                        arena.getGame().aliveTeams.remove(prev);
                        arena.getGame().teams.remove(prev);
                    }
                }
            }

            Team finalTeam = Team.getPlayerTeam(target, arena.getGame().teams);
            if (finalTeam != null) {
                target.displayName(Utils.format(finalTeam.teamColor + "[" + finalTeam.teamName + "] " + target.getName()));
                target.playerListName(Utils.format(finalTeam.teamColor + "[" + finalTeam.teamName + "] " + target.getName()));
                target.getInventory().clear();
                target.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, Config.data.getInt("players.spawn.steakAmount")));
                target.setGameMode(GameMode.SURVIVAL);
            }
        } catch (Exception e) {
            sender.sendMessage(Utils.format("&cArguments provided aren't valid!"));
            Utils.getPlugin().getLogger().warning(e.toString());
        }

        return false;
    }
}
