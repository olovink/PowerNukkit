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
public class OpCommand extends VanillaCommand {

    public OpCommand(String name) {
        super(name, "%nukkit.command.op.description", "%commands.op.description");
        this.setPermission("nukkit.command.op.give");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("player", CommandParamType.TARGET)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (missingPermissionOrArgs(sender, args, 1)) {
            return false;
        }
        String name = args[0];
        @SuppressWarnings("deprecation") IPlayer player = sender.getServer().getOfflinePlayer(name);

        Command.broadcastCommandMessage(sender, TranslationKey.COMMANDS_OP_SUCCESS.with(player.getName()));
        if (player instanceof Player) {
            ((Player) player).sendMessage(TranslationKey.COMMANDS_OP_MESSAGE.with(TextFormat.GRAY));
        }

        player.setOp(true);

        return true;
    }
}
