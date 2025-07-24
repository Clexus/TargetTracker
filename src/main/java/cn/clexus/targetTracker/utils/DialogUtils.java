package cn.clexus.targetTracker.utils;

import cn.clexus.targetTracker.TargetTracker;
import cn.clexus.targetTracker.commands.PlayerCommand;
import cn.clexus.targetTracker.managers.PointsManager;
import cn.clexus.targetTracker.points.*;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static cn.clexus.targetTracker.utils.I18n.mm;

public class DialogUtils implements Listener {
    
    private static final ActionButton cancelButton = ActionButton.builder(I18n.getMessage("dialog-cancel")).action(DialogAction.customClick(Key.key("tracker:open/main"),null)).build();

    public enum DialogPart {
        MAIN, START, STOP, LIST
    }

    private static Point defaultPoint = null;

    public static void init(ConfigurationSection pointSection) {
        PointsManager pointsManager = PointsManager.getInstance();
        ConfigurationSection targetSection = pointSection.getConfigurationSection("target");
        String pointId = "temp";
        Target target = null;
        if (targetSection != null) {
            ConfigurationSection beamSection = targetSection.getConfigurationSection("beam");
            Beam beam = null;
            if (beamSection != null) {
                float length = (float) beamSection.getDouble("length", 255);
                float width = (float) beamSection.getDouble("width", 1);
                float offset = (float) beamSection.getDouble("offset", 0);
                float spinSpeed = (float) beamSection.getDouble("spinSpeed", 1);
                BlockData blockData = Bukkit.createBlockData(beamSection.getString("block", "red_stained_glass"));
                beam = new Beam(length, width, offset, spinSpeed, blockData);
            }
            target = (Target) pointsManager.createIcon(targetSection, Point.Part.TARGET, pointId);
            target.setBeam(beam);
        }

        ConfigurationSection markSection = pointSection.getConfigurationSection("mark");
        Mark mark = null;
        if (markSection != null) {
            double distance = markSection.getDouble("distance", 5.0);
            mark = (Mark) pointsManager.createIcon(markSection, Point.Part.MARK, pointId);
            mark.setDistance(distance);
        }

        double triggerDistance = pointSection.getDouble("trigger-distance", 4.0);
        int fadeSpeed = pointSection.getInt("fade-speed", 10);

        List<String> stop_triggers = pointSection.getStringList("stop-triggers");

        boolean showInList = pointSection.getBoolean("show-in-list", true);

        Component display = mm.deserialize(I18n.replaceLegacyColorCodes(pointSection.getString("display", pointId)));

        // 创建并存储点
        defaultPoint = new Point(pointId, display, target, mark, triggerDistance, fadeSpeed, new ArrayList<>(), stop_triggers, showInList);
    }

    private static List<PlainMessageDialogBody> getTrackingPoints(Player player) {
        return PointsManager.getInstance().getAllActivePoints(player).stream().filter(Point::isShowInList).map(point -> {
            Location location = point.getTarget().getLocation();
            return DialogBody.plainMessage(point.getDisplay()
                    .hoverEvent(HoverEvent.showText(Component.empty()
                            .append(Component.text(location.blockX() + ", " + location.blockY() + ", " + location.blockZ()).color(NamedTextColor.YELLOW))
                            .appendNewline()
                            .append(point.getCreator() == null ? I18n.getMessage("dialog-created-by-server") : I18n.getMessage("dialog-created-by", Map.of("player", point.getCreator().getName())))
                    ))
            );
        }).toList();
    }

    private static List<SingleOptionDialogInput.OptionEntry> getOptions(Player player) {
        List<SingleOptionDialogInput.OptionEntry> options = new ArrayList<>();
        if (PlayerCommand.hasPermission(player, "text-display")) {
            options.add(SingleOptionDialogInput.OptionEntry.create("text", I18n.getMessage("dialog-display-text"), true));
        }
        if (PlayerCommand.hasPermission(player, "block-display")) {
            options.add(SingleOptionDialogInput.OptionEntry.create("block", I18n.getMessage("dialog-display-block"), false));
        }
        if (PlayerCommand.hasPermission(player, "item-display")) {
            options.add(SingleOptionDialogInput.OptionEntry.create("item", I18n.getMessage("dialog-display-item"), false));
        }
        return options;
    }

    private static List<DialogInput> getAvailableInputs(Player player, DialogPart part) {
        List<DialogInput> inputs = new ArrayList<>();
        switch (part) {
            case START -> {
                inputs.add(DialogInput.text("location", I18n.getMessage("dialog-location")).maxLength(60).build());
                if (!getOptions(player).isEmpty()) {
                    inputs.add(DialogInput.singleOption("display_types", I18n.getMessage("dialog-display-types"), getOptions(player)).build());
                }
                if (PlayerCommand.hasPermission(player, "custom-text")) {
                    int lines = TargetTracker.getInstance().getConfig().getInt("player-points.max-lines", 2);
                    if (lines < 0) {
                        throw new IllegalArgumentException("lines must be a non-negative integer");
                    }
                    inputs.add(DialogInput.text("custom_display", I18n.getMessage("dialog-custom-display")).build());
                    inputs.add(DialogInput.text("custom_text", I18n.getMessage("dialog-custom-text")).maxLength(60).multiline(TextDialogInput.MultilineOptions.create(lines == 0 ? null : lines, null)).build());
                }
                if (PlayerCommand.hasPermission(player, "custom-block")) {
                    inputs.add(DialogInput.text("custom_block", I18n.getMessage("dialog-custom-block")).build());
                }
                if (PlayerCommand.hasPermission(player, "custom-item")) {
                    inputs.add(DialogInput.text("custom_item", I18n.getMessage("dialog-custom-item")).build());
                }
                if (PlayerCommand.hasPermission(player, "beam")) {
                    inputs.add(DialogInput.text("beam", I18n.getMessage("dialog-custom-beam")).build());
                }
                if (PlayerCommand.hasPermission(player, "share")) {
                    inputs.add(DialogInput.text("share", I18n.getMessage("dialog-share")).maxLength(60).build());
                }
            }
        }
        return inputs;
    }

    private static List<ActionButton> getAvailableButtons(Player player, DialogPart part) {
        List<ActionButton> buttons = new ArrayList<>();
        switch (part) {
            case MAIN -> {
                if (PlayerCommand.hasPermission(player, "start")) {
                    buttons.add(ActionButton.builder(I18n.getMessage("dialog-start"))
                            .action(DialogAction.customClick(Key.key("tracker:open/start"), null))
                            .build());
                }
                if (PlayerCommand.hasPermission(player, "stop")) {
                    buttons.add(ActionButton.builder(I18n.getMessage("dialog-stop"))
                            .action(DialogAction.customClick(Key.key("tracker:open/stop"), null))
                            .build());
                }
                if (PlayerCommand.hasPermission(player, "list")) {
                    buttons.add(ActionButton.builder(I18n.getMessage("dialog-list"))
                            .action(DialogAction.customClick(Key.key("tracker:open/list"), null))
                            .build());
                }
                return buttons;
            }
            case STOP -> {
                PointsManager.getInstance().getAllActivePoints(player).stream().filter(Point::isShowInList).forEach(point -> {
                    Location location = point.getTarget().getLocation();
                    buttons.add(ActionButton.builder(point.getDisplay())
                            .tooltip(Component.empty()
                                    .append(Component.text(location.blockX() + ", " + location.blockY() + ", " + location.blockZ()).color(NamedTextColor.YELLOW))
                                    .appendNewline()
                                    .append(point.getCreator() == null ? I18n.getMessage("dialog-created-by-server") : I18n.getMessage("dialog-created-by", Map.of("player", point.getCreator().getName())))
                            )
                            .action(DialogAction.customClick(Key.key("tracker:stop/" + point.getId()), null))
                            .build()
                    );
                });
            }
        }
        return buttons;
    }

    @NotNull
    public static Dialog getPlayerDialog(Player player, DialogPart part) {
        
        switch (part) {
            case MAIN -> {
                return Dialog.create(builder -> builder.empty()
                        .base(
                                DialogBase.builder(I18n.getMessage("dialog-title"))
                                        .build())
                        .type(
                                DialogType.multiAction(
                                        getAvailableButtons(player, part),
                                        ActionButton.builder(I18n.getMessage("dialog-cancel")).build(), 1
                                )
                        ));
            }
            case START -> {
                return Dialog.create(builder -> builder.empty()
                        .base(
                                DialogBase.builder(I18n.getMessage("dialog-start-title"))
                                        .inputs(getAvailableInputs(player, DialogPart.START))
                                        .build())
                        .type(
                                DialogType.confirmation(
                                        ActionButton.builder(I18n.getMessage("dialog-confirm")).action(DialogAction.customClick(Key.key("tracker:start/confirm"), null)).build(),
                                        ActionButton.builder(I18n.getMessage("dialog-cancel")).action(DialogAction.customClick(Key.key("tracker:open/main"), null)).build()
                                )
                        ));
            }
            case LIST -> {
                if (PointsManager.getInstance().getAllActivePoints(player).isEmpty()) {
                    return Dialog.create(builder -> builder.empty()
                            .base(
                                    DialogBase.builder(I18n.getMessage("dialog-list-title"))
                                            .body(List.of(DialogBody.plainMessage(I18n.getMessage("dialog-no-points"))))
                                            .build())
                            .type(
                                    DialogType.notice(cancelButton)
                            )
                    );
                }
                return Dialog.create(builder -> builder.empty()
                        .base(
                                DialogBase.builder(I18n.getMessage("dialog-list-title"))
                                        .body(getTrackingPoints(player))
                                        .build())
                        .type(
                                DialogType.notice(cancelButton)));
            }
            case STOP -> {
                if (PointsManager.getInstance().getAllActivePoints(player).isEmpty()) {
                    return Dialog.create(builder -> builder.empty()
                                .base(
                                        DialogBase.builder(I18n.getMessage("dialog-stop-title"))
                                                .body(List.of(DialogBody.plainMessage(I18n.getMessage("dialog-no-points"))))
                                                .build())
                                .type(
                                        DialogType.notice(cancelButton)
                                )
                       );
                }
                return Dialog.create(builder -> builder.empty()
                        .base(
                                DialogBase.builder(I18n.getMessage("dialog-stop-title"))
                                        .build())
                        .type(
                                DialogType.multiAction(getAvailableButtons(player, part),
                                        cancelButton, 3)
                        )
                );
            }
        }
        return Dialog.create(builder -> builder.empty().base(DialogBase.builder(Component.text("null")).build()));
    }

