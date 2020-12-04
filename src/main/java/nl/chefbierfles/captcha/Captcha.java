package nl.chefbierfles.captcha;

import nl.chefbierfles.captcha.listener.*;
import nl.chefbierfles.captcha.managers.ConfigManager;
import nl.chefbierfles.captcha.managers.ModuleManager;
import nl.chefbierfles.captcha.module.CaptchaModule;
import nl.chefbierfles.captcha.module.DatabaseModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class Captcha extends JavaPlugin {

    public ModuleManager moduleManager;
    public ConfigManager configManager;

    @Override
    public void onEnable() {

        configManager = new ConfigManager();
        moduleManager = new ModuleManager();

        getServer().getPluginManager().registerEvents(new InventoryClickEventListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitEventListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractEventListener(), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatEventListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveEventListener(), this);

        moduleManager.registerModule(new DatabaseModule());
        moduleManager.registerModule(new CaptchaModule(moduleManager.getDatabaseModule()));
    }

    @Override
    public void onDisable() { }
}
