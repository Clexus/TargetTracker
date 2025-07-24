package cn.clexus.targetTracker.utils;

import cn.clexus.targetTracker.commands.PlayerCommand;
import cn.clexus.targetTracker.points.Beam;
import cn.clexus.targetTracker.points.Icon;
import cn.clexus.targetTracker.points.Point;
import cn.clexus.targetTracker.support.PlaceholderAPISupport;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.BlockDisplayMeta;
import me.tofaa.entitylib.meta.display.ItemDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

import static cn.clexus.targetTracker.utils.I18n.mm;

public class PacketUtils {
    private final static PlayerManager manager = PacketEvents.getAPI().getPlayerManager();

    public static void sendBeamPacket(Player player, Beam beam, int beamEntityId, Location targetLocation) {
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                beamEntityId,
                UUID.randomUUID(),
                EntityTypes.BLOCK_DISPLAY,
                targetLocation,
                0f,
                0,
                null
        );
        manager.sendPacket(player, spawnPacket);
        BlockDisplayMeta meta = (BlockDisplayMeta) EntityMeta.createMeta(beamEntityId, EntityTypes.BLOCK_DISPLAY);
        meta.setBrightnessOverride((15 << 4) | (15 << 20));
        meta.setTransformationInterpolationDuration(2);
        meta.setPositionRotationInterpolationDuration(2);
        meta.setBlockId(SpigotConversionUtil.fromBukkitBlockData(beam.blockData()).getGlobalId());
        meta.setTranslation(new Vector3f(-0.5f * beam.width(), beam.offset(), -0.5f * beam.width()));
        meta.setScale(new Vector3f(beam.width(), beam.length(), beam.width()));
        meta.setViewRange(900);
        manager.sendPacket(player, meta.createPacket());
    }

    public static void spinBeam(Player player, int beamId, Beam beam, int opacity, org.bukkit.Location location, float yaw) {
        float width = (opacity - 25f) / 230 * beam.width();
        BlockDisplayMeta meta = (BlockDisplayMeta) EntityMeta.createMeta(beamId, EntityTypes.BLOCK_DISPLAY);
        meta.setScale(new Vector3f(width, beam.length(), width));
        meta.setTranslation(new Vector3f(-0.5f * width, beam.offset(), -0.5f * width));
        manager.sendPacket(player, meta.createPacket());
        WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                beamId,
                new Location(
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        yaw,
                        0
                ),
                false
        );
        manager.sendPacket(player, teleportPacket);
    }

    public static int sendSpawnPacket(Player player, Icon icon, int entityId, Location location) {
        float scale = icon.getScale();
        int belowId = 0;
        WrapperPlayServerEntityMetadata metadataPacket;
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                entityId,
                UUID.randomUUID(),
                EntityTypes.TEXT_DISPLAY,
                location,
                0f,
                0,
                null
        );
        manager.sendPacket(player, spawnPacket);
        TextDisplayMeta textDisplayMeta = (TextDisplayMeta) EntityMeta.createMeta(entityId, EntityTypes.TEXT_DISPLAY);
        textDisplayMeta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        textDisplayMeta.setPositionRotationInterpolationDuration(2);
        textDisplayMeta.setTransformationInterpolationDuration(2);
        textDisplayMeta.setBackgroundColor(0);
        textDisplayMeta.setSeeThrough(true);
        textDisplayMeta.setShadow(false);
        textDisplayMeta.setViewRange(900);
        textDisplayMeta.setScale(new Vector3f(scale, scale, scale));
        metadataPacket = textDisplayMeta.createPacket();
        manager.sendPacket(player, metadataPacket);
        switch (icon.getType()) {
            case BLOCK -> {
                belowId = Bukkit.getUnsafe().nextEntityId();
                spawnPacket = new WrapperPlayServerSpawnEntity(
                        belowId,
                        UUID.randomUUID(),
                        EntityTypes.BLOCK_DISPLAY,
                        location,
                        0f,
                        0,
                        null
                );
                manager.sendPacket(player, spawnPacket);

                BlockDisplayMeta meta = (BlockDisplayMeta) EntityMeta.createMeta(belowId, EntityTypes.BLOCK_DISPLAY);
                meta.setBlockId(SpigotConversionUtil.fromBukkitBlockData(icon.getBlock()).getGlobalId());
                meta.setScale(new Vector3f(scale, scale, scale));
                meta.setPositionRotationInterpolationDuration(2);
                meta.setTransformationInterpolationDuration(2);
                meta.setTranslation(new Vector3f(-0.5f * scale, -scale, -0.5f * scale));
                meta.setBrightnessOverride((15 << 4) | (15 << 20));
                metadataPacket = meta.createPacket();
                manager.sendPacket(player, metadataPacket);

                textDisplayMeta.setTranslation(new Vector3f(0, 1, 0));
                metadataPacket = textDisplayMeta.createPacket();
                manager.sendPacket(player, metadataPacket);

                WrapperPlayServerSetPassengers passengersPacket = new WrapperPlayServerSetPassengers(belowId, new int[]{entityId});
                manager.sendPacket(player, passengersPacket);
            }
            case ITEM -> {
                belowId = Bukkit.getUnsafe().nextEntityId();
                spawnPacket = new WrapperPlayServerSpawnEntity(
                        belowId,
                        UUID.randomUUID(),
                        EntityTypes.ITEM_DISPLAY,
                        location,
                        0f,
                        0,
                        null
                );
                manager.sendPacket(player, spawnPacket);

                ItemDisplayMeta meta = (ItemDisplayMeta) EntityMeta.createMeta(belowId, EntityTypes.ITEM_DISPLAY);
                meta.setItem(SpigotConversionUtil.fromBukkitItemStack(icon.getItem()));
                meta.setScale(new Vector3f(scale, scale, scale));
                meta.setTranslation(new Vector3f(0, -scale / 2, 0));
                meta.setPositionRotationInterpolationDuration(2);
                meta.setTransformationInterpolationDuration(2);
                metadataPacket = meta.createPacket();
                manager.sendPacket(player, metadataPacket);

                metadataPacket = textDisplayMeta.createPacket();
                manager.sendPacket(player, metadataPacket);

                WrapperPlayServerSetPassengers passengersPacket = new WrapperPlayServerSetPassengers(belowId, new int[]{entityId});
                manager.sendPacket(player, passengersPacket);
            }
            default -> {
            }
        }
        return belowId;
    }

    public static void updateProperties(Player player, Point point, int entityId, List<String> displayLines, int distance) {
        Component displayText = Component.empty();
        for (int i = 0; i < displayLines.size(); i++) {
            if (i > 0) {
                displayText = displayText.appendNewline();
            }
            String convertedLine = I18n.replaceLegacyColorCodes(displayLines.get(i));
            convertedLine = convertedLine.replaceAll("%distance%", String.valueOf(distance)).replaceAll("%player%", player.getName());
            if (point.getCreator() != null) {
                displayText = displayText.append(parse(point.getCreator(), convertedLine));
            } else {
                if (PlaceholderAPISupport.hasSupport()) {
                    convertedLine = PlaceholderAPI.setPlaceholders(player, convertedLine);
                }
                displayText = displayText.append(mm.deserialize(convertedLine));
            }
        }
        TextDisplayMeta meta = (TextDisplayMeta) EntityMeta.createMeta(entityId, EntityTypes.TEXT_DISPLAY);
        meta.setText(displayText);
        WrapperPlayServerEntityMetadata metadataPacket = meta.createPacket();
        manager.sendPacket(player, metadataPacket);
    }

    public static void sendTeleportPacket(Player player, int entityId, org.bukkit.Location location) {
        org.bukkit.Location to = player.getEyeLocation();

        Vector direction = to.toVector().subtract(location.toVector()).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        float pitch = (float) Math.toDegrees(-Math.asin(direction.getY()));
        WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                entityId,
                new Location(
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        yaw,
                        pitch
                ),
                false
        );
        manager.sendPacket(player, teleportPacket);
    }

    public static void updateScaleAndOpacity(Player player, int entityId, int belowId, byte opacity, float scale) {

        TextDisplayMeta meta = (TextDisplayMeta) EntityMeta.createMeta(entityId, EntityTypes.TEXT_DISPLAY);
        meta.setTextOpacity(opacity);
        WrapperPlayServerEntityMetadata opacityPacket = meta.createPacket();
        manager.sendPacket(player, opacityPacket);

        if (belowId != 0) {
            AbstractDisplayMeta displayMeta;
            try {
                displayMeta = (AbstractDisplayMeta) EntityMeta.createMeta(belowId, EntityTypes.BLOCK_DISPLAY);
            } catch (Exception e) {
                displayMeta = (AbstractDisplayMeta) EntityMeta.createMeta(belowId, EntityTypes.ITEM_DISPLAY);
            }
            displayMeta.setScale(new Vector3f(scale, scale, scale));
            displayMeta.setTranslation(new Vector3f(-0.5f * scale, 0, -0.5f * scale));
            manager.sendPacket(player, displayMeta.createPacket());
        }
    }

    public static void sendDestroyPacket(Player player, int entityId) {
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityId);
        manager.sendPacket(player, destroyPacket);
    }

    public static Component parse(Player player, String text) {
        MiniMessage miniMessage;
        MiniMessage.Builder builder = MiniMessage.builder();
        if (PlayerCommand.hasPermission(player, "text-color")) {
            builder.tags(TagResolver.builder().resolver(StandardTags.color()).build());
        }
        if (PlayerCommand.hasPermission(player, "text-format")) {
            builder.tags(TagResolver.builder().resolver(StandardTags.decorations()).build());
        }
        miniMessage = builder.build();
        return miniMessage.deserialize(text);
    }
}
