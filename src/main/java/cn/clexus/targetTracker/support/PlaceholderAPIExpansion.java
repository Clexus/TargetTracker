package cn.clexus.targetTracker.support;

import cn.clexus.targetTracker.TargetTracker;
import cn.clexus.targetTracker.managers.PointsManager;
import cn.clexus.targetTracker.points.Point;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    public static TargetTracker plugin; //

    public PlaceholderAPIExpansion(TargetTracker plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors()); //
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "targettracker";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion(); //
    }

    @Override
    public boolean persist() {
        return true; //
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if(!offlinePlayer.isOnline()) return "";
        Player player = (Player) offlinePlayer;
        String[] param = params.split("_");
        String error = "%targettracker_active/all_amount%\n" +
                "%targettracker_active/all_nearest/furthest_distance%\n" +
                "%targettracker_active/all_nearest/furthest_markdisplay%\n" +
                "%targettracker_active/all_nearest/furthest_targetdisplay%\n" +
                "%targettracker_active/all_nearest/furthest_id%\n"+
                "%targettracker_point_<id>_distance%\n" +
                "%targettracker_point_<id>_markdisplay%\n" +
                "%targettracker_point_<id>_targetdisplay%\n";
        if (param.length != 3 && param.length != 2) {
            return error;
        } else if (param.length == 2 && param[1].equals("amount")) {
            if(param[0].equals("active")){
                return String.valueOf(PointsManager.getInstance().getAllActivePoints(player).size());
            }else if(param[0].equals("all")){
                return String.valueOf(PointsManager.getInstance().getAllPoints().size());
            }else{
                return error;
            }
        } else{
            if(!param[0].equals("point")) {
                List<Point> points = null;
                if(param[0].equals("active")) {
                    points = PointsManager.getInstance().getAllActivePoints(player);
                }else if(param[0].equals("all")) {
                    points = PointsManager.getInstance().getAllPoints().stream().toList();
                }
                if(points == null||points.isEmpty()) return "";
                Point point = null;
                if(param[1].equals("nearest")) {
                    double distance = Double.MAX_VALUE;
                    for(Point p : points) {
                        if(player.getLocation().distance(p.getTarget().getLocation())<distance) {
                            distance = player.getLocation().distance(p.getTarget().getLocation());
                            point = p;
                        }
                    }
                }else if(param[1].equals("furthest")) {
                    double distance = 0;
                    for(Point p : points) {
                        if(player.getLocation().distance(p.getTarget().getLocation())>distance) {
                            distance = player.getLocation().distance(p.getTarget().getLocation());
                            point = p;
                        }
                    }
                }
                if(point == null) return "";
                return switch (param[2]) {
                    case "distance" -> String.valueOf(point.getTarget().getLocation().distance(player.getLocation()));
                    case "markdisplay" -> point.getMark().getDisplay().toString();
                    case "targetdisplay" -> point.getTarget().getDisplay().toString();
                    case "id" -> point.getId();
                    default -> error;
                };
            }else{
                String id = param[1];
                Point point = PointsManager.getInstance().getPointById(id);
                if(point == null) return "";
                return switch (param[2]) {
                    case "distance" -> String.valueOf(point.getTarget().getLocation().distance(player.getLocation()));
                    case "markdisplay" -> point.getMark().getDisplay().toString();
                    case "targetdisplay" -> point.getTarget().getDisplay().toString();
                    default -> error;
                };
            }
        }
    }
}
