package cn.clexus.targetTracker.points;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Point {
    private Player creator;
    private String id; // 点的唯一标识
    private Target target; // 目标点相关信息
    private Component display;
    private Mark mark; // 标记点相关信息
    private double triggerDistance; // 触发距离
    private int fadeSpeed; // 透明度淡出速度
    private List<String> actions; // 触发后执行的动作
    private List<String> stopTriggers;
    private boolean showInList; // 是否在列表中显示

    public enum Part{
        MARK, TARGET
    }

    public Point(Point point){
        this.creator = point.creator;
        this.id = point.id;
        this.display = point.display;
        Beam oldBeam = point.target.getBeam();
        Beam newBeam = null;
        if(oldBeam != null){
            newBeam = new Beam(
                    oldBeam.length(),
                    oldBeam.width(),
                    oldBeam.offset(),
                    oldBeam.spinSpeed(),
                    oldBeam.blockData() == null ? null : oldBeam.blockData().clone()
            );
        }
        Target oldTarget = point.target;
        this.target = new Target(
                oldTarget.getLocation() == null ? null : oldTarget.getLocation().clone(),
                newBeam,
                oldTarget.getDisplay() == null ? null : new ArrayList<>(oldTarget.getDisplay()),
                oldTarget.getScale(),
                oldTarget.getType(),
                oldTarget.getBlock() == null ? null : oldTarget.getBlock().clone(),
                oldTarget.getItem() == null ? null : oldTarget.getItem().clone()
        );
        Mark oldMark = point.mark;
        this.mark = new Mark(
                oldMark.getDistance(),
                oldMark.getDisplay() == null ? null : new ArrayList<>(oldMark.getDisplay()),
                oldMark.getScale(),
                oldMark.getType(),
                oldMark.getBlock() == null ? null : oldTarget.getBlock().clone(),
                oldMark.getItem() == null ? null : oldTarget.getItem().clone()
        );
        this.triggerDistance = point.triggerDistance;
        this.fadeSpeed = point.fadeSpeed;
        this.actions = point.actions == null ? null : new ArrayList<>(point.actions);
        this.stopTriggers = point.stopTriggers == null ? null : new ArrayList<>(point.stopTriggers);
        this.showInList = point.showInList;
    }

    public Point(String id, Component display, Target target, Mark mark, double triggerDistance, int fadeSpeed, List<String> actions, List<String> stopTriggers, boolean showInList) {
        this(null, id, display, target, mark, triggerDistance, fadeSpeed, actions, stopTriggers, showInList);
    }

    public Point(Player creator,String id, Component display, Target target, Mark mark, double triggerDistance, int fadeSpeed, List<String> actions, List<String> stopTriggers, boolean showInList) {
        this.creator = creator;
        this.id = id;
        this.target = target;
        this.display = display;
        this.mark = mark;
        this.triggerDistance = triggerDistance;
        this.fadeSpeed = fadeSpeed;
        this.actions = actions;
        this.stopTriggers = stopTriggers;
        this.showInList = showInList;
    }

    public Player getCreator() {
        return creator;
    }

    public void setCreator(Player creator) {
        this.creator = creator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Component getDisplay() {
        return display;
    }
    public void setDisplay(Component display) {
        this.display = display;
    }

    public boolean isShowInList() {
        return showInList;
    }

    public void setShowInList(boolean showInList) {
        this.showInList = showInList;
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

    public List<String> getStopTriggers() {
        return stopTriggers;
    }

    public void setStopTriggers(List<String> stopTriggers) {
        this.stopTriggers = stopTriggers;
    }
}
