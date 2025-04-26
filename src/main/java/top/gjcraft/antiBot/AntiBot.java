package top.gjcraft.antiBot;

import org.bukkit.plugin.java.JavaPlugin;
import top.gjcraft.antiBot.verification.SheepVerification;
import top.gjcraft.antiBot.listeners.VerificationListener;

public final class AntiBot extends JavaPlugin {
    private SheepVerification sheepVerification;

    @Override
    public void onEnable() {
        // 初始化验证管理器
        sheepVerification = new SheepVerification(this);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(
            new VerificationListener(sheepVerification),
            this
        );
        
        getLogger().info("AntiBot插件已启动！");
    }

    @Override
    public void onDisable() {
        getLogger().info("AntiBot插件已关闭！");
    }
}