    @EventHandler
    public void onCustomClick(PlayerCustomClickEvent event) {
        Key key = event.getIdentifier();
        if (!key.namespace().equals("tracker")) {
            return;
        }
        if (event.getCommonConnection() instanceof PlayerGameConnection connection) {
            Player player = connection.getPlayer();
            if (key.value().startsWith("stop/")) {
                String id = key.value().substring(5);
                PointsManager.getInstance().stopTrack(player, PointsManager.getInstance().getPointById(id), false);
                return;
            }
            Point point = new Point(defaultPoint);
            switch (key.value()) {
                case "open/main" -> player.showDialog(getPlayerDialog(player, DialogPart.MAIN));
                case "open/start" -> player.showDialog(getPlayerDialog(player, DialogPart.START));
                case "open/stop" -> player.showDialog(getPlayerDialog(player, DialogPart.STOP));
                case "open/list" -> player.showDialog(getPlayerDialog(player, DialogPart.LIST));
                case "start/confirm" -> {
                    DialogResponseView view = event.getDialogResponseView();
                    if (view == null) break;
                    if (view.getText("location") != null) {
                        String location = view.getText("location");
                        if (location.isEmpty()) {
                            player.sendMessage(I18n.getMessage("no-location"));
                            return;
                        }
                        String[] parts = location.split(",");
                        if (parts.length != 3) {
                            player.sendMessage(I18n.getMessage("wrong-number"));
                            return;
                        }
                        Integer[] coordinates = new Integer[3];
                        for (int i = 0; i < parts.length; i++) {
                            try {
                                coordinates[i] = Integer.parseInt(parts[i]);
                            } catch (NumberFormatException e) {
                                player.sendMessage(I18n.getMessage("invalid-number"));
                                return;
                            }
                        }
                        Location loc = new Location(player.getWorld(), coordinates[0], coordinates[1], coordinates[2]);
                        if (PointsManager.getInstance().getAllActivePoints(player).stream().anyMatch(point1 -> point1.getTarget().getLocation().distance(loc) < 10)) {
                            player.sendMessage(I18n.getMessage("too-close"));
                            return;
                        }
                        point.setId(UUID.randomUUID().toString());
                        point.setCreator(player);
                        point.getTarget().setLocation(loc);
                    }
                    String display = view.getText("custom_display");
                    ;
                    String text = view.getText("custom_text");
                    if (display != null && !display.isEmpty()) {
                        point.setDisplay(Component.text(display));
                    }
                    if (text != null && !text.isEmpty()) {
                        point.getMark().getDisplay().addAll(List.of(text.split("\n")));
                    }
                    switch (view.getText("display_types")) {
                        case "block": {
                            String block = view.getText("custom_block");
                            if (block == null || block.isEmpty()) break;
                            Material material = Material.matchMaterial(block);
                            if (material == null || !material.isBlock()) {
                                player.sendMessage(I18n.getMessage("wrong-material", Map.of("material", block)));
                                return;
                            }
                            BlockData blockData = Bukkit.createBlockData(material);
                            point.getMark().setBlock(blockData);
                            point.getMark().setType(Icon.Type.BLOCK);
                        }
                        break;
                        case "item": {
                            String item = view.getText("custom_item");
                            if (item == null || item.isEmpty()) break;
                            Material material = Material.matchMaterial(item);
                            if (material == null || !material.isItem()) {
                                player.sendMessage(I18n.getMessage("wrong-material", Map.of("material", item)));
                                return;
                            }
                            point.getMark().setItem(ItemStack.of(material));
                            point.getMark().setType(Icon.Type.ITEM);
                        }
                        break;
                        case null:
                        case "text":
                        default: {
                        }
                    }
                    if (view.getText("beam") != null && !view.getText("beam").isEmpty()) {
                        Beam beam = point.getTarget().getBeam();
                        String beamText = view.getText("beam");
                        Material material = Material.matchMaterial(beamText);
                        if (material == null || !material.isItem()) {
                            player.sendMessage(I18n.getMessage("wrong-material", Map.of("material", beamText)));
                            return;
                        }
                        BlockData blockData = Bukkit.createBlockData(material);
                        Beam newBeam = new Beam(
                                beam.length(),
                                beam.width(),
                                beam.offset(),
                                beam.spinSpeed(),
                                blockData
                        );
                        point.getTarget().setBeam(newBeam);
                    }
                    if (view.getText("share") != null && !view.getText("share").isEmpty()) {
                        String share = view.getText("share");
                        List<Player> players = Arrays.stream(share.split(",")).map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
                        PointsManager.getInstance().sendShareMessage(point, players);
                    }
                    PointsManager.getInstance().startTrack(point, player);
                }
            }
        }
    }


}
