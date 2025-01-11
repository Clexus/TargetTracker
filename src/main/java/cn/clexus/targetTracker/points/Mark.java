package cn.clexus.targetTracker.points;

import java.util.List;

public class Mark {
    private double distance; // 标记点距离玩家的距离
    private float scale;
    private List<String> display; // 显示内容

    public Mark(double distance, List<String> display, float scale) {
        this.distance = distance;
        this.display = display;
        this.scale = scale;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
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
