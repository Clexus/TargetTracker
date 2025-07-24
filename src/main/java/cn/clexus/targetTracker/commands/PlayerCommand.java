package cn.clexus.targetTracker.commands;

import cn.clexus.targetTracker.TargetTracker;
import cn.clexus.targetTracker.utils.DialogUtils;
import cn.clexus.targetTracker.utils.I18n;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class PlayerCommand {
    public static LiteralCommandNode<CommandSourceStack> playerCommandNode = Commands.literal("track")
                .requires(stack -> hasPermission(stack.getSender(), "open"))
                .executes(stack -> {
                    if(!(stack.getSource().getSender() instanceof Player player)){
                        stack.getSource().getSender().sendMessage(I18n.getMessage("not-player"));
                    }else{
                        player.showDialog(DialogUtils.getPlayerDialog(player, DialogUtils.DialogPart.MAIN));
                    }
                    return 1;
                })
                .build();


    public static boolean hasPermission(Permissible permissible, String section){
        ConfigurationSection points = TargetTracker.getInstance().getConfig().getConfigurationSection("player-points");
        if(points != null){
            ConfigurationSection permissions = points.getConfigurationSection("permissions");
            if(permissions != null){
                String permission = permissions.getString(section, "");
                return permission.isEmpty() || permissible.hasPermission(permission);
            }
        }
        return false;
    }
}
