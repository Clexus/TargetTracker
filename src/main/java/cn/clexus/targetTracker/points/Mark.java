package cn.clexus.targetTracker.points;

import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Mark extends Icon{
    private double distance; // 标记点距离玩家的距离

    public Mark(double distance, List<String> display, float scale, Type type, BlockData blockData, ItemStack itemStack) {
        super(scale,display,type,blockData,itemStack);
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

}
