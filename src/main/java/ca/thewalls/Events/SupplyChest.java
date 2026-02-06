package ca.thewalls.Events;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.Walls.TempBlock;

import java.util.ArrayList;
import java.util.Random;

public class SupplyChest extends Event{

    ItemStack[] gearChest = new ItemStack[]{
            new ItemStack(Material.DIAMOND_AXE, 1),
            new ItemStack(Material.DIAMOND_CHESTPLATE, 1),
            new ItemStack(Material.GOLDEN_APPLE, 3),
            new ItemStack(Material.IRON_INGOT, 32)
    };
    ItemStack[] griefChest = new ItemStack[]{
            new ItemStack(Material.TNT, 32),
            new ItemStack(Material.FLINT_AND_STEEL, 1),
            new ItemStack(Material.FLINT_AND_STEEL, 1),
            new ItemStack(Material.LAVA_BUCKET, 1),
            new ItemStack(Material.LAVA_BUCKET, 1),
            new ItemStack(Material.LAVA_BUCKET, 1),
            new ItemStack(Material.LAVA_BUCKET, 1)
    };
    ItemStack[] blocksChest = new ItemStack[]{
            new ItemStack(Material.OAK_LOG, 64),
            new ItemStack(Material.COBBLESTONE, 64),
            new ItemStack(Material.COBBLESTONE, 64),
            new ItemStack(Material.COBBLESTONE, 64),
            new ItemStack(Material.COBBLESTONE, 64),
            new ItemStack(Material.OBSIDIAN, 8)
    };
    ItemStack[] enchantChest = new ItemStack[]{
            new ItemStack(Material.ENCHANTING_TABLE, 1),
            new ItemStack(Material.BOOKSHELF, 22),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
            new ItemStack(Material.LAPIS_LAZULI, 64),
            new ItemStack(Material.LAPIS_LAZULI, 64),
            new ItemStack(Material.GRINDSTONE, 1),
            new ItemStack(Material.ANVIL, 1)
    };
    ArrayList<ItemStack[]> chests = new ArrayList<>();

    public SupplyChest(String eventName, Arena arena) {
        super(eventName, arena);
        chests.add(enchantChest);
        chests.add(gearChest);
        chests.add(griefChest);
        chests.add(blocksChest);
    }

    @Override
    public void run() {
        double reducer = 1 - Config.data.getDouble("events.supplyChest.allowedRegionPercentageOfSize");
        int[] positionOne = new int[]{(int) (this.arena.getWorld().positionOne[0] - (this.arena.getGame().size * reducer)), (int) (this.arena.getWorld().positionOne[1] - (this.arena.getGame().size * reducer))};
        int[] positionTwo = new int[]{(int) (this.arena.getWorld().positionTwo[0] + (this.arena.getGame().size * reducer)), (int) (this.arena.getWorld().positionTwo[1] + (this.arena.getGame().size * reducer))};

        Random rand = new Random();
        int minX = Math.min(positionOne[0], positionTwo[0]);
        int maxX = Math.max(positionOne[0], positionTwo[0]);
        int minZ = Math.min(positionOne[1], positionTwo[1]);
        int maxZ = Math.max(positionOne[1], positionTwo[1]);

        int randX = (minX == maxX) ? minX : rand.nextInt((maxX - minX) + 1) + minX;
        int randZ = (minZ == maxZ) ? minZ : rand.nextInt((maxZ - minZ) + 1) + minZ;
        int y = this.arena.getWorld().world.getHighestBlockYAt(randX, randZ);

        Location chestLoc = new Location(this.arena.getWorld().world, randX, y + 1, randZ);
        TempBlock t = new TempBlock(chestLoc, chestLoc.getBlock().getType());
        this.arena.getWorld().originalBlocks.add(t); // Used for world resetting after the game finishes
      
        chestLoc.getBlock().setType(Material.CHEST);
        Chest chest = (Chest) chestLoc.getBlock().getState();
        int chestInv = rand.nextInt(chests.size());
        chest.getInventory().setContents(chests.get(chestInv));

        Firework firework = (Firework) this.arena.getWorld().world.spawnEntity(chestLoc.add(0, 4, 0), EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Builder fwb = FireworkEffect.builder();
        fwb.flicker(true);
        fwb.trail(true);
        fwb.with(Type.BALL_LARGE);
        fwb.withColor(Color.GREEN);
        fwb.withFade(Color.RED);
        meta.addEffect(fwb.build());
        firework.setFireworkMeta(meta);

        for (Player p : this.arena.getPlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 255, 1);
            p.playSound(chestLoc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 255, 1);
            p.sendMessage(Messages.msg("events.supply_chest"));
        }
    }
}
