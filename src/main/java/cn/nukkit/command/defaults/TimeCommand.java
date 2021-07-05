package cn.nukkit.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.TranslationKey;
import cn.nukkit.level.Level;

/**
 * @author xtypr
 * @since 2015/11/11
 */
public class TimeCommand extends VanillaCommand {

    public TimeCommand(String name) {
        super(name, "%nukkit.command.time.description", "%nukkit.command.time.usage");
        this.setPermission("nukkit.command.time.add;" +
                "nukkit.command.time.set;" +
                "nukkit.command.time.start;" +
                "nukkit.command.time.stop");
        this.commandParameters.clear();
        this.commandParameters.put("1arg", new CommandParameter[]{
                CommandParameter.newEnum("mode", new CommandEnum("TimeMode", "query", "start", "stop"))
        });
        this.commandParameters.put("add", new CommandParameter[]{
                CommandParameter.newEnum("mode", new CommandEnum("TimeModeAdd", "add")),
                CommandParameter.newType("amount", CommandParamType.INT)
        });
        this.commandParameters.put("setAmount", new CommandParameter[]{
                CommandParameter.newEnum("mode", false, new CommandEnum("TimeModeSet", "set")),
                CommandParameter.newType("amount", CommandParamType.INT)
        });
        this.commandParameters.put("setTime", new CommandParameter[]{
                CommandParameter.newEnum("mode", new CommandEnum("TimeModeSet", "set")),
                CommandParameter.newEnum("time", new CommandEnum("TimeSpec", "day", "night", "midnight", "noon", "sunrise", "sunset"))
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length < 1) {
            sendUsage(sender);

            return false;
        }

        if ("start".equals(args[0])) {
            if (!sender.hasPermission("nukkit.command.time.start")) {
                sendNoPermissionMessage(sender);

                return true;
            }
            for (Level level : sender.getServer().getLevels().values()) {
                level.checkTime();
                level.startTime();
                level.checkTime();
            }
            Command.broadcastCommandMessage(sender, "Restarted the time");
            return true;
        } else if ("stop".equals(args[0])) {
            if (!sender.hasPermission("nukkit.command.time.stop")) {
                sendNoPermissionMessage(sender);

                return true;
            }
            for (Level level : sender.getServer().getLevels().values()) {
                level.checkTime();
                level.stopTime();
                level.checkTime();
            }
            Command.broadcastCommandMessage(sender, "Stopped the time");
            return true;
        } else if ("query".equals(args[0])) {
            if (!sender.hasPermission("nukkit.command.time.query")) {
                sendNoPermissionMessage(sender);

                return true;
            }
            Level level;
            if (sender instanceof Player) {
                level = ((Player) sender).getLevel();
            } else {
                level = sender.getServer().getDefaultLevel();
            }
            sender.sendMessage(TranslationKey.Commands.TIME_QUERY_GAMETIME.with(Integer.toString(level.getTime())));
            return true;
        }


        if (args.length < 2) {
            sendUsage(sender);

            return false;
        }

        if ("set".equals(args[0])) {
            if (!sender.hasPermission("nukkit.command.time.set")) {
                sendNoPermissionMessage(sender);

                return true;
            }

            int value;
            if ("day".equals(args[1])) {
                value = Level.TIME_DAY;
            } else if ("night".equals(args[1])) {
                value = Level.TIME_NIGHT;
            } else if ("midnight".equals(args[1])) {
                value = Level.TIME_MIDNIGHT;
            } else if ("noon".equals(args[1])) {
                value = Level.TIME_NOON;
            } else if ("sunrise".equals(args[1])) {
                value = Level.TIME_SUNRISE;
            } else if ("sunset".equals(args[1])) {
                value = Level.TIME_SUNSET;
            } else {
                try {
                    value = Math.max(0, Integer.parseInt(args[1]));
                } catch (Exception e) {
                    sendUsage(sender);
                    return true;
                }
            }

            for (Level level : sender.getServer().getLevels().values()) {
                level.checkTime();
                level.setTime(value);
                level.checkTime();
            }
            Command.broadcastCommandMessage(sender, TranslationKey.Commands.TIME_SET.with(Integer.toString(value)));
        } else if ("add".equals(args[0])) {
            if (!sender.hasPermission("nukkit.command.time.add")) {
                sendNoPermissionMessage(sender);

                return true;
            }

            int value;
            try {
                value = Math.max(0, Integer.parseInt(args[1]));
            } catch (Exception e) {
                sendUsage(sender);
                return true;
            }

            for (Level level : sender.getServer().getLevels().values()) {
                level.checkTime();
                level.setTime(level.getTime() + value);
                level.checkTime();
            }
            Command.broadcastCommandMessage(sender, TranslationKey.Commands.TIME_ADDED.with(Integer.toString(value)));
        } else {
            sendUsage(sender);
        }

        return true;
    }
}
