package cn.clexus.targetTracker.points;

import java.util.List;

public class Point {
    private String id; // 点的唯一标识
    private Target target; // 目标点相关信息
    private Mark mark; // 标记点相关信息
    private double triggerDistance; // 触发距离
    private int fadeSpeed; // 透明度淡出速度
    private List<String> actions; // 触发后执行的动作

    public Point(String id, Target target, Mark mark, double triggerDistance, int fadeSpeed, List<String> actions) {
        this.id = id;
        this.target = target;
        this.mark = mark;
        this.triggerDistance = triggerDistance;
        this.fadeSpeed = fadeSpeed;
        this.actions = actions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Mark getMark() {
        return mark;
    }

    public void setMark(Mark mark) {
        this.mark = mark;
    }

    public double getTriggerDistance() {
        return triggerDistance;
    }

    public void setTriggerDistance(double triggerDistance) {
        this.triggerDistance = triggerDistance;
    }

    public int getFadeSpeed() {
        return fadeSpeed;
    }

    public void setFadeSpeed(int fadeSpeed) {
        this.fadeSpeed = fadeSpeed;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }
}
