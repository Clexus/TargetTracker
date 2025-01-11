package cn.clexus.targetTracker.utils;

import cn.clexus.targetTracker.TargetTracker;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class FireworkHandler {

    /**
     * 根据配置字符串生成烟花效果。
     *
     * @param configString 配置字符串。
     * @param player       触发的玩家对象，可为空。
     * @param target       目标位置，可为空。
     */
    public static void spawnFirework(String configString, Player player, Location target) {
        try {
            // 按照新的格式解析配置
            String[] parts = configString.split(";");
            if (parts.length != 5) {
                throw new IllegalArgumentException("Invalid firework configuration: " + configString);
            }

            // 解析位置
            Location spawnLocation = parseLocation(parts[0], player, target);
            if (spawnLocation == null) {
                throw new IllegalArgumentException("Failed to resolve spawn location from: " + parts[0]);
            }

            // 解析颜色、类型、渐变颜色和飞行高度
            List<Color> colors = parseColors(parts[1]);
            FireworkEffect.Type type = parseFireworkType(parts[2]);
            List<Color> fadeColors = parseColors(parts[3]);
            int power = Integer.parseInt(parts[4]);

            // 创建烟花实体
            Firework firework = spawnLocation.getWorld().spawn(spawnLocation, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();

            // 设置烟花效果
            FireworkEffect effect = FireworkEffect.builder()
                    .withColor(colors)
                    .withFade(fadeColors)
                    .with(type)
                    .trail(true)
                    .flicker(true)
                    .build();

            meta.addEffect(effect);
            meta.setPower(power);
            firework.setFireworkMeta(meta);
            if(power == 0) {
                firework.detonate();
            }
        } catch (Exception e) {
            TargetTracker.getInstance().getLogger().log(Level.WARNING ,"Failed to spawn firework: " + e.getMessage());
        }
    }

    /**
     * 解析颜色列表。
     * @param colorString 颜色字符串，格式如 "#123456,#654321"。
     * @return 解析后的颜色列表。
     */
    private static List<Color> parseColors(String colorString) {
        List<Color> colors = new ArrayList<>();
        String[] colorParts = colorString.split(",");
        for (String colorPart : colorParts) {
            colors.add(Color.fromRGB(
                    Integer.valueOf(colorPart.substring(1, 3), 16),
                    Integer.valueOf(colorPart.substring(3, 5), 16),
                    Integer.valueOf(colorPart.substring(5, 7), 16)));
        }
        return colors;
    }

    /**
     * 解析烟花类型。
     * @param typeString 类型字符串。
     * @return 对应的 FireworkEffect.Type。
     */
    private static FireworkEffect.Type parseFireworkType(String typeString) {
        return switch (typeString.toUpperCase()) {
            case "BALL" -> FireworkEffect.Type.BALL;
            case "BALL_LARGE" -> FireworkEffect.Type.BALL_LARGE;
            case "STAR" -> FireworkEffect.Type.STAR;
            case "BURST" -> FireworkEffect.Type.BURST;
            case "CREEPER" -> FireworkEffect.Type.CREEPER;
            default -> throw new IllegalArgumentException("Invalid firework type: " + typeString);
        };
    }

    /**
     * 解析生成烟花的位置。
     *
     * @param locationString 位置字符串，可以是 `player`、`target` 或世界坐标。
     * @param player         触发的玩家。
     * @param target         目标位置。
     * @return 解析后的 Location。
     */
    private static Location parseLocation(String locationString, Player player, Location target) {
        if ("player".equalsIgnoreCase(locationString)) {
            return player != null ? player.getEyeLocation() : null;
        } else if ("target".equalsIgnoreCase(locationString)) {
            return target;
        } else {
            String[] parts = locationString.split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid location format: " + locationString);
            }
            World world = Bukkit.getWorld(parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            return new Location(world, x, y, z);
        }
    }
}

