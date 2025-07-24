package cn.clexus.targetTracker.points;

import org.bukkit.block.data.BlockData;

public record Beam(float length, float width, float offset, float spinSpeed, BlockData blockData){
}