package top.gjcraft.antiBot;

import org.bukkit.plugin.java.JavaPlugin;
import top.gjcraft.antiBot.verification.SheepVerification;
import top.gjcraft.antiBot.listeners.VerificationListener;

public final class AntiBot extends JavaPlugin {

    @Override
    public void onEnable() {
        // 初始化验证管理器
        SheepVerification sheepVerification = new SheepVerification(this);
        
        // 保存默认配置
        saveDefaultConfig();
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(
            new VerificationListener(this, sheepVerification),
            this
        );
        
        getLogger().info("AntiBot插件已启动!");
        getLogger().info("作者: gjyyds1");
    }

    @Override
    public void onDisable() {
        getLogger().info("AntiBot插件已关闭!");
    }
}
