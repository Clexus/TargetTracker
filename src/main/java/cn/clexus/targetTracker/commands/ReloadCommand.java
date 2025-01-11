package cn.clexus.targetTracker.commands;

import cn.clexus.targetTracker.TargetTracker;
import cn.clexus.targetTracker.managers.PointsManager;
import cn.clexus.targetTracker.utils.I18n;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {
    private final File pointsFolder = new File(TargetTracker.getInstance().getDataFolder(), "points");
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("targettracker")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                try {
                    PointsManager.getInstance().stopAllPoints();
                    PointsManager.getInstance().removeAllPoints();
                    PointsManager.getInstance().loadPointsFromFolder(pointsFolder);
                    TargetTracker.getInstance().reloadConfig();
                    I18n.initialize(TargetTracker.getInstance().getConfig());
                    I18n.sendMessage(sender,"reload",null);
                } catch (Exception e) {
                    I18n.sendMessage(sender,"reload-failed",null);
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 1) {
            return List.of("reload");
        }
        return null;
    }
}
