package top.gjcraft.antiBot.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import top.gjcraft.antiBot.verification.SheepVerification;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import java.util.HashMap;
import java.util.Map;

public class VerificationListener implements Listener {
    private final SheepVerification verification;
    private final Map<Player, BukkitTask> timeoutTasks = new HashMap<>();
    private final Plugin plugin;
    private final int timeout;
    private final ProtocolManager protocolManager;

    public VerificationListener(Plugin plugin, SheepVerification verification) {
        this.plugin = plugin;
        this.verification = verification;
        this.timeout = plugin.getConfig().getInt("verification.timeout", 30);
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 获取世界出生点
        World world = player.getWorld();
        Location spawnLocation = world.getSpawnLocation();
        Location verificationLocation = spawnLocation.clone().add(0, 3, 0); // 在出生点上方3格处进行验证
        
        // 在位置下方放置屏障方块
        Location barrierLoc = verificationLocation.clone().subtract(0, 1, 0);
        barrierLoc.getBlock().setType(Material.BARRIER);
        
        // 确保玩家不会受到伤害并添加夜视效果
        player.setInvulnerable(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
        
        // 使用ProtocolLib设置最小视距
        PacketContainer viewDistancePacket = protocolManager.createPacket(PacketType.Play.Server.VIEW_DISTANCE);
        viewDistancePacket.getIntegers().write(0, 1);
        try {
            protocolManager.sendServerPacket(player, viewDistancePacket);
        } catch (Exception e) {
            plugin.getLogger().warning("无法设置玩家视距: " + e.getMessage());
        }

        // 取消原版加入消息
        event.setJoinMessage(null);

        // 传送玩家到验证位置
        player.teleport(verificationLocation);
        
        // 开始验证过程
        verification.startVerification(player, verificationLocation);
        
        // 设置验证超时
        BukkitTask timeoutTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (timeoutTasks.containsKey(player)) {
                player.kickPlayer("§c验证超时！请重新进入服务器进行验证。");
                timeoutTasks.remove(player);
            }
        }, timeout * 20L);
        timeoutTasks.put(player, timeoutTask);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("选择你看到的羊的颜色")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        // 验证选择
        boolean isCorrect = verification.verifySelection(player, clicked);
        
        if (isCorrect) {
            // 验证成功
            player.sendMessage("§a验证成功！欢迎进入服务器。");
            player.setInvulnerable(false);
            // 移除夜视效果
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            // 传送到出生点
            player.teleport(player.getWorld().getSpawnLocation());
            // 取消超时任务
            if (timeoutTasks.containsKey(player)) {
                timeoutTasks.get(player).cancel();
                timeoutTasks.remove(player);
            }
            // 恢复默认视距
            PacketContainer resetViewDistancePacket = protocolManager.createPacket(PacketType.Play.Server.VIEW_DISTANCE);
            resetViewDistancePacket.getIntegers().write(0, plugin.getServer().getViewDistance());
            try {
                protocolManager.sendServerPacket(player, resetViewDistancePacket);
            } catch (Exception e) {
                plugin.getLogger().warning("无法重置玩家视距: " + e.getMessage());
            }
            // 发送原版加入消息
            plugin.getServer().broadcastMessage("§e" + player.getName() + " joined the game");
        } else {
            // 验证失败
            player.kickPlayer("§c验证失败！请重新进入服务器进行验证。");
        }
        
        player.closeInventory();
    }
}