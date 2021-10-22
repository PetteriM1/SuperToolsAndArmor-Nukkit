package me.petterim1.staa;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.potion.Effect;

public class Main extends PluginBase implements Listener {

    public void onEnable() {
        getServer().getScheduler().scheduleDelayedRepeatingTask(this, this::checkEffects, 10, 10);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent ev) {
        Item it = ev.getItem();
        if (it.isAxe() || it.isPickaxe() || it.isShovel() || it.isHoe()) {
            CompoundTag tag = it.getNamedTag();
            if (tag == null) return;
            int lvl = tag.getInt("super_lvl");
            if (lvl > 0) {
                Block bl = ev.getBlock();
                Vector3 temp = new Vector3(0, 0, 0);
                for (int x = bl.getFloorX() - 1; x < bl.getFloorX() + 2; x++) {
                    for (int y = bl.getFloorY() - 1; y < bl.getFloorY() + 2; y++) {
                        for (int z = bl.getFloorZ() - 1; z < bl.getFloorZ() + 2; z++) {
                            if (!(x == bl.getFloorX() && y == bl.getFloorY() && z == bl.getFloorZ())) {
                                bl.getLevel().useBreakOn(temp.setComponents(x, y, z), it, null, true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent ev) {
        if (ev.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        Item it = ev.getItem();
        if (it.isHoe()) {
            CompoundTag tag = it.getNamedTag();
            if (tag == null) return;
            int lvl = tag.getInt("super_lvl");
            if (lvl > 0) {
                Block bl = ev.getBlock();
                for (int x = bl.getFloorX() - 1; x < bl.getFloorX() + 2; x++) {
                    for (int y = bl.getFloorY() - 1; y < bl.getFloorY() + 2; y++) {
                        for (int z = bl.getFloorZ() - 1; z < bl.getFloorZ() + 2; z++) {
                            if (!(x == bl.getFloorX() && y == bl.getFloorY() && z == bl.getFloorZ())) {
                                Block at = bl.getLevel().getBlock(x, y, z);
                                if (at.getId() == BlockID.DIRT || at.getId() == BlockID.GRASS) {
                                    if (at.up().getId() == BlockID.AIR) {
                                        it.useOn(at);
                                        bl.getLevel().setBlock(at, at.getDamage() == 0 ? Block.get(BlockID.FARMLAND) : Block.get(BlockID.DIRT), true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (it.isShovel()) {
            CompoundTag tag = it.getNamedTag();
            if (tag == null) return;
            int lvl = tag.getInt("super_lvl");
            if (lvl > 0) {
                Block bl = ev.getBlock();
                for (int x = bl.getFloorX() - 1; x < bl.getFloorX() + 2; x++) {
                    for (int y = bl.getFloorY() - 1; y < bl.getFloorY() + 2; y++) {
                        for (int z = bl.getFloorZ() - 1; z < bl.getFloorZ() + 2; z++) {
                            if (!(x == bl.getFloorX() && y == bl.getFloorY() && z == bl.getFloorZ())) {
                                Block at = bl.getLevel().getBlock(x, y, z);
                                if (at.getId() == BlockID.DIRT || at.getId() == BlockID.GRASS) {
                                    if (at.up().getId() == BlockID.AIR) {
                                        it.useOn(at);
                                        bl.getLevel().setBlock(at, Block.get(BlockID.GRASS_PATH), true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent ev) {
        if (ev.getDamager() instanceof Player) {
            Player pl = (Player) ev.getDamager();
            Item it = pl.getInventory().getItemInHand();
            if (it.isSword()) {
                CompoundTag tag = it.getNamedTag();
                if (tag == null) return;
                int lvl = Math.min(tag.getInt("super_lvl"), 2);
                if (lvl > 0) {
                    ev.setDamage(ev.getDamage() * (1f + (0.5f * lvl)));
                }
            }
        }
    }

    private void checkEffects() {
        for (Player player : getServer().getOnlinePlayers().values()) {
            PlayerInventory inv = player.getInventory();
            if (inv != null) {
                int lvl;
                CompoundTag tag = inv.getHelmet().getNamedTag();
                if (tag != null) {
                    lvl = Math.min(tag.getInt("super_lvl"), 2);
                    if (lvl > 0) {
                        player.addEffect(Effect.getEffect(Effect.NIGHT_VISION).setVisible(false).setDuration(220)); //longer duration to avoid client side blinking on effect about to end
                    }
                }
                tag = inv.getLeggings().getNamedTag();
                if (tag != null) {
                    lvl = Math.min(tag.getInt("super_lvl"), 2);
                    if (lvl > 0) {
                        player.addEffect(Effect.getEffect(Effect.SPEED).setAmplifier(lvl - 1).setVisible(false).setDuration(40));
                    }
                }
                tag = inv.getChestplate().getNamedTag();
                if (tag != null) {
                    lvl = Math.min(tag.getInt("super_lvl"), 2);
                    if (lvl > 0) {
                        int health = 20 + (lvl * 4);
                        if (player.getMaxHealth() < health) {
                            player.setMaxHealth(health);
                        }
                    }
                } else {
                    if (player.getMaxHealth() > 20) {
                        player.setMaxHealth(20);
                    }
                }
                tag = inv.getBoots().getNamedTag();
                if (tag != null) {
                    lvl = Math.min(tag.getInt("super_lvl"), 2);
                    if (lvl > 0) {
                        player.addEffect(Effect.getEffect(Effect.JUMP).setAmplifier(lvl - 1).setVisible(false).setDuration(40));
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("supergive")) {
            if (args.length < 2) {
                return false;
            }
            Player pl = getServer().getPlayer(args[0].replace("@s", sender.getName()));
            if (pl == null) {
                sender.sendMessage("§cUnknown player");
                return true;
            }
            int lvl = 0;
            if (args.length > 2) {
                try {
                    lvl = Integer.parseInt(args[2]);
                } catch (Exception ignore) {
                }
                if (lvl != 1 && lvl != 2) {
                    sender.sendMessage("§cLevel must be a number [1-2]");
                    return true;
                }
            } else {
                lvl = 1;
            }
            int type;
            switch (args[1].toLowerCase()) {
                case "axe":
                    type = lvl == 1 ? ItemID.GOLD_AXE : ItemID.NETHERITE_AXE;
                    break;
                case "hoe":
                    type = lvl == 1 ? ItemID.GOLD_HOE : ItemID.NETHERITE_HOE;
                    break;
                case "pickaxe":
                    type = lvl == 1 ? ItemID.GOLD_PICKAXE : ItemID.NETHERITE_PICKAXE;
                    break;
                case "shovel":
                    type = lvl == 1 ? ItemID.GOLD_SHOVEL : ItemID.NETHERITE_SHOVEL;
                    break;
                case "sword":
                    type = lvl == 1 ? ItemID.GOLD_SWORD : ItemID.NETHERITE_SWORD;
                    break;
                case "helmet":
                    type = lvl == 1 ? ItemID.GOLD_HELMET : ItemID.NETHERITE_HELMET;
                    break;
                case "chestplate":
                    type = lvl == 1 ? ItemID.GOLD_CHESTPLATE : ItemID.NETHERITE_CHESTPLATE;
                    break;
                case "leggings":
                    type = lvl == 1 ? ItemID.GOLD_LEGGINGS : ItemID.NETHERITE_LEGGINGS;
                    break;
                case "boots":
                    type = lvl == 1 ? ItemID.GOLD_BOOTS : ItemID.NETHERITE_BOOTS;
                    break;
                default:
                    sender.sendMessage("§cUnknown item. §7Items available: axe, hoe, pickaxe, shovel, sword, helmet, chestplate, leggings, boots");
                    return true;
            }
            Item it = Item.get(type, 0, 1);
            it.setNamedTag(new CompoundTag().putInt("super_lvl", lvl));
            it.setLore("§lSuper " + lvl);
            if (!pl.getInventory().canAddItem(it)) {
                sender.sendMessage("§cNo space left in player's inventory");
            }
            pl.getInventory().addItem(it);
            sender.sendMessage("§aItem given to " + pl.getName());
            return true;
        }
        return false;
    }
}
