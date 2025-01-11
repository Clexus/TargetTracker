package cn.clexus.targetTracker.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PacketUtils {
    public static void sendSpawnPacket(Player player, int entityId, Location location, float scale) {
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                entityId,
                UUID.randomUUID(),
                EntityTypes.TEXT_DISPLAY,
                new Location(
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        0,
                        0
                ),
                0f,
                0,
                null
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnPacket);
        WrapperPlayServerEntityMetadata metadataPacket;
        TextDisplayMeta textDisplayMeta = (TextDisplayMeta) EntityMeta.createMeta(entityId, EntityTypes.TEXT_DISPLAY);
        textDisplayMeta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        textDisplayMeta.setPositionRotationInterpolationDuration(2);
        textDisplayMeta.setBackgroundColor(0);
        textDisplayMeta.setSeeThrough(true);
        textDisplayMeta.setShadow(false);
        textDisplayMeta.setScale(new Vector3f(scale,scale,scale));
        metadataPacket = textDisplayMeta.createPacket();
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);
    }

    public static void sendTextChangePacket(Player player, int entityId, List<String> displayLines, int distance) {
        Component displayText = Component.text("");
        for (int i = 0; i < displayLines.size(); i++) {
            if (i > 0) {
                displayText = displayText.append(Component.text("\n"));
            }
            // 使用 MiniMessage 转换 & 格式
            String convertedLine = displayLines.get(i).replace('&', '§'); // 将 & 替换为 §
            convertedLine = convertedLine.replaceAll("%distance%", String.valueOf(distance));
            displayText = displayText.append(LegacyComponentSerializer.legacySection().deserialize(convertedLine));
        }
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
                entityId,
                List.of(new EntityData(23, EntityDataTypes.ADV_COMPONENT,displayText))
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);
    }
    public static void sendTeleportPacket(Player player, int entityId, org.bukkit.Location location) {
        WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                entityId,
                new Location(
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        0,
                        0
                ),
                false
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, teleportPacket);
    }

    public static void sendOpacityPacket(Player player, int entityId, byte opacity) {

        WrapperPlayServerEntityMetadata opacityPacket = new WrapperPlayServerEntityMetadata(
                entityId,List.of(new EntityData(26,EntityDataTypes.BYTE,opacity))
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, opacityPacket);
    }

    public static void sendDestroyPacket(Player player, int entityId) {
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyPacket);
    }
}
