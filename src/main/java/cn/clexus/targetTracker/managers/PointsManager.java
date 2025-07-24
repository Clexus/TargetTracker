package cn.clexus.targetTracker.managers;

import cn.clexus.targetTracker.TargetTracker;
import cn.clexus.targetTracker.events.TrackStartEvent;
import cn.clexus.targetTracker.events.TrackStopEvent;
import cn.clexus.targetTracker.points.*;
import cn.clexus.targetTracker.utils.FireworkHandler;
import cn.clexus.targetTracker.utils.I18n;
import cn.clexus.targetTracker.utils.ParseUtil;
import com.github.retrooper.packetevents.protocol.world.Location;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static cn.clexus.targetTracker.utils.I18n.mm;
import static cn.clexus.targetTracker.utils.I18n.replaceLegacyColorCodes;
import static cn.clexus.targetTracker.utils.PacketUtils.*;

public class PointsManager {
    public enum CommandResult {
        NOT_FOUND,
        ALREADY_TRACKING,
        CANCELLED,
        SUCCESS
    }

    private static final PointsManager instance = new PointsManager();
    private final Map<String, Point> points = new HashMap<>();
    private final Map<String, TrackTask> activeTracks = new HashMap<>();
    private final Map<UUID, List<Point>> savedPoints = new HashMap<>();

    public static PointsManager getInstance() {
        return instance;
    }

    public void sendShareMessage(Point point, List<Player> players) {
        org.bukkit.Location location = point.getTarget().getLocation();
        Component message = I18n.getMessage("point-share-message", Map.of("player", point.getCreator().getName(),
                "display", PlainTextComponentSerializer.plainText().serialize(point.getDisplay()),
                "location", location.getBlockX()+", "+location.getBlockY()+", "+location.getBlockZ()));
        message = message.clickEvent(ClickEvent.callback(audience -> {
            if(audience instanceof Player player) {
                PointsManager.getInstance().startTrack(point, player);
            }
        }));
        for (Player player : players) {
            if(player == point.getCreator()) continue;
            player.sendMessage(message);
        }
    }

    public int getMarkEntityId(Point point, Player player) {
        TrackTask task = activeTracks.get(player.getUniqueId() + ":" + point.getId());
        if (task != null) {
            return task.getMarkEntityId();
        }
        return -1;
    }

    public int getTargetEntityId(Point point, Player player) {
        TrackTask task = activeTracks.get(player.getUniqueId() + ":" + point.getId());
        if (task != null) {
            return task.getTargetEntityId();
        }
        return -1;
    }

    public CommandResult startTrack(Point point, Player player) {
        TrackStartEvent event = new TrackStartEvent(player, point);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return CommandResult.CANCELLED;
        }
        if (activeTracks.containsKey(player.getUniqueId() + ":" + point.getId())) {
            return CommandResult.ALREADY_TRACKING;
        }
        Location playerLocation = new Location(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        Location targetLocation = new Location(
                point.getTarget().getLocation().getX(),
                point.getTarget().getLocation().getY(),
                point.getTarget().getLocation().getZ(),
                0,
                0
        );
        double initialDistance = point.getMark().getDistance();
        int markEntityId = Bukkit.getUnsafe().nextEntityId();
        int targetEntityId = Bukkit.getUnsafe().nextEntityId();
        int beamEntityId = 0;
        int markBelowId = sendSpawnPacket(player, point.getMark(), markEntityId, playerLocation);
        int targetBelowId = sendSpawnPacket(player, point.getTarget(), targetEntityId, targetLocation);
        if (point.getTarget().getBeam() != null) {
            beamEntityId = Bukkit.getUnsafe().nextEntityId();
            sendBeamPacket(player, point.getTarget().getBeam(), beamEntityId, targetLocation);
        }
        int distance = 0;
        if (player.getWorld() == point.getTarget().getLocation().getWorld()) {
            distance = (int) player.getLocation().distance(point.getTarget().getLocation());
        }
        updateProperties(player, point, markEntityId, point.getMark().getDisplay(), distance);
        updateProperties(player, point, targetEntityId, point.getTarget().getDisplay(), distance);
        TrackTask task = new TrackTask(player, point, markEntityId, markBelowId, targetEntityId, targetBelowId, beamEntityId, point.getTarget().getLocation(), initialDistance);
        task.runTaskTimer(TargetTracker.getInstance(), 0L, 1L);
        activeTracks.put(player.getUniqueId() + ":" + point.getId(), task);
        return CommandResult.SUCCESS;
    }

