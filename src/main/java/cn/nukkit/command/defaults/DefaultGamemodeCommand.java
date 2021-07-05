package cn.nukkit.command.defaults;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.TranslationKey;

/**
 * @author xtypr
 * @since 2015/11/12
 */
public class DefaultGamemodeCommand extends VanillaCommand {

    public DefaultGamemodeCommand(String name) {
        super(name, "%nukkit.command.defaultgamemode.description", "%commands.defaultgamemode.usage");
        this.setPermission("nukkit.command.defaultgamemode");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("gameMode", CommandParamType.INT)
        });
        this.commandParameters.put("byString", new CommandParameter[]{
                CommandParameter.newEnum("gameMode", CommandEnum.ENUM_GAMEMODE)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (missingPermissionOrArgs(sender, args, 1)) {
            return false;
        }
        int gameMode = Server.getGamemodeFromString(args[0]);
        if (gameMode != -1) {
            sender.getServer().setPropertyInt("gamemode", gameMode);
            sender.sendMessage(TranslationKey.Commands.DEFAULTGAMEMODE_SUCCESS.with(Server.getGamemodeString(gameMode)));
        } else {
            sender.sendMessage(TranslationKey.Commands.GAMEMODE_FAIL_INVALID.with(args[0])); //
        }
        return true;
    }
}
