package cn.clexus.targetTracker.listeners;

import cn.clexus.targetTracker.managers.PointsManager;
import cn.clexus.targetTracker.points.Point;
import cn.clexus.targetTracker.utils.PacketUtils;
import com.github.retrooper.packetevents.protocol.world.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;


public class EventsListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PointsManager.getInstance().getAllActivePoints(player).forEach(point -> {
            if(!point.getStopTriggers().contains("quit")&&PointsManager.getInstance().isActive(player,point)) {
                PointsManager.getInstance().savePlayerToPoint(player, point);
                PointsManager.getInstance().stopTrack(player,point,false);
            }
        });
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        List<Point> points = new ArrayList<>(PointsManager.getInstance().getPlayerSavedPoints(player));
        for(Point p : points) {
            PointsManager.getInstance().startTrack(p, player);
            PointsManager.getInstance().removePlayerFromPoint(player, p);
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        stopTrack(event.getPlayer(),"death");
    }
    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        PointsManager.getInstance().getAllActivePoints(player).forEach(point -> {
            if(!point.getStopTriggers().contains("world_change")) {
                if(point.getTarget().getLocation().getWorld()==event.getPlayer().getWorld()) {
                    int mark = PointsManager.getInstance().getMarkEntityId(point,player);
                    int target = PointsManager.getInstance().getTargetEntityId(point,player);
                    if(mark!=-1&&target!=-1) {
                        PacketUtils.sendSpawnPacket(player,point.getMark(),mark,new Location(player.getX(), player.getY(), player.getZ(), 0,0));
                        PacketUtils.sendSpawnPacket(player,point.getTarget(),target,new Location(point.getTarget().getLocation().x(),point.getTarget().getLocation().y(),point.getTarget().getLocation().z(),0,0));
                    }
                }
            }else{
                PointsManager.getInstance().stopTrack(player,point,false);
            }
        });
    }
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        stopTrack(event.getPlayer(),"teleport");
    }
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if(event.getDamageSource().getDirectEntity() instanceof Player) {
            stopTrack((Player)event.getDamageSource().getDirectEntity(),"attack");
        } else if (event.getDamageSource().getDirectEntity()!=null&&event.getDamageSource().getCausingEntity() instanceof Player) {
            stopTrack((Player) event.getDamageSource().getCausingEntity(),"attack");
        }
    }
    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player) {
            stopTrack((Player)event.getEntity(),"damaged");
        }
    }

    private void stopTrack(Player player, String reason) {
        PointsManager.getInstance().getAllActivePoints(player).forEach(point -> {
            if(point.getStopTriggers().contains(reason)) {
                PointsManager.getInstance().stopTrack(player, point, false);
            }
        });
    }
}
