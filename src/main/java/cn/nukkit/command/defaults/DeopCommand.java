package cn.nukkit.command.defaults;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.TranslationKey;
import cn.nukkit.utils.TextFormat;

/**
 * @author xtypr
 * @since 2015/11/12
 */
public class DeopCommand extends VanillaCommand {
    public DeopCommand(String name) {
        super(name, "%nukkit.command.deop.description", "%commands.deop.description");
        this.setPermission("nukkit.command.op.take");
        this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("player", CommandParamType.TARGET)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (missingPermissionOrArgs(sender, args, 1)) {
            return false;
        }

        String playerName = args[0];
        @SuppressWarnings("deprecation") IPlayer player = sender.getServer().getOfflinePlayer(playerName);
        player.setOp(false);

        if (player instanceof Player) {
            ((Player) player).sendMessage(TranslationKey.COMMANDS_DEOP_MESSAGE.with(TextFormat.GRAY));
        }

        Command.broadcastCommandMessage(sender, TranslationKey.COMMANDS_DEOP_SUCCESS.with(player.getName()));

        return true;
    }
}
