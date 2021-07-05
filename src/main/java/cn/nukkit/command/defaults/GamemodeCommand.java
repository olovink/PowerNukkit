package cn.nukkit.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.TranslationKey;
import cn.nukkit.utils.TextFormat;

/**
 * @author xtypr
 * @since 2015/11/13
 */
public class GamemodeCommand extends VanillaCommand {

    public GamemodeCommand(String name) {
        super(name, "%nukkit.command.gamemode.description", "%commands.gamemode.usage",
                new String[]{"gm"});
        this.setPermission("nukkit.command.gamemode.survival;" +
                "nukkit.command.gamemode.creative;" +
                "nukkit.command.gamemode.adventure;" +
                "nukkit.command.gamemode.spectator;" +
                "nukkit.command.gamemode.other");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("gameMode", CommandParamType.INT),
                CommandParameter.newType("player", true, CommandParamType.TARGET)
        });
        this.commandParameters.put("byString", new CommandParameter[]{
                CommandParameter.newEnum("gameMode", CommandEnum.ENUM_GAMEMODE),
                CommandParameter.newType("player", true, CommandParamType.TARGET)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return false;
        }

        int gameMode = Server.getGamemodeFromString(args[0]);
        if (gameMode == -1) {
            sender.sendMessage(TranslationKey.Commands.GAMEMODE_FAIL_INVALID.with(args[0]));
            return false;
        }

        CommandSender target = sender;
        if (args.length > 1) {
            if (sender.hasPermission("nukkit.command.gamemode.other")) {
                target = sender.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(TranslationKey.Commands.GENERIC_PLAYER_NOTFOUND.with(TextFormat.RED));
                    return false;
                }
            } else {
                sendNoPermissionMessage(sender);
                return false;
            }
        } else if (!(sender instanceof Player)) {
            sendUsage(sender);
            return true;
        }

        if ((gameMode == 0 && !sender.hasPermission("nukkit.command.gamemode.survival")) ||
                (gameMode == 1 && !sender.hasPermission("nukkit.command.gamemode.creative")) ||
                (gameMode == 2 && !sender.hasPermission("nukkit.command.gamemode.adventure")) ||
                (gameMode == 3 && !sender.hasPermission("nukkit.command.gamemode.spectator"))) {
            sendNoPermissionMessage(sender);
            return true;
        }

        if (!((Player) target).setGamemode(gameMode)) {
            sender.sendMessage("Game mode update for " + target.getName() + " failed");
        } else {
            if (target.equals(sender)) {
                Command.broadcastCommandMessage(sender, TranslationKey.Commands.GAMEMODE_SUCCESS_SELF.with(Server.getGamemodeString(gameMode)));
            } else {
                //TODO joserobjr target.sendMessage(TranslationKey.GAMEMODE_CHANGED.with(Server.getGamemodeString(gameMode)));
                //TODO joserobjr Command.broadcastCommandMessage(sender, TranslationKey.COMMANDS_GAMEMODE_SUCCESS_OTHER.with(target.getName(), Server.getGamemodeString(gameMode)));
            }
        }

        return true;
    }
}
