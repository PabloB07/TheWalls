package ca.thewalls;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class Config {
    public static File dataFile;
    public static YamlConfiguration data;
    public static File leaderboardFile;
    public static YamlConfiguration leaderboard;
    public static File lobbiesFile;
    public static YamlConfiguration lobbies;
    public static File perksFile;
    public static YamlConfiguration perks;
    public static File cratesFile;
    public static YamlConfiguration crates;
    public static File kitsFile;
    public static YamlConfiguration kits;

    public static void initializeData() {
        try {
            dataFile = new File(Utils.getPlugin().getDataFolder(), "config.yml");
            if (!dataFile.exists()) {
                if (dataFile.getParentFile().mkdirs()) {
                    Utils.getPlugin().getLogger().info("Created data folder!");
                }
                if (dataFile.createNewFile()) {
                    Utils.getPlugin().getLogger().info("Created config file!");
                }
            }
            data = YamlConfiguration.loadConfiguration(dataFile);
            // fill config
            // events
            if (!data.isSet("events.reveal.enabled")) {
                data.set("events.reveal.enabled", true);
                data.set("events.reveal.displayCords", true);
                data.set("events.reveal.seconds", 10);
            }
            if (!data.isSet("events.blood.enabled")) {
                data.set("events.blood.enabled", true);
            }
            if (!data.isSet("events.blood.color")) {
                data.set("events.blood.color", "#8b0000");
            }
            if (!data.isSet("events.blood.useTeamColor")) {
                data.set("events.blood.useTeamColor", true);
            }
            if (!data.isSet("events.blood.amount")) {
                data.set("events.blood.amount", 12);
            }
            if (!data.isSet("events.blood.spread")) {
                data.set("events.blood.spread", 0.3);
            }
            if (!data.isSet("events.blood.size")) {
                data.set("events.blood.size", 1.0);
            }
            if (!data.isSet("events.minPlayers")) {
                data.set("events.minPlayers", 2);
            }
            if (!data.isSet("events.safeSecondsAfterStart")) {
                data.set("events.safeSecondsAfterStart", 30);
            }
            if (!data.isSet("events.safeSpawnRadius")) {
                data.set("events.safeSpawnRadius", 16);
            }
            if (!data.isSet("events.maxTargets")) {
                data.set("events.maxTargets", 1);
            }
            if (!data.isSet("events.blindSnail.enabled")) {
                data.set("events.blindSnail.enabled", true);
                data.set("events.blindSnail.seconds", 5);
                data.set("events.blindSnail.blindStrength", 10);
                data.set("events.blindSnail.slowStrength", 3);
            }
            if (!data.isSet("events.hailStorm.enabled")) {
                data.set("events.hailStorm.enabled", true);
                data.set("events.hailStorm.delay", 5);
                data.set("events.hailStorm.height", 5);
                data.set("events.hailStorm.volleySize", 3);
                data.set("events.hailStorm.arrowDamage", 4);
            }
            if (!data.isSet("events.tnt.enabled")) {
                data.set("events.tnt.enabled", true);
                data.set("events.tnt.amount", 5);
            }
            if (!data.isSet("events.tnt.followPlayer")) {
                data.set("events.tnt.followPlayer", true);
            }
            if (!data.isSet("events.locationSwap.enabled")) {
                data.set("events.locationSwap.enabled", true);
            }
            if (!data.isSet("events.supplyChest.enabled")) {
                data.set("events.supplyChest.enabled", true);
                data.set("events.supplyChest.allowedRegionPercentageOfSize", 0.2);
            }
            if (!data.isSet("events.sinkHole.enabled")) {
                data.set("events.sinkHole.enabled", true);
                data.set("events.sinkHole.size", 1);
                data.set("events.sinkHole.seconds", 3);
                data.set("events.sinkHole.timeUntilReset", 10);
            }
            // players
            if (!data.isSet("players.spawn.steakAmount")) {
                data.set("players.spawn.steakAmount", 8);
            }
            // teams
            if (!data.isSet("teams.zero.name")) {
                data.set("teams.zero.name", "Red");
                data.set("teams.zero.color", "<#ff3b3b>");
            }
            if (!data.isSet("teams.one.name")) {
                data.set("teams.one.name", "Blue");
                data.set("teams.one.color", "<#3b6cff>");
            }
            if (!data.isSet("teams.two.name")) {
                data.set("teams.two.name", "Yellow");
                data.set("teams.two.color", "<#ffd23b>");
            }
            if (!data.isSet("teams.three.name")) {
                data.set("teams.three.name", "Green");
                data.set("teams.three.color", "<#3bff6f>");
            }
            if (!data.isSet("teams.allowTie")) {
                data.set("teams.allowTie", false);
            }
            if (!data.isSet("teams.checkWinEverySecond")) {
                data.set("teams.checkWinEverySecond", true);
            }
            if (!data.isSet("teams.clearProtectionBlocksAfterDrop")) {
                data.set("teams.clearProtectionBlocksAfterDrop", true);
            }

            // world
            if (!data.isSet("world.borderShrinkPercentageOfSize")) {
                data.set("world.borderShrinkPercentageOfSize", 0.2);
            }
            if (!data.isSet("world.reset.strategy")) {
                data.set("world.reset.strategy", "classic");
            }
            if (!data.isSet("world.reset.copy.templateDir")) {
                data.set("world.reset.copy.templateDir", "worlds/templates");
            }
            if (!data.isSet("world.reset.copy.instancePrefix")) {
                data.set("world.reset.copy.instancePrefix", "tw_");
            }
            if (!data.isSet("world.reset.copy.deleteOnUnload")) {
                data.set("world.reset.copy.deleteOnUnload", true);
            }
            if (!data.isSet("world.saving")) {
                data.set("world.saving", true);
            }
            if (!data.isSet("world.saveAir")) {
                data.set("world.saveAir", true);
            }
            if (!data.isSet("world.safetyBounds")) {
                data.set("world.safetyBounds", 3);
            }
            if (!data.isSet("theWalls.respawnDuringPrepTime")) {
                data.set("theWalls.respawnDuringPrepTime", false);
            }
            if (!data.isSet("theWalls.respawnDuringInitialFighting")) {
                data.set("theWalls.respawnDuringInitialFighting", false);
            }
            if (!data.isSet("theWalls.autoExecute.center")) {
                data.set("theWalls.autoExecute.center.x", 0);
                data.set("theWalls.autoExecute.center.z", 0);
                data.set("theWalls.autoExecute.size", 100);
                data.set("theWalls.autoExecute.eventCooldown", 60);
                data.set("theWalls.autoExecute.prepTime", 600);
                data.set("theWalls.autoExecute.timeUntilBorderClose", 600);
                data.set("theWalls.autoExecute.speedOfBorderClose", 180);
                data.set("theWalls.autoExecute.worldName", "world");
            }
            if (!data.isSet("holograms.top.location.world")) {
                data.set("holograms.top.location.world", "");
                data.set("holograms.top.location.x", 0);
                data.set("holograms.top.location.y", 0);
                data.set("holograms.top.location.z", 0);
                data.set("holograms.top.location.yaw", 0);
                data.set("holograms.top.location.pitch", 0);
            }
            if (!data.isSet("holograms.top.offsetY")) {
                data.set("holograms.top.offsetY", 0.0);
            }
            if (!data.isSet("lobby.minPlayers")) {
                data.set("lobby.minPlayers", 2);
            }
            if (!data.isSet("lobby.maxPlayers")) {
                data.set("lobby.maxPlayers", -1);
            }
            if (!data.isSet("lobby.countdownSeconds")) {
                data.set("lobby.countdownSeconds", 20);
            }
            // gui
            if (!data.isSet("gui.titles.default")) {
                data.set("gui.titles.default", "<gray><bold>TheWalls</bold></gray>");
                data.set("gui.titles.kit", "<gradient:#22d3ee:#a78bfa><bold>Kits</bold></gradient>");
                data.set("gui.titles.perk", "<gradient:#a78bfa:#22d3ee><bold>Perks</bold></gradient>");
                data.set("gui.titles.confirm", "<gradient:#22d3ee:#a78bfa><bold>Confirm</bold></gradient>");
                data.set("gui.titles.crate_confirm", "<gradient:#f59e0b:#f97316><bold>Crate</bold></gradient>");
                data.set("gui.titles.arena", "<gradient:#38bdf8:#a78bfa><bold>Select Arena</bold></gradient>");
                data.set("gui.titles.team", "<gradient:#f472b6:#fb7185><bold>Select Team</bold></gradient>");
                data.set("gui.titles.cosmetics", "<gradient:#f59e0b:#f97316><bold>Cosmetics</bold></gradient>");
                data.set("gui.titles.trails", "<gradient:#06b6d4:#22d3ee><bold>Trails</bold></gradient>");
                data.set("gui.titles.killeffects", "<gradient:#ef4444:#f97316><bold>Kill Effects</bold></gradient>");
            }
            if (!data.isSet("gui.rows.arenas")) {
                data.set("gui.rows.arenas", 6);
                data.set("gui.rows.kits", 6);
                data.set("gui.rows.perks", 6);
                data.set("gui.rows.cosmetics", 6);
                data.set("gui.rows.trails", 6);
                data.set("gui.rows.killeffects", 6);
                data.set("gui.rows.team", 3);
                data.set("gui.rows.confirm", 3);
                data.set("gui.rows.crate_confirm", 3);
            }
            if (!data.isSet("gui.filler.enabled")) {
                data.set("gui.filler.enabled", true);
                data.set("gui.filler.material", "BLACK_STAINED_GLASS_PANE");
                data.set("gui.filler.name", "<gray> </gray>");
                data.set("gui.filler.lore", java.util.Collections.emptyList());
            }
            if (!data.isSet("gui.toolbar.enabled")) {
                data.set("gui.toolbar.enabled", true);
                data.set("gui.toolbar.prev.slot", 45);
                data.set("gui.toolbar.prev.material", "ARROW");
                data.set("gui.toolbar.prev.name", "<#94a3b8>Previous");
                data.set("gui.toolbar.prev.lore", java.util.Collections.emptyList());
                data.set("gui.toolbar.next.slot", 53);
                data.set("gui.toolbar.next.material", "ARROW");
                data.set("gui.toolbar.next.name", "<#94a3b8>Next");
                data.set("gui.toolbar.next.lore", java.util.Collections.emptyList());
                data.set("gui.toolbar.current.slot", 49);
                data.set("gui.toolbar.current.material", "CLOCK");
                data.set("gui.toolbar.current.name", "<#e2e8f0>Page <#38bdf8>{page}</#38bdf8>/<#38bdf8>{pages}</#38bdf8>");
                data.set("gui.toolbar.current.lore", java.util.Collections.emptyList());
                data.set("gui.toolbar.back.slot", 48);
                data.set("gui.toolbar.back.material", "BARRIER");
                data.set("gui.toolbar.back.name", "<#ef4444>Back");
                data.set("gui.toolbar.back.lore", java.util.Collections.emptyList());
                data.set("gui.toolbar.close.slot", 50);
                data.set("gui.toolbar.close.material", "STRUCTURE_VOID");
                data.set("gui.toolbar.close.name", "<#94a3b8>Close");
                data.set("gui.toolbar.close.lore", java.util.Collections.emptyList());
            }
            if (!data.isSet("gui.slots.arenas.info")) {
                data.set("gui.slots.arenas.info", 4);
                data.set("gui.slots.arenas.refresh", 6);
                data.set("gui.slots.kits.info", 4);
                data.set("gui.slots.perks.info", 4);
                data.set("gui.slots.perks.crate", 8);
                data.set("gui.slots.perks.balance", 0);
                data.set("gui.slots.cosmetics.info", 22);
                data.set("gui.slots.cosmetics.balance", 0);
                data.set("gui.slots.cosmetics.trails", 20);
                data.set("gui.slots.cosmetics.killeffects", 24);
                data.set("gui.slots.trails.info", 4);
                data.set("gui.slots.killeffects.info", 4);
                data.set("gui.slots.team.red", 10);
                data.set("gui.slots.team.blue", 12);
                data.set("gui.slots.team.yellow", 14);
                data.set("gui.slots.team.green", 16);
                data.set("gui.slots.confirm.accept", 11);
                data.set("gui.slots.confirm.cancel", 15);
                data.set("gui.slots.confirm.preview", 13);
                data.set("gui.slots.crate_confirm.accept", 11);
                data.set("gui.slots.crate_confirm.cancel", 15);
                data.set("gui.slots.crate_confirm.preview", 13);
            }
            if (!data.isSet("gui.items.balance")) {
                data.set("gui.items.balance.material", "GOLD_NUGGET");
                data.set("gui.items.balance.name", "<#fbbf24>Balance: <#ffffff>{amount}");
                data.set("gui.items.balance.lore", java.util.Collections.emptyList());
                data.set("gui.items.kit_info.material", "BOOK");
                data.set("gui.items.kit_info.name", "<#38bdf8>Choose Your Kit");
                data.set("gui.items.kit_info.lore", java.util.List.of("<#94a3b8>Pick a kit before the game starts."));
                data.set("gui.items.perk_info.material", "ENCHANTED_BOOK");
                data.set("gui.items.perk_info.name", "<#a78bfa>Perks");
                data.set("gui.items.perk_info.lore", java.util.List.of("<#94a3b8>Unlock perks permanently."));
                data.set("gui.items.perks_crate.material", "ENDER_CHEST");
                data.set("gui.items.perks_crate.name", "<#f59e0b>Open Crate");
                data.set("gui.items.perks_crate.lore", java.util.List.of("<#94a3b8>Random perk or cosmetic."));
                data.set("gui.items.cosmetics_info.material", "NAME_TAG");
                data.set("gui.items.cosmetics_info.name", "<#f59e0b>Cosmetics");
                data.set("gui.items.cosmetics_info.lore", java.util.List.of("<#94a3b8>Right-click to preview."));
                data.set("gui.items.cosmetics_trails.material", "FIREWORK_STAR");
                data.set("gui.items.cosmetics_trails.name", "<#38bdf8>Trails");
                data.set("gui.items.cosmetics_trails.lore", java.util.List.of("<#94a3b8>Particles while moving."));
                data.set("gui.items.cosmetics_killeffects.material", "NETHERITE_SWORD");
                data.set("gui.items.cosmetics_killeffects.name", "<#ef4444>Kill Effects");
                data.set("gui.items.cosmetics_killeffects.lore", java.util.List.of("<#94a3b8>Effects on kill."));
                data.set("gui.items.arena_info.material", "COMPASS");
                data.set("gui.items.arena_info.name", "<#38bdf8>Select Arena");
                data.set("gui.items.arena_info.lore", java.util.Collections.emptyList());
                data.set("gui.items.arena_refresh.material", "SPYGLASS");
                data.set("gui.items.arena_refresh.name", "<#94a3b8>Refresh List");
                data.set("gui.items.arena_refresh.lore", java.util.List.of("<#64748b>Click to update."));
                data.set("gui.items.team_red.material", "RED_WOOL");
                data.set("gui.items.team_red.name", "<#ff3b3b>Red Team");
                data.set("gui.items.team_red.lore", java.util.List.of("<#94a3b8>Click to join."));
                data.set("gui.items.team_blue.material", "BLUE_WOOL");
                data.set("gui.items.team_blue.name", "<#3b6cff>Blue Team");
                data.set("gui.items.team_blue.lore", java.util.List.of("<#94a3b8>Click to join."));
                data.set("gui.items.team_yellow.material", "YELLOW_WOOL");
                data.set("gui.items.team_yellow.name", "<#ffd23b>Yellow Team");
                data.set("gui.items.team_yellow.lore", java.util.List.of("<#94a3b8>Click to join."));
                data.set("gui.items.team_green.material", "GREEN_WOOL");
                data.set("gui.items.team_green.name", "<#3bff6f>Green Team");
                data.set("gui.items.team_green.lore", java.util.List.of("<#94a3b8>Click to join."));
                data.set("gui.items.confirm_accept.material", "LIME_CONCRETE");
                data.set("gui.items.confirm_accept.name", "<#22c55e>Confirm");
                data.set("gui.items.confirm_accept.lore", java.util.Collections.emptyList());
                data.set("gui.items.confirm_cancel.material", "RED_CONCRETE");
                data.set("gui.items.confirm_cancel.name", "<#ef4444>Cancel");
                data.set("gui.items.confirm_cancel.lore", java.util.Collections.emptyList());
                data.set("gui.items.trails_info.material", "END_ROD");
                data.set("gui.items.trails_info.name", "<#38bdf8>Trails");
                data.set("gui.items.trails_info.lore", java.util.List.of("<#94a3b8>Right-click to preview."));
                data.set("gui.items.killeffects_info.material", "NETHERITE_SWORD");
                data.set("gui.items.killeffects_info.name", "<#ef4444>Kill Effects");
                data.set("gui.items.killeffects_info.lore", java.util.List.of("<#94a3b8>Right-click to preview."));
            }
            if (!data.isSet("lobby.endCooldownSeconds")) {
                data.set("lobby.endCooldownSeconds", 10);
            }
            if (!data.isSet("lobby.items.enabled")) {
                data.set("lobby.items.enabled", true);
                data.set("lobby.items.teamSelectorSlot", 0);
                data.set("lobby.items.leaveSlot", 8);
            }
            if (!data.isSet("hub.enabled")) {
                data.set("hub.enabled", false);
            }
            if (!data.isSet("hub.location.world")) {
                data.set("hub.location.world", "");
                data.set("hub.location.x", 0);
                data.set("hub.location.y", 0);
                data.set("hub.location.z", 0);
                data.set("hub.location.yaw", 0);
                data.set("hub.location.pitch", 0);
            }
            if (!data.isSet("economy.provider")) {
                data.set("economy.provider", "vault");
            }
            if (!data.isSet("economy.currency")) {
                data.set("economy.currency", "VAULT");
            }
            if (!data.isSet("bounties.enabled")) {
                data.set("bounties.enabled", true);
            }
            if (!data.isSet("bounties.minPlayers")) {
                data.set("bounties.minPlayers", 2);
            }
            if (!data.isSet("bounties.intervalSeconds")) {
                data.set("bounties.intervalSeconds", 120);
            }
            if (!data.isSet("bounties.rewardMin")) {
                data.set("bounties.rewardMin", 5);
            }
            if (!data.isSet("bounties.rewardMax")) {
                data.set("bounties.rewardMax", 15);
            }
            if (!data.isSet("bounties.showOnScoreboard")) {
                data.set("bounties.showOnScoreboard", true);
            }
            if (!data.isSet("bounties.showBossBar")) {
                data.set("bounties.showBossBar", true);
            }
            if (!data.isSet("bounties.bossBarColor")) {
                data.set("bounties.bossBarColor", "YELLOW");
            }
            if (!data.isSet("bounties.bossBarStyle")) {
                data.set("bounties.bossBarStyle", "SOLID");
            }
            if (!data.isSet("antiabuse.enabled")) {
                data.set("antiabuse.enabled", true);
            }
            if (!data.isSet("antiabuse.kills.cooldownSeconds")) {
                data.set("antiabuse.kills.cooldownSeconds", 60);
            }
            if (!data.isSet("reconnect.enabled")) {
                data.set("reconnect.enabled", true);
            }
            if (!data.isSet("reconnect.graceSeconds")) {
                data.set("reconnect.graceSeconds", 45);
            }
            if (!data.isSet("protections.enabled")) {
                data.set("protections.enabled", true);
            }
            if (!data.isSet("protections.allowBuildInLobby")) {
                data.set("protections.allowBuildInLobby", false);
            }
            if (!data.isSet("protections.allowBuildInGame")) {
                data.set("protections.allowBuildInGame", true);
            }
            if (!data.isSet("protections.allowInteractInLobby")) {
                data.set("protections.allowInteractInLobby", false);
            }
            if (!data.isSet("protections.allowInteractInGame")) {
                data.set("protections.allowInteractInGame", true);
            }
            if (!data.isSet("protections.preventBlockExplosions")) {
                data.set("protections.preventBlockExplosions", true);
            }
            if (!data.isSet("protections.preventFireIgnite")) {
                data.set("protections.preventFireIgnite", true);
            }
            if (!data.isSet("cosmetics.trails.enabled")) {
                data.set("cosmetics.trails.enabled", true);
            }
            if (!data.isSet("cosmetics.trails.default")) {
                data.set("cosmetics.trails.default", "red_dust");
            }
            if (!data.isSet("cosmetics.trails.showInLobby")) {
                data.set("cosmetics.trails.showInLobby", false);
            }
            if (!data.isSet("cosmetics.trails.stepDistance")) {
                data.set("cosmetics.trails.stepDistance", 0.6);
            }
            if (!data.isSet("cosmetics.killEffects.enabled")) {
                data.set("cosmetics.killEffects.enabled", true);
            }
            if (!data.isSet("cosmetics.killEffects.default")) {
                data.set("cosmetics.killEffects.default", "burst_red");
            }
            if (!data.isSet("cosmetics.previewSeconds")) {
                data.set("cosmetics.previewSeconds", 3);
            }
            if (!data.isSet("cosmetics.filters.enabled")) {
                data.set("cosmetics.filters.enabled", true);
            }
            if (!data.isSet("cosmetics.sort.enabled")) {
                data.set("cosmetics.sort.enabled", true);
            }
            if (!data.isSet("cosmetics.trails.list.red_dust")) {
                data.set("cosmetics.trails.list.red_dust.particle", "DUST");
                data.set("cosmetics.trails.list.red_dust.color", "#ff3b3b");
                data.set("cosmetics.trails.list.red_dust.count", 8);
                data.set("cosmetics.trails.list.red_dust.spread", 0.2);
                data.set("cosmetics.trails.list.red_dust.size", 1.0);
                data.set("cosmetics.trails.list.red_dust.permission", "thewalls.cosmetics.trail.red_dust");
                data.set("cosmetics.trails.list.red_dust.display.name", "<#ff3b3b><bold>Red Dust</bold></#ff3b3b>");
                data.set("cosmetics.trails.list.red_dust.display.lore", java.util.List.of("<gray>Leaves a red trail</gray>"));
            }
            if (!data.isSet("cosmetics.killEffects.list.burst_red")) {
                data.set("cosmetics.killEffects.list.burst_red.particle", "DUST");
                data.set("cosmetics.killEffects.list.burst_red.color", "#ff3b3b");
                data.set("cosmetics.killEffects.list.burst_red.count", 24);
                data.set("cosmetics.killEffects.list.burst_red.spread", 0.6);
                data.set("cosmetics.killEffects.list.burst_red.size", 1.5);
                data.set("cosmetics.killEffects.list.burst_red.sound", "ENTITY_PLAYER_LEVELUP");
                data.set("cosmetics.killEffects.list.burst_red.permission", "thewalls.cosmetics.killeffect.burst_red");
                data.set("cosmetics.killEffects.list.burst_red.display.name", "<#ff3b3b><bold>Red Burst</bold></#ff3b3b>");
                data.set("cosmetics.killEffects.list.burst_red.display.lore", java.util.List.of("<gray>Explosive red kill effect</gray>"));
            }
            if (!data.isSet("events.bossMan.enabled")) {
                data.set("events.bossMan.enabled", true);
                data.set("events.bossMan.prepTime", 3);
                data.set("events.bossMan.dropRate", 0.35f);
                data.set("events.bossMan.protectionLevel", 3);
                data.set("events.bossMan.resistance", 1);
                data.set("events.bossMan.name", "Boss Man Jim");
            }
            if (!data.isSet("events.itemCheck.enabled")) {
                data.set("events.itemCheck.enabled", true);
            }
            if (!data.isSet("events.itemCheck.materials")) {
                /*
                    Item check layout
                    events
                        -> itemCheck
                            -> materials
                                -> some_id
                                    -> material: OAK_LOG
                                    -> quantity: 4
                                    -> time: 30 (seconds)
                */

                data.set("events.itemCheck.materials.oak_log.material", "OAK_LOG"); // initialize sec
                ConfigurationSection sec = data.getConfigurationSection("events.itemCheck.materials");

                sec.set("oak_log.quantity", 16);
                sec.set("oak_log.time", 30);  
                
                sec.set("diamonds.material", "DIAMOND");
                sec.set("diamonds.quantity", 8);
                sec.set("diamonds.time", 120);  

                sec.set("cobblestone.material", "COBBLESTONE");
                sec.set("cobblestone.quantity", 64);
                sec.set("cobblestone.time", 60);  
            }
            if (!data.isSet("events.bombingRun.enabled")) {
                data.set("events.bombingRun.enabled", true);
                data.set("events.bombingRun.tntPower", 16);
                data.set("events.bombingRun.tntSpread", 2);
                data.set("events.bombingRun.alertTime", 5);
                data.set("events.bombingRun.detonationtime", 10);
            }
            if (!data.isSet("arenas.list")) {
                data.set("arenas.list", new java.util.ArrayList<String>());
            }
            if (!data.isSet("arenas.settings")) {
                data.set("arenas.settings", new java.util.HashMap<String, Object>());
            }

            data.save(dataFile);

            leaderboardFile = new File(Utils.getPlugin().getDataFolder(), "leaderboard.yml");
            if (!leaderboardFile.exists()) {
                if (leaderboardFile.getParentFile().mkdirs()) {
                    Utils.getPlugin().getLogger().info("Created data folder!");
                }
                if (leaderboardFile.createNewFile()) {
                    Utils.getPlugin().getLogger().info("Created leadeboard file!");
                }
            }
            leaderboard = YamlConfiguration.loadConfiguration(leaderboardFile);

            lobbiesFile = new File(Utils.getPlugin().getDataFolder(), "lobbies.yml");
            if (!lobbiesFile.exists()) {
                if (lobbiesFile.getParentFile().mkdirs()) {
                    Utils.getPlugin().getLogger().info("Created data folder!");
                }
                if (lobbiesFile.createNewFile()) {
                    Utils.getPlugin().getLogger().info("Created lobbies file!");
                }
            }
            lobbies = YamlConfiguration.loadConfiguration(lobbiesFile);
            migrateLobbiesFromConfig();
            migrateTopHologramFromArena();

            perksFile = new File(Utils.getPlugin().getDataFolder(), "perks.yml");
            if (!perksFile.exists()) {
                Utils.getPlugin().saveResource("perks.yml", false);
            }
            perks = YamlConfiguration.loadConfiguration(perksFile);

            cratesFile = new File(Utils.getPlugin().getDataFolder(), "crates.yml");
            if (!cratesFile.exists()) {
                Utils.getPlugin().saveResource("crates.yml", false);
            }
            crates = YamlConfiguration.loadConfiguration(cratesFile);

            kitsFile = new File(Utils.getPlugin().getDataFolder(), "kits.yml");
            if (!kitsFile.exists()) {
                Utils.getPlugin().saveResource("kits.yml", false);
            }
            kits = YamlConfiguration.loadConfiguration(kitsFile);

            for (Player p : Bukkit.getOnlinePlayers()) {
                createLeaderboardPlayer(p);
            }
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static void createLeaderboardPlayer(Player p) {
        if (!Config.leaderboard.isSet(p.getPlayer().getUniqueId().toString() + ".wins")) {
            Config.leaderboard.set(p.getPlayer().getUniqueId().toString() + ".wins", 0);
        }
        if (!Config.leaderboard.isSet(p.getPlayer().getUniqueId().toString() + ".losses")) {
            Config.leaderboard.set(p.getPlayer().getUniqueId().toString() + ".losses", 0);
        }
        if (!Config.leaderboard.isSet(p.getPlayer().getUniqueId().toString() + ".kills")) {
            Config.leaderboard.set(p.getPlayer().getUniqueId().toString() + ".kills", 0);
        }
        Config.leaderboard.set(p.getPlayer().getUniqueId().toString() + ".username", p.getPlayer().getName());

        try {
            Config.leaderboard.save(Config.leaderboardFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static void setArenaLobby(String arenaName, Location loc) {
        if (arenaName == null || loc == null) return;
        String key = "arenas." + arenaName.toLowerCase() + ".lobby";
        if (lobbies == null) return;
        lobbies.set(key + ".world", loc.getWorld().getName());
        lobbies.set(key + ".x", loc.getX());
        lobbies.set(key + ".y", loc.getY());
        lobbies.set(key + ".z", loc.getZ());
        lobbies.set(key + ".yaw", loc.getYaw());
        lobbies.set(key + ".pitch", loc.getPitch());
        try {
            lobbies.save(lobbiesFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static Location getArenaLobby(String arenaName) {
        if (arenaName == null) return null;
        String key = "arenas." + arenaName.toLowerCase() + ".lobby";
        if (lobbies == null) return null;
        if (!lobbies.isSet(key + ".world")) return null;
        World world = Bukkit.getWorld(lobbies.getString(key + ".world"));
        if (world == null) return null;
        double x = lobbies.getDouble(key + ".x");
        double y = lobbies.getDouble(key + ".y");
        double z = lobbies.getDouble(key + ".z");
        float yaw = (float) lobbies.getDouble(key + ".yaw");
        float pitch = (float) lobbies.getDouble(key + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static Location getHub() {
        if (data == null) return null;
        if (!data.getBoolean("hub.enabled", false)) return null;
        String worldName = data.getString("hub.location.world", "");
        if (worldName == null || worldName.isEmpty()) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        double x = data.getDouble("hub.location.x");
        double y = data.getDouble("hub.location.y");
        double z = data.getDouble("hub.location.z");
        float yaw = (float) data.getDouble("hub.location.yaw");
        float pitch = (float) data.getDouble("hub.location.pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void setHub(Location loc) {
        if (loc == null || data == null) return;
        data.set("hub.location.world", loc.getWorld().getName());
        data.set("hub.location.x", loc.getX());
        data.set("hub.location.y", loc.getY());
        data.set("hub.location.z", loc.getZ());
        data.set("hub.location.yaw", loc.getYaw());
        data.set("hub.location.pitch", loc.getPitch());
        data.set("hub.enabled", true);
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static String getArenaGameWorld(String arenaName) {
        if (arenaName == null || data == null) return null;
        return data.getString("arenas." + arenaName.toLowerCase() + ".gameWorld", null);
    }

    public static Location getTopHologramLocation() {
        if (data == null) return null;
        String worldName = data.getString("holograms.top.location.world", "");
        if (worldName == null || worldName.isEmpty()) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        double x = data.getDouble("holograms.top.location.x");
        double y = data.getDouble("holograms.top.location.y");
        double z = data.getDouble("holograms.top.location.z");
        float yaw = (float) data.getDouble("holograms.top.location.yaw");
        float pitch = (float) data.getDouble("holograms.top.location.pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void setTopHologramLocation(Location loc) {
        if (loc == null || data == null) return;
        data.set("holograms.top.location.world", loc.getWorld().getName());
        data.set("holograms.top.location.x", loc.getX());
        data.set("holograms.top.location.y", loc.getY());
        data.set("holograms.top.location.z", loc.getZ());
        data.set("holograms.top.location.yaw", loc.getYaw());
        data.set("holograms.top.location.pitch", loc.getPitch());
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static void removeArenaLobby(String arenaName) {
        if (arenaName == null || lobbies == null) return;
        String key = "arenas." + arenaName.toLowerCase() + ".lobby";
        lobbies.set(key, null);
        try {
            lobbies.save(lobbiesFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static void setPlayerArena(java.util.UUID uuid, String arenaName) {
        if (uuid == null) return;
        if (arenaName == null || arenaName.isEmpty()) {
            data.set("players." + uuid + ".arena", null);
        } else {
            data.set("players." + uuid + ".arena", arenaName.toLowerCase());
        }
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static String getPlayerArena(java.util.UUID uuid) {
        if (uuid == null) return null;
        return data.getString("players." + uuid + ".arena", null);
    }

    public static String getPlayerKit(java.util.UUID uuid) {
        if (uuid == null || data == null) return null;
        return data.getString("players." + uuid + ".kit", null);
    }

    public static void setPlayerKit(java.util.UUID uuid, String kitId) {
        if (uuid == null || data == null) return;
        if (kitId == null || kitId.isEmpty()) {
            data.set("players." + uuid + ".kit", null);
        } else {
            data.set("players." + uuid + ".kit", kitId.toLowerCase());
        }
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static java.util.List<String> getPlayerPerks(java.util.UUID uuid) {
        if (uuid == null || data == null) return java.util.Collections.emptyList();
        return data.getStringList("players." + uuid + ".perks");
    }

    public static boolean hasPerk(java.util.UUID uuid, String perkId) {
        if (uuid == null || perkId == null) return false;
        java.util.List<String> perks = getPlayerPerks(uuid);
        for (String p : perks) {
            if (p.equalsIgnoreCase(perkId)) return true;
        }
        return false;
    }

    public static void unlockPerk(java.util.UUID uuid, String perkId) {
        if (uuid == null || perkId == null) return;
        java.util.List<String> perks = new java.util.ArrayList<>(getPlayerPerks(uuid));
        for (String p : perks) {
            if (p.equalsIgnoreCase(perkId)) {
                return;
            }
        }
        perks.add(perkId.toLowerCase());
        data.set("players." + uuid + ".perks", perks);
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static int getArenaMaxPlayers(String arenaName) {
        if (arenaName == null || data == null) return data.getInt("lobby.maxPlayers", -1);
        String key = "arenas.settings." + arenaName.toLowerCase() + ".maxPlayers";
        if (data.isSet(key)) {
            return data.getInt(key, -1);
        }
        return data.getInt("lobby.maxPlayers", -1);
    }

    public static String getResetStrategy() {
        if (data == null) return "classic";
        return data.getString("world.reset.strategy", "classic");
    }

    public static String getCopyTemplateWorld(String arenaName) {
        if (data == null) return "";
        if (arenaName != null) {
            String key = "arenas.settings." + arenaName.toLowerCase() + ".templateWorld";
            if (data.isSet(key)) {
                return data.getString(key, "");
            }
        }
        return data.getString("world.reset.copy.templateWorld", "");
    }

    public static String getCopyInstancePrefix() {
        if (data == null) return "tw_";
        return data.getString("world.reset.copy.instancePrefix", "tw_");
    }

    public static boolean isCopyDeleteOnUnload() {
        if (data == null) return true;
        return data.getBoolean("world.reset.copy.deleteOnUnload", true);
    }

    public static String getCopyTemplateDir() {
        if (data == null) return "worlds/templates";
        return data.getString("world.reset.copy.templateDir", "worlds/templates");
    }

    public static void reloadPerksAndCrates() {
        if (perksFile != null) {
            perks = YamlConfiguration.loadConfiguration(perksFile);
        }
        if (cratesFile != null) {
            crates = YamlConfiguration.loadConfiguration(cratesFile);
        }
        if (kitsFile != null) {
            kits = YamlConfiguration.loadConfiguration(kitsFile);
        }
    }

    public static void setPlayerTeamPref(java.util.UUID uuid, String arenaName, Integer teamId) {
        if (uuid == null || arenaName == null) return;
        String key = "players." + uuid + ".teamPref." + arenaName.toLowerCase();
        if (teamId == null) {
            data.set(key, null);
        } else {
            data.set(key, teamId);
        }
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static String getPlayerTrail(java.util.UUID uuid) {
        if (uuid == null || data == null) return "";
        return data.getString("players." + uuid + ".cosmetics.trail", "");
    }

    public static void setPlayerTrail(java.util.UUID uuid, String trailId) {
        if (uuid == null || data == null) return;
        String key = "players." + uuid + ".cosmetics.trail";
        data.set(key, trailId == null || trailId.isEmpty() ? null : trailId);
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static String getPlayerKillEffect(java.util.UUID uuid) {
        if (uuid == null || data == null) return "";
        return data.getString("players." + uuid + ".cosmetics.killEffect", "");
    }

    public static void setPlayerKillEffect(java.util.UUID uuid, String effectId) {
        if (uuid == null || data == null) return;
        String key = "players." + uuid + ".cosmetics.killEffect";
        data.set(key, effectId == null || effectId.isEmpty() ? null : effectId);
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static java.util.List<String> getUnlockedCosmetics(java.util.UUID uuid, String type) {
        if (uuid == null || data == null || type == null) return java.util.Collections.emptyList();
        String key = "players." + uuid + ".cosmetics.unlocked." + type;
        return data.getStringList(key);
    }

    public static boolean isCosmeticUnlocked(java.util.UUID uuid, String type, String id) {
        if (uuid == null || type == null || id == null) return false;
        java.util.List<String> list = getUnlockedCosmetics(uuid, type);
        for (String s : list) {
            if (id.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    public static void unlockCosmetic(java.util.UUID uuid, String type, String id) {
        if (uuid == null || data == null || type == null || id == null) return;
        String key = "players." + uuid + ".cosmetics.unlocked." + type;
        java.util.List<String> list = new java.util.ArrayList<>(data.getStringList(key));
        boolean exists = false;
        for (String s : list) {
            if (id.equalsIgnoreCase(s)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            list.add(id);
            data.set(key, list);
            try {
                data.save(dataFile);
            } catch (IOException ex) {
                Utils.getPlugin().getLogger().warning(ex.toString());
            }
        }
    }

    public static Integer getPlayerTeamPref(java.util.UUID uuid, String arenaName) {
        if (uuid == null || arenaName == null) return null;
        String key = "players." + uuid + ".teamPref." + arenaName.toLowerCase();
        if (!data.isSet(key)) return null;
        return data.getInt(key);
    }

    public static java.util.List<String> getArenaSigns(String arenaName) {
        if (arenaName == null) return java.util.Collections.emptyList();
        String key = "arenas." + arenaName.toLowerCase() + ".signs";
        return data.getStringList(key);
    }

    public static void addArenaSign(String arenaName, Location loc) {
        if (arenaName == null || loc == null) return;
        String key = "arenas." + arenaName.toLowerCase() + ".signs";
        java.util.List<String> list = new java.util.ArrayList<>(data.getStringList(key));
        String entry = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
        if (!list.contains(entry)) {
            list.add(entry);
            data.set(key, list);
            try {
                data.save(dataFile);
            } catch (IOException ex) {
                Utils.getPlugin().getLogger().warning(ex.toString());
            }
        }
    }

    public static void clearArenaSigns(String arenaName) {
        if (arenaName == null) return;
        String key = "arenas." + arenaName.toLowerCase() + ".signs";
        data.set(key, null);
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static java.util.Set<String> getArenaNames() {
        if (data == null) return java.util.Collections.emptySet();
        java.util.Set<String> names = new java.util.LinkedHashSet<>();
        java.util.List<String> list = data.getStringList("arenas.list");
        if (list != null && !list.isEmpty()) {
            for (String name : list) {
                if (name != null && !name.isEmpty()) {
                    names.add(name.toLowerCase());
                }
            }
            return names;
        }
        ConfigurationSection sec = data.getConfigurationSection("arenas");
        if (sec == null) return names;
        for (String key : sec.getKeys(false)) {
            if (key != null && !key.equalsIgnoreCase("list")) {
                names.add(key.toLowerCase());
            }
        }
        return names;
    }

    public static void addArenaName(String arenaName) {
        if (arenaName == null) return;
        String name = arenaName.toLowerCase();
        java.util.List<String> list = new java.util.ArrayList<>(data.getStringList("arenas.list"));
        if (!list.contains(name)) {
            list.add(name);
            data.set("arenas.list", list);
            try {
                data.save(dataFile);
            } catch (IOException ex) {
                Utils.getPlugin().getLogger().warning(ex.toString());
            }
        }
    }

    public static void removeArenaName(String arenaName) {
        if (arenaName == null) return;
        String name = arenaName.toLowerCase();
        java.util.List<String> list = new java.util.ArrayList<>(data.getStringList("arenas.list"));
        if (list.remove(name)) {
            data.set("arenas.list", list);
        }
        data.set("arenas." + name, null);
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static void incrementArenaPlays(String arenaName) {
        if (arenaName == null || data == null) return;
        String key = "stats.arenas." + arenaName.toLowerCase() + ".plays";
        int plays = data.getInt(key, 0) + 1;
        data.set(key, plays);
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static java.util.Map<String, Integer> getArenaPlays() {
        if (data == null) return java.util.Collections.emptyMap();
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        ConfigurationSection sec = data.getConfigurationSection("stats.arenas");
        if (sec == null) return map;
        for (String key : sec.getKeys(false)) {
            map.put(key, sec.getInt(key + ".plays", 0));
        }
        return map;
    }

    public static void incrementKills(java.util.UUID uuid) {
        if (uuid == null || leaderboard == null) return;
        String key = uuid.toString() + ".kills";
        int kills = leaderboard.getInt(key, 0) + 1;
        leaderboard.set(key, kills);
        try {
            leaderboard.save(leaderboardFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    public static java.util.List<java.util.Map.Entry<String, Integer>> getTopKills(int limit) {
        if (leaderboard == null) return java.util.Collections.emptyList();
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        for (String key : leaderboard.getKeys(false)) {
            int kills = leaderboard.getInt(key + ".kills", 0);
            String name = leaderboard.getString(key + ".username", key);
            map.put(name, kills);
        }
        java.util.List<java.util.Map.Entry<String, Integer>> list = new java.util.ArrayList<>(map.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        if (list.size() > limit) return list.subList(0, limit);
        return list;
    }

    private static void migrateLobbiesFromConfig() {
        if (data == null || lobbies == null) return;
        ConfigurationSection arenas = data.getConfigurationSection("arenas");
        if (arenas == null) return;
        boolean changed = false;
        for (String key : arenas.getKeys(false)) {
            String lobbyKey = "arenas." + key.toLowerCase() + ".lobby";
            if (!data.isSet(lobbyKey + ".world")) continue;
            if (lobbies.isSet(lobbyKey + ".world")) continue;
            lobbies.set(lobbyKey + ".world", data.getString(lobbyKey + ".world"));
            lobbies.set(lobbyKey + ".x", data.getDouble(lobbyKey + ".x"));
            lobbies.set(lobbyKey + ".y", data.getDouble(lobbyKey + ".y"));
            lobbies.set(lobbyKey + ".z", data.getDouble(lobbyKey + ".z"));
            lobbies.set(lobbyKey + ".yaw", data.getDouble(lobbyKey + ".yaw"));
            lobbies.set(lobbyKey + ".pitch", data.getDouble(lobbyKey + ".pitch"));
            changed = true;
        }
        if (!changed) return;
        try {
            lobbies.save(lobbiesFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }

    private static void migrateTopHologramFromArena() {
        if (data == null || lobbies == null) return;
        String worldName = data.getString("holograms.top.location.world", "");
        if (worldName != null && !worldName.isEmpty()) return;
        String arenaName = data.getString("holograms.top.arena", "");
        if (arenaName == null || arenaName.isEmpty()) return;
        Location lobby = getArenaLobby(arenaName);
        if (lobby == null) return;
        setTopHologramLocation(lobby);
        data.set("holograms.top.arena", null);
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Utils.getPlugin().getLogger().warning(ex.toString());
        }
    }
}
