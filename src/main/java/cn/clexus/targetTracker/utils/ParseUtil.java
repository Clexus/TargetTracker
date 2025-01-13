package cn.clexus.targetTracker.utils;

import cn.clexus.targetTracker.points.Point;
import cn.clexus.targetTracker.support.PlaceholderAPISupport;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class ParseUtil {
    public static String parsePlaceholders(String str, Point point, Player player) {
        str = str.replaceAll("%player%", player.getName()).replaceAll("%distance%", String.valueOf(player.getLocation().distance(point.getTarget().getLocation())));
        if(PlaceholderAPISupport.hasSupport()){
            str = PlaceholderAPI.setPlaceholders(player, str);
        }
        return str;
    }
}
