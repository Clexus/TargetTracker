package cn.clexus.targetTracker.points;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class Target extends Icon{
    private Location location; // 目标点的坐标
    @Nullable
    private Beam beam;

    public Target(Location location, List<String> display, float scale, Type type, BlockData blockData, ItemStack itemStack){
        this(location, null, display, scale, type, blockData, itemStack);
    }

    public Target(Location location, @org.jetbrains.annotations.Nullable Beam beam, List<String> display, float scale, Type type, BlockData blockData, ItemStack itemStack) {
        super(scale, display, type, blockData, itemStack);
        this.location = location;
        this.beam = beam;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Beam getBeam() {
        return beam;
    }

    public void setBeam(Beam beam) {
        this.beam = beam;
    }
}
