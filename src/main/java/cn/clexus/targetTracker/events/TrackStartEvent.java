package cn.clexus.targetTracker.events;

import cn.clexus.targetTracker.points.Point;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TrackStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Player player;
    private Point point;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public Point getPoint() {
        return point;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public TrackStartEvent(Player player, Point point) {
        this.player = player;
        this.point = point;
    }
}
