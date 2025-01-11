package cn.clexus.targetTracker.points;

import org.bukkit.Location;

import java.util.List;

public class Target {
    private Location location; // 目标点的坐标
    private float scale;
    private List<String> display; // 显示内容

    public Target(Location location, List<String> display, float scale) {
        this.location = location;
        this.display = display;
        this.scale = scale;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<String> getDisplay() {
        return display;
    }

    public void setDisplay(List<String> display) {
        this.display = display;
    }
    public float getScale() {
        return scale;
    }
    public void setScale(float scale) {
        this.scale = scale;
    }
}
