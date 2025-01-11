package cn.clexus.targetTracker.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class I18n {
    private static FileConfiguration config;

    /**
     * 初始化 MessageManager。应在插件启动时调用一次。
     *
     * @param fileConfig 插件的主配置文件
     */
    public static void initialize(FileConfiguration fileConfig) {
        config = fileConfig;
    }

    /**
     * 获取消息并格式化颜色代码。
     *
     * @param receiver 接收消息方
     * @param key 消息的键（相对于 message 部分，例如 "welcome" 表示 "message.welcome"）
     * @param placeholders 占位符替换键值对
     */
    public static void sendMessage(CommandSender receiver, String key, Map<String, String> placeholders) {
        if (config == null) {
            throw new IllegalStateException("I18n is not initialized!");
        }

        String message = config.getString("message." + key, "Message not found: " + key);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    public static String getMessage(String key, Map<String, String> placeholders) {
        if (config == null) {
            throw new IllegalStateException("I18n is not initialized!");
        }

        String message = config.getString("message." + key, "Message not found: " + key);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
