package top.gjcraft.antiBot.verification;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class SheepVerification {
    private final Plugin plugin;
    private final Map<Player, DyeColor> playerVerification;
    private final Random random;
    private final int sheepDisplayTime;

    public SheepVerification(Plugin plugin) {
        this.plugin = plugin;
        this.playerVerification = new HashMap<>();
        this.random = new Random();
        this.sheepDisplayTime = plugin.getConfig().getInt("verification.sheep-display-time", 60);
    }

    public void startVerification(Player player, Location location) {
        // 生成随机颜色的羊
        DyeColor randomColor = getRandomColor();

        // 生成羊并设置属性
        Location sheepLocation = location.clone();
        Sheep sheep = Objects.requireNonNull(sheepLocation.getWorld()).spawn(sheepLocation, Sheep.class);
        sheep.setAI(false); // 禁用AI防止移动
        sheep.setGravity(false); // 禁用重力防止掉落
        sheep.setColor(randomColor);

        // 保存玩家验证信息
        playerVerification.put(player, randomColor);

        // 将玩家传送到羊的位置
        player.teleport(sheepLocation);

        // 设置显示时间后移除羊并打开GUI
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            sheep.remove();
            openVerificationGUI(player);
        }, sheepDisplayTime); // 使用配置的显示时间
    }

    private void openVerificationGUI(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 27, "选择你看到的羊的颜色");

        // 添加所有可能的颜色选项
        int slot = 0;
        for (DyeColor color : DyeColor.values()) {
            ItemStack wool = new ItemStack(Material.valueOf(color.name() + "_WOOL"));
            ItemMeta meta = wool.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(getColorName(color));
            }
            wool.setItemMeta(meta);
            gui.setItem(slot++, wool);
        }

        player.openInventory(gui);
    }

    public boolean verifySelection(Player player, ItemStack selected) {
        DyeColor expectedColor = playerVerification.get(player);
        String woolName = selected.getType().name();
        DyeColor selectedColor = DyeColor.valueOf(woolName.replace("_WOOL", ""));
        boolean isCorrect = selectedColor == expectedColor;

        playerVerification.remove(player);
        return isCorrect;
    }

    private DyeColor getRandomColor() {
        DyeColor[] colors = DyeColor.values();
        return colors[random.nextInt(colors.length)];
    }

    private String getColorName(DyeColor color) {
        return switch (color) {
            case WHITE -> "§f白色";
            case ORANGE -> "§6橙色";
            case MAGENTA -> "§d品红色";
            case LIGHT_BLUE -> "§b淡蓝色";
            case YELLOW -> "§e黄色";
            case LIME -> "§a黄绿色";
            case PINK -> "§d粉色";
            case GRAY -> "§8灰色";
            case LIGHT_GRAY -> "§7淡灰色";
            case CYAN -> "§3青色";
            case PURPLE -> "§5紫色";
            case BLUE -> "§9蓝色";
            case BROWN -> "§6棕色";
            case GREEN -> "§2绿色";
            case RED -> "§c红色";
            case BLACK -> "§0黑色";
        };
    }
}