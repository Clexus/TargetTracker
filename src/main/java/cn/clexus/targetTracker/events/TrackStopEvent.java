package cn.clexus.targetTracker.events;

import cn.clexus.targetTracker.points.Point;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TrackStopEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    private boolean cancelled;
    private Player player;
    private Point point;
    private boolean trigger;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
    public TrackStopEvent(Player player, Point point, boolean trigger) {
        this.player = player;
        this.point = point;
        this.trigger = trigger;
    }

    public Player getPlayer() {
        return player;
    }

    public Point getPoint() {
        return point;
    }

    public boolean isTrigger() {
        return trigger;
    }
}
