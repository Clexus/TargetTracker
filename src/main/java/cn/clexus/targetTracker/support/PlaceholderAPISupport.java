package cn.clexus.targetTracker.support;


import cn.clexus.targetTracker.TargetTracker;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderAPISupport {
    static PlaceholderAPIPlugin papiPlugin;
    private static boolean hasSupport = false;
    public static boolean hasSupport(){
        return hasSupport;
    }
    public static void init(TargetTracker plugin) {
        try{
            Plugin PAPI = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
            if (!(PAPI instanceof PlaceholderAPIPlugin)) {
                return;
            }
            String papiVersion = PAPI.getPluginMeta().getVersion();
            plugin.getLogger().info("PlaceholderAPI version: " + papiVersion + " found");
            Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
            Matcher matcher = pattern.matcher(papiVersion);
            if (matcher.find()) { // Check if there is a match
                int middleNumber = Integer.parseInt(matcher.group(2));
                int lastNumber = Integer.parseInt(matcher.group(3));
                if (middleNumber < 10 || (middleNumber == 10 && lastNumber < 2)) {
                    plugin.getLogger().warning("Requires PlaceholderAPI 2.10.2 or later, disabling integration");
                    hasSupport = false;
                    return;
                }
            } else {
                plugin.getLogger().warning("Failed to parse PlaceholderAPI version, disabling integration");
                hasSupport = false;
                return;
            }
            hasSupport = true;
        }catch (Exception e){
            plugin.getLogger().log(Level.WARNING, "Error enabling PlaceholderAPI support", e);
            hasSupport = false;
        }
        if(hasSupport()){
            new PlaceholderAPIExpansion(plugin).register();
        }
    }
}
