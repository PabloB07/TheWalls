package ca.thewalls;

import ca.thewalls.Walls.Game;
import ca.thewalls.Walls.Team;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BountyManager {
    private final Arena arena;
    private final Game game;
    private final Random random = new Random();
    private UUID target;
    private int reward;
    private int timer;

    public BountyManager(Arena arena) {
        this.arena = arena;
        this.game = arena.getGame();
        this.timer = Config.data.getInt("bounties.intervalSeconds", 120);
    }

    public void tick() {
        if (!Config.data.getBoolean("bounties.enabled", true)) return;
        if (!game.started) return;
        int minPlayers = Config.data.getInt("bounties.minPlayers", 2);
        int alive = Utils.getAlivePlayers(arena).size();
        if (alive < minPlayers) return;
        if (target == null) {
            if (timer > 0) {
                timer--;
                return;
            }
            assignNewBounty();
            return;
        }
        if (timer > 0) timer--;
        if (timer <= 0) {
            assignNewBounty();
        }
    }

    public void clear() {
        target = null;
        reward = 0;
        timer = Config.data.getInt("bounties.intervalSeconds", 120);
    }

    public UUID getTarget() {
        return target;
    }

    public int getReward() {
        return reward;
    }

    public int getRemainingSeconds() {
        return Math.max(0, timer);
    }

    public int getIntervalSeconds() {
        return Math.max(1, Config.data.getInt("bounties.intervalSeconds", 120));
    }

    public void handleKill(Player killer, Player victim) {
        if (!Config.data.getBoolean("bounties.enabled", true)) return;
        if (killer == null || victim == null) return;
        if (target == null || !victim.getUniqueId().equals(target)) return;

        boolean paid = EconomyService.deposit(killer, reward);
        for (Player p : arena.getPlayers()) {
            p.sendMessage(Messages.msg("bounty.claimed", java.util.Map.of(
                    "killer", killer.getName(),
                    "target", victim.getName(),
                    "amount", String.valueOf(reward)
            )));
        }
        if (!paid) {
            killer.sendMessage(Messages.msg("bounty.no_economy"));
        }
        clear();
    }

    private void assignNewBounty() {
        List<Player> alive = Utils.getAlivePlayers(arena);
        if (alive.isEmpty()) return;
        Player chosen = alive.get(random.nextInt(alive.size()));
        target = chosen.getUniqueId();
        int min = Config.data.getInt("bounties.rewardMin", 5);
        int max = Config.data.getInt("bounties.rewardMax", 15);
        if (max < min) max = min;
        reward = min + random.nextInt(Math.max(1, max - min + 1));
        timer = Config.data.getInt("bounties.intervalSeconds", 120);
        for (Player p : arena.getPlayers()) {
            p.sendMessage(Messages.msg("bounty.new", java.util.Map.of(
                    "target", chosen.getName(),
                    "amount", String.valueOf(reward)
            )));
        }
    }
}
