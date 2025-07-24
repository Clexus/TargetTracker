package cn.clexus.targetTracker.points;

import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class Icon {
    public enum Type {
        TEXT, BLOCK, ITEM
    }

    private float scale;
    private Type type;
    private ItemStack item;
    private BlockData block;
    private List<String> display;

    public Icon(float scale, List<String> display) {
        this(scale, display, Type.TEXT);
    }

    public Icon(float scale, List<String> display, Type type) {
        this(scale, display, type, null, null);
    }

    public Icon(float scale, List<String> display, Type type, BlockData block) {
        this(scale, display, type, block, null);
    }

    public Icon(float scale, List<String> display, Type type, ItemStack item) {
        this(scale, display, type, null, item);
    }

    public Icon(float scale, List<String> display, Type type, BlockData block, ItemStack item) {
        this.scale = scale;
        this.display = display;
        this.type = type;
        this.block = block;
        this.item = item;
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

    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }
    public ItemStack getItem() {
        return item;
    }
    public void setItem(ItemStack item) {
        this.item = item;
    }

    public BlockData getBlock() {
        return block;
    }

    public void setBlock(BlockData block) {
        this.block = block;
    }
}
