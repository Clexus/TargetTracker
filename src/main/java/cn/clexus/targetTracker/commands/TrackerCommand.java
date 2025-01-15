package cn.clexus.targetTracker.commands;

import cn.clexus.targetTracker.managers.PointsManager;
import cn.clexus.targetTracker.points.Point;
import cn.clexus.targetTracker.utils.I18n;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrackerCommand implements CommandExecutor, TabCompleter {
    PointsManager pointsManager = PointsManager.getInstance();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            if(label.equalsIgnoreCase("stoptrack")&&args.length==1) {
                Player targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer == null) {
                    I18n.sendMessage(sender,"player-not-exist",Map.of("player", args[0]));
                    return true;
                }
                if(PointsManager.getInstance().stopAllPointsForPlayer(targetPlayer,false)){
                    I18n.sendMessage(sender,"all-tracks-stopped",Map.of("player", args[0]));
                }else{
                    I18n.sendMessage(sender,"all-tracks-stopped-failed",Map.of("player", args[0]));
                }
                return true;
            }
            I18n.sendMessage(sender,"usage", Map.of("label", label));
            return true;
        }

        String playerName = args[0];
        String pointId = args[1];
        boolean triggerActions = args.length > 2 && Boolean.parseBoolean(args[2]);

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            I18n.sendMessage(sender,"player-not-exist",Map.of("player", playerName));
            return true;
        }

        Point point = pointsManager.getPointById(pointId);
        if (point == null) {
            I18n.sendMessage(sender,"point-not-found",Map.of("point", pointId));
            return true;
        }

        if (label.equalsIgnoreCase("startTrack")) {
            if(pointsManager.startTrack(point, targetPlayer)){
                I18n.sendMessage(sender,"track-started",Map.of("point", pointId,"player", playerName));
            }else{
                I18n.sendMessage(sender,"track-start-failed",Map.of("point", pointId,"player", playerName));
            }
        } else if (label.equalsIgnoreCase("stopTrack")) {
            if(pointsManager.stopTrack(targetPlayer, point, triggerActions)){
                I18n.sendMessage(sender,"track-stopped",Map.of("point", pointId,"player", playerName,"trigger",String.valueOf(triggerActions)));
            }else{
                I18n.sendMessage(sender,"track-stopped-failed",Map.of("point", pointId,"player", playerName));
            }
        }

        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            // 补全在线玩家名
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // 补全点的 ID
            return pointsManager.getAllPoints().stream()
                    .map(Point::getId) // 提取点的 ID
                    .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && label.equalsIgnoreCase("stopTrack")) {
            // 补全 true 或 false
            return List.of("true", "false");
        }

        return Collections.emptyList();
    }
}
