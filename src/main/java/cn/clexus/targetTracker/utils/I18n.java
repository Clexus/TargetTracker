package cn.clexus.targetTracker.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class I18n {
    private static FileConfiguration config;
    public static final MiniMessage mm = MiniMessage.miniMessage();

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

        receiver.sendMessage(mm.deserialize(replaceLegacyColorCodes(message)));
    }

    public static void sendMessage(CommandSender receiver, String key) {
        sendMessage(receiver, key, null);
    }

    public static Component getMessage(String key){
        return getMessage(key, null);
    }

    public static Component getMessage(String key, Map<String, String> placeholders) {
        if (config == null) {
            throw new IllegalStateException("I18n is not initialized!");
        }

        String message = config.getString("message." + key, "Message not found: " + key);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return mm.deserialize(replaceLegacyColorCodes(message)).replaceText(TextReplacementConfig.builder().match("<nl>").replacement(Component.newline()).build());
    }

    public static String replaceLegacyColorCodes(String text) {
        String[][] formatMap = {
                {"0", "black"}, {"1", "dark_blue"}, {"2", "dark_green"}, {"3", "dark_aqua"},
                {"4", "dark_red"}, {"5", "dark_purple"}, {"6", "gold"}, {"7", "gray"},
                {"8", "dark_gray"}, {"9", "blue"}, {"a", "green"}, {"b", "aqua"},
                {"c", "red"}, {"d", "light_purple"}, {"e", "yellow"}, {"f", "white"},
                {"k", "obf"}, {"l", "b"}, {"m", "st"},
                {"n", "u"}, {"o", "i"}, {"r", "reset"}
        };

        Pattern pattern = Pattern.compile("[§&]x([§&][0-9a-fA-F]){6}");
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group().replaceAll("[§&]x|[§&]", "");
            matcher.appendReplacement(result, "<#" + hex + ">");
        }
        matcher.appendTail(result);
        text = result.toString();

        for (String[] entry : formatMap) {
            text = text.replaceAll("[§&]" + entry[0], "<" + entry[1] + ">");
        }

        return text;
    }

}