    public CommandResult stopTrack(Player player, Point point, boolean trigger) {
        TrackStopEvent event = new TrackStopEvent(player, point, trigger);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return CommandResult.CANCELLED;
        }
        TrackTask task = activeTracks.remove(player.getUniqueId() + ":" + point.getId());
        if (task != null) {
            task.cancel();
            task.removeEntities();
            if (trigger) {
                executeActions(point, player);
            }
            return CommandResult.SUCCESS;
        }

        return CommandResult.NOT_FOUND;
    }


    private class TrackTask extends BukkitRunnable {
        private final Player player;
        private final Point point;
        private final int markEntityId;
        private final int markBelowId;
        private final int targetEntityId;
        private final int targetBelowId;
        private final int beamId;
        private final org.bukkit.Location target;
        private final double initialDistance;
        private int opacity = 255;
        private float yaw = 0;

        public TrackTask(Player player, Point point, int markEntityId, int markBelowId, int targetEntityId, int targetBelowId, int beamId, org.bukkit.Location target, double initialDistance) {
            this.player = player;
            this.point = point;
            this.markEntityId = markEntityId;
            this.markBelowId = markBelowId;
            this.targetEntityId = targetEntityId;
            this.targetBelowId = targetBelowId;
            this.beamId = beamId;
            this.target = target;
            this.initialDistance = initialDistance;
        }

        public double getInitialDistance() {
            return initialDistance;
        }

        public Point getPoint() {
            return point;
        }

        public int getMarkEntityId() {
            return markEntityId;
        }

        public int getMarkBelowId() {
            return markBelowId;
        }

        public int getTargetEntityId() {
            return targetEntityId;
        }

        public int getTargetBelowId() {
            return targetBelowId;
        }

        public int getBeamId() {
            return beamId;
        }

        public int getOpacity() {
            return opacity;
        }

        public org.bukkit.Location getTargetLocation() {
            return target;
        }

        public Player getPlayer() {
            return player;
        }

        @Override
        public void run() {
            if (!player.isOnline()) {
                return;
            }
            if (target.getWorld() != player.getWorld()) {
                return;
            }

            org.bukkit.Location playerLocation = player.getEyeLocation();
            Vector direction = target.toVector().subtract(playerLocation.toVector()).normalize();
            double playerToTargetDistance = player.getLocation().distance(target);

            double distance = Math.min(initialDistance, playerToTargetDistance - 0.5);
            int intDistance = (int) Math.ceil(playerToTargetDistance);
            updateProperties(player, point, markEntityId, point.getMark().getDisplay(), intDistance);
            updateProperties(player, point, targetEntityId, point.getTarget().getDisplay(), intDistance);
            if (playerToTargetDistance <= 3) {
                // 距离小于等于3格，标记固定在目标点
                org.bukkit.Location targetLocation = point.getTarget().getLocation().clone();
                sendTeleportPacket(player, markBelowId == 0 ? markEntityId : markBelowId, targetLocation.add(0, -0.5, 0));
            } else {
                // 距离大于等于触发距离，更新标记点位置
                org.bukkit.Location newLocation = playerLocation.clone().add(direction.multiply(distance)).add(0, -0.5, 0);
                sendTeleportPacket(player, markBelowId == 0 ? markEntityId : markBelowId, newLocation);
            }
            if (playerToTargetDistance < point.getTriggerDistance()) {
                // 距离小于触发距离但大于3格，正常更新标记点位置
                opacity = Math.max(25, opacity - point.getFadeSpeed());

                // 若透明度降到最低，停止追踪
                if (opacity == 25) {
                    stopTrack(player, point, true);
                    return;
                }
            } else {
                opacity = Math.min(255, opacity + point.getFadeSpeed());
            }

            byte displayOpacity = (byte) (opacity > 127 ? opacity - 256 : opacity);
            float markScale = (opacity - 25f) / 230 * point.getMark().getScale();
            float targetScale = (opacity - 25f) / 230 * point.getTarget().getScale();
            updateScaleAndOpacity(player, markEntityId, markBelowId, displayOpacity, markScale);
            updateScaleAndOpacity(player, targetEntityId, targetBelowId, displayOpacity, targetScale);
            if(beamId != 0) {
                yaw += point.getTarget().getBeam().spinSpeed();
                spinBeam(player, beamId, point.getTarget().getBeam(), opacity, point.getTarget().getLocation().clone(), yaw);
            }
        }

        public void removeEntities() {
            sendDestroyPacket(player, markEntityId);
            sendDestroyPacket(player, targetEntityId);
            if (targetBelowId != 0) {
                sendDestroyPacket(player, targetBelowId);
            }
            if (markBelowId != 0) {
                sendDestroyPacket(player, markBelowId);
            }
            if (beamId != 0) {
                sendDestroyPacket(player, beamId);
            }
        }
    }

    public void loadPointsFromFolder(File folder) {
        Logger logger = TargetTracker.getInstance().getLogger();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            logger.info("points 文件夹中没有点配置文件。");
            return;
        }
        for (File file : files) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                loadPoints(config);
                logger.info("成功加载点配置文件: " + file.getName());
            } catch (Exception e) {
                logger.log(Level.WARNING, "加载点配置文件失败: " + file.getName(), e);
            }
        }
    }

    /**
     * 加载配置文件中的所有点。
     *
     * @param config 配置文件对象。
     */
    public void loadPoints(FileConfiguration config) {
        for (String pointId : config.getKeys(false)) {
            ConfigurationSection pointSection = config.getConfigurationSection(pointId);
            if (pointSection == null) continue;

            // 加载 target
            ConfigurationSection targetSection = pointSection.getConfigurationSection("target");
            Target target = null;
            if (targetSection != null) {
                String input = targetSection.getString("location");
                String[] parts = null;
                if (input != null) {
                    parts = input.split(",");
                }
                if (parts == null || parts.length != 4) {
                    throw new IllegalArgumentException("Invalid location format in point " + pointId + ". Expected: world,x,y,z");
                }
                World world = Bukkit.getWorld(parts[0]);
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                org.bukkit.Location location = new org.bukkit.Location(world, x, y, z);
                ConfigurationSection beamSection = targetSection.getConfigurationSection("beam");
                Beam beam = null;
                if (beamSection != null) {
                    float length = (float) beamSection.getDouble("length", 255);
                    float width = (float) beamSection.getDouble("width", 1);
                    float offset = (float) beamSection.getDouble("offset", 0);
                    float spinSpeed = (float) beamSection.getDouble("spinSpeed", 1);
                    BlockData blockData = Bukkit.createBlockData(beamSection.getString("block", "red_stained_glass"));
                    beam = new Beam(length, width, offset, spinSpeed, blockData);
                }
                target = (Target) createIcon(targetSection, Point.Part.TARGET, pointId);
                target.setLocation(location);
                target.setBeam(beam);
            }

            // 加载 mark
            ConfigurationSection markSection = pointSection.getConfigurationSection("mark");
            Mark mark = null;
            if (markSection != null) {
                double distance = markSection.getDouble("distance", 5.0);
                mark = (Mark) createIcon(markSection, Point.Part.MARK, pointId);
                mark.setDistance(distance);
            }

            // 加载点的其他属性
            double triggerDistance = pointSection.getDouble("trigger-distance", 4.0);
            int fadeSpeed = pointSection.getInt("fade-speed", 10);

            // 加载 actions
            List<String> actions = pointSection.getStringList("actions");

            // 加载 stop_triggers
            List<String> stop_triggers = pointSection.getStringList("stop-triggers");

            boolean showInList = pointSection.getBoolean("show-in-list", true);

            Component display = mm.deserialize(I18n.replaceLegacyColorCodes(pointSection.getString("display", pointId)));

            // 创建并存储点
            Point point = new Point(pointId, display, target, mark, triggerDistance, fadeSpeed, actions, stop_triggers, showInList);
            points.put(pointId, point);
        }
    }

    public Icon createIcon(ConfigurationSection section, Point.Part part, String pointId) {
        List<String> display = section.getStringList("display");
        float scale = (float) section.getDouble("scale");
        Icon.Type type = Icon.Type.valueOf(section.getString("type", "TEXT").toUpperCase());
        String blockDataString = section.getString("block", null);
        BlockData blockData;
        try {
            blockData = blockDataString == null ? null : Bukkit.createBlockData(blockDataString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid block data format in point " + pointId + ": " + blockDataString, e);
        }
        String itemStackString = section.getString("item", null);
        ItemStack itemStack;
        try {
            itemStack = itemStackString == null ? null : Bukkit.getItemFactory().createItemStack(itemStackString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid item stack format in point " + pointId + ": " + itemStackString, e);
        }
        return switch (part) {
            case MARK -> new Mark(-1, display, scale, type, blockData, itemStack);
            case TARGET -> new Target(null, null, display, scale, type, blockData, itemStack);
        };
    }

    /**
     * 获取所有点。
     *
     * @return 点的集合。
     */
    public Collection<Point> getAllPoints() {
        return points.values();
    }

    public List<Point> getAllActivePoints(Player player) {
        List<Point> activePoints = new ArrayList<>();

        // 遍历所有追踪任务，找到属于指定玩家的点
        for (TrackTask task : activeTracks.values()) {
            if (task.getPlayer().equals(player)) {
                activePoints.add(task.getPoint());
            }
        }

        return activePoints;
    }

    public boolean isActive(Player player, Point point) {
        for (TrackTask task : activeTracks.values()) {
            if (task.getPlayer().equals(player) && task.getPoint().equals(point)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据 ID 获取点。
     *
     * @param id 点的 ID。
     * @return 对应的点对象。
     */
    public Point getPointById(String id) {
        for(TrackTask task : activeTracks.values()) {
            if (task.getPoint().getId().equals(id)) {
                return task.getPoint();
            }
        }
        return points.get(id);
    }

    /**
     * 移除所有点
     */
    public void removeAllPoints() {
        points.clear();
        savedPoints.clear();
    }

    /**
     * 移除点。
     *
     * @param id 点的 ID。
     */
    public void removePoint(String id) {
        points.remove(id);
    }

    /**
     * 添加或更新点。
     *
     * @param point 要添加或更新的点。
     */
    public void addOrUpdatePoint(Point point) {
        points.put(point.getId(), point);
    }

    public void savePlayerToPoint(Player player, Point point) {
        List<Point> points = savedPoints.get(player.getUniqueId()) == null ? new ArrayList<>() : savedPoints.get(player.getUniqueId());
        points.add(point);
        savedPoints.put(player.getUniqueId(), points);
    }

    public List<Point> getPlayerSavedPoints(Player player) {
        List<Point> points = new ArrayList<>();
        if (savedPoints.containsKey(player.getUniqueId())) {
            points = savedPoints.get(player.getUniqueId());
        }
        return points;
    }

    public void removePlayerFromPoint(Player player, Point point) {
        List<Point> points = savedPoints.get(player.getUniqueId());
        points.remove(point);
        savedPoints.put(player.getUniqueId(), points);
    }

    public void stopAllPoints() {
        for (TrackTask task : activeTracks.values()) {
            task.cancel();
            task.removeEntities();
        }
        activeTracks.clear();
    }

    public boolean stopAllPointsForPlayer(Player player, boolean trigger) {
        // 创建一个列表用于存储需要停止的任务 ID
        List<String> pointsToStop = new ArrayList<>();

        // 遍历所有追踪任务，找到属于该玩家的任务
        for (Map.Entry<String, TrackTask> entry : activeTracks.entrySet()) {
            TrackTask task = entry.getValue();
            if (task.getPlayer().equals(player)) {
                pointsToStop.add(entry.getKey());
            }
        }
        if (pointsToStop.isEmpty()) {
            return false;
        }
        // 停止所有找到的任务
        for (String trackId : pointsToStop) {
            TrackTask task = activeTracks.get(trackId);
            if (task != null) {
                stopTrack(task.getPlayer(), task.getPoint(), trigger);
                return true;
            }
        }
        return false;
    }

    public void stopAllPlayersForPoint(Point point, boolean trigger) {
        // 创建一个列表用于存储需要停止的任务 ID
        List<String> pointsToStop = new ArrayList<>();

        // 遍历所有追踪任务，找到与指定点相关的任务
        for (Map.Entry<String, TrackTask> entry : activeTracks.entrySet()) {
            TrackTask task = entry.getValue();
            if (task.getPoint().equals(point)) {
                pointsToStop.add(entry.getKey());
            }
        }

        // 停止所有找到的任务
        for (String trackId : pointsToStop) {
            TrackTask task = activeTracks.get(trackId);
            if (task != null) {
                stopTrack(task.getPlayer(), task.getPoint(), trigger);
            }
        }
    }

    /**
     * 执行指定的动作。
     *
     * @param point  点。
     * @param player 玩家。
     */
    private void executeActions(Point point, Player player) {
        List<String> actions = point.getActions();
        if(actions == null || actions.isEmpty()){
            return;
        }
        for (String action : actions) {
            action = ParseUtil.parsePlaceholders(action, point, player);
            if (action.startsWith("command:")) {
                player.performCommand(action.substring(8));
            } else if (action.startsWith("op:")) {
                boolean isOp = player.isOp();
                try {
                    player.setOp(true);
                    player.performCommand(action.substring(3));
                } finally {
                    player.setOp(isOp);
                }
            } else if (action.startsWith("console:")) {
                player.getServer().dispatchCommand(player.getServer().getConsoleSender(), action.substring(8));
            } else if (action.startsWith("message:")) {
                String message = action.substring(9);
                message = replaceLegacyColorCodes(message);
                player.sendMessage(mm.deserialize(message));
            } else if (action.startsWith("title:")) {
                String[] parts = action.substring(7).split(";", 5);
                String title = parts.length > 0 ? parts[0] : "";
                String subtitle = parts.length > 1 ? parts[1] : "";
                title = replaceLegacyColorCodes(title);
                subtitle = replaceLegacyColorCodes(subtitle);
                int fadeIn = parts.length > 2 ? Integer.parseInt(parts[2]) : 10;
                int stay = parts.length > 3 ? Integer.parseInt(parts[3]) : 70;
                int fadeOut = parts.length > 4 ? Integer.parseInt(parts[4]) : 20;
                Title title1 = Title.title(mm.deserialize(title), mm.deserialize(subtitle), Title.Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut)));
                player.showTitle(title1);
            } else if (action.startsWith("actionbar:")) {
                String message = action.substring(11);
                message = replaceLegacyColorCodes(message);
                player.sendActionBar(mm.deserialize(message));
            } else if (action.startsWith("sound:")) {
                String[] parts = action.substring(7).split(";", 3);
                String sound = parts[0].replaceAll("_", ".").toLowerCase(Locale.ROOT);
                float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
                player.playSound(player.getLocation(), sound, volume, pitch);
            } else if (action.startsWith("lightning:")) {
                World world = player.getWorld();
                String[] parts = action.substring(11).split(";", 4);
                if (parts.length == 1) {
                    if (parts[0].equalsIgnoreCase("target")) {
                        world.strikeLightningEffect(point.getTarget().getLocation());
                    } else if (parts[0].equalsIgnoreCase("player")) {
                        world.strikeLightningEffect(player.getLocation());
                    }
                } else {
                    String worldName = parts[0];
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    player.getWorld().strikeLightning(new org.bukkit.Location(Bukkit.getWorld(worldName), x, y, z));
                }
            } else if (action.startsWith("firework:")) {
                FireworkHandler.spawnFirework(action.substring(10), player, point.getTarget().getLocation());
            } else if (action.startsWith("start:")) {
                String nextPointId = action.substring(7);
                Point nextPoint = getPointById(nextPointId);
                if (nextPoint != null) {
                    if (nextPoint == point) {
                        ComponentLogger logger = ComponentLogger.logger();
                        logger.warn(I18n.getMessage("start-same-point", Map.of("point", point.getId())));
                        return;
                    }
                    startTrack(nextPoint, player);
                }
            }
        }
    }
}

