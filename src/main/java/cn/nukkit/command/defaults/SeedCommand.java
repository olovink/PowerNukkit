package cn.nukkit.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.lang.TranslationKey;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class SeedCommand extends VanillaCommand {

    public SeedCommand(String name) {
        super(name, "%nukkit.command.seed.description", "%commands.seed.usage");
        this.setPermission("nukkit.command.seed");
        this.commandParameters.clear();
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        long seed;
        if (sender instanceof Player) {
            seed = ((Player) sender).getLevel().getSeed();
        } else {
            seed = sender.getServer().getDefaultLevel().getSeed();
        }

        sender.sendMessage(TranslationKey.Commands.SEED_SUCCESS.with(Long.toString(seed)));

        return true;
    }
}
