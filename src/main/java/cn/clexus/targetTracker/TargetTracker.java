package cn.clexus.targetTracker;

import cn.clexus.targetTracker.commands.ReloadCommand;
import cn.clexus.targetTracker.commands.TrackerCommand;
import cn.clexus.targetTracker.listeners.EventsListener;
import cn.clexus.targetTracker.managers.PointsManager;
import cn.clexus.targetTracker.support.PlaceholderAPISupport;
import cn.clexus.targetTracker.utils.DialogUtils;
import cn.clexus.targetTracker.utils.I18n;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

import static cn.clexus.targetTracker.commands.PlayerCommand.playerCommandNode;

public final class TargetTracker extends JavaPlugin {
    private final File pointsFolder = new File(getDataFolder(), "points");
    private static TargetTracker instance;

    public static TargetTracker getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
        instance = this;
    }
    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, 26547);
        PacketEvents.getAPI().init();
        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);
        APIConfig settings = new APIConfig(PacketEvents.getAPI())
                .debugMode()
                .tickTickables()
                .useBstats()
                .usePlatformLogger();

        EntityLib.init(platform, settings);
        PointsManager pointsManager = PointsManager.getInstance();
        if (!pointsFolder.exists()) {
            pointsFolder.mkdirs();
        }
        saveDefaultConfig();
        saveDefaultExampleFile();
        pointsManager.loadPointsFromFolder(pointsFolder);
        I18n.initialize(getConfig());
        getCommand("targettracker").setExecutor(new ReloadCommand());
        getCommand("targettracker").setTabCompleter(new ReloadCommand());
        getCommand("starttrack").setExecutor(new TrackerCommand());
        getCommand("starttrack").setTabCompleter(new TrackerCommand());
        getCommand("stoptrack").setExecutor(new TrackerCommand());
        getCommand("stoptrack").setTabCompleter(new TrackerCommand());
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, c -> c.registrar().register(playerCommandNode));
        Bukkit.getPluginManager().registerEvents(new EventsListener(),this);
        Bukkit.getPluginManager().registerEvents(new DialogUtils(), this);
        DialogUtils.init(getConfig().getConfigurationSection("player-points.defaults"));
        PlaceholderAPISupport.init(this);
    }
    private void saveDefaultExampleFile() {
        File exampleFile = new File(pointsFolder, "example.yml");
        if (!exampleFile.exists()) {
            try (InputStream resourceStream = getResource("points/example.yml")) {
                if (resourceStream != null) {
                    Files.copy(resourceStream, exampleFile.toPath());
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "An error occurred when copying files:", e);
            }
        }
    }


    @Override
    public void onDisable() {
        PointsManager.getInstance().stopAllPoints();
    }
}
