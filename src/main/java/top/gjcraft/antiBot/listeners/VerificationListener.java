package top.gjcraft.antiBot.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import top.gjcraft.antiBot.verification.SheepVerification;

public class VerificationListener implements Listener {
    private final SheepVerification verification;

    public VerificationListener(SheepVerification verification) {
        this.verification = verification;
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
        
        // 传送玩家到验证位置
        player.teleport(verificationLocation);
        
        // 开始验证过程
        verification.startVerification(player, verificationLocation);
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
        } else {
            // 验证失败
            player.kickPlayer("§c验证失败！请重新进入服务器进行验证。");
        }
        
        player.closeInventory();
    }
}