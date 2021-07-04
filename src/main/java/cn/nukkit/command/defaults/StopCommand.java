package cn.nukkit.command.defaults;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.lang.TranslationKey;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class StopCommand extends VanillaCommand {

    public StopCommand(String name) {
        super(name, "%nukkit.command.stop.description", "%commands.stop.usage");
        this.setPermission("nukkit.command.stop");
        this.commandParameters.clear();
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        Command.broadcastCommandMessage(sender, TranslationKey.COMMANDS_STOP_START.container());

        sender.getServer().shutdown();

        return true;
    }
}
