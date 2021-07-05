package cn.nukkit.command.defaults;

import cn.nukkit.IPlayer;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.TranslationKey;

/**
 * @author xtypr
 * @since 2015/11/12
 */
public class WhitelistCommand extends VanillaCommand {

    public WhitelistCommand(String name) {
        super(name, "%nukkit.command.whitelist.description", "%commands.whitelist.usage");
        this.setPermission(
                "nukkit.command.whitelist.reload;" +
                        "nukkit.command.whitelist.enable;" +
                        "nukkit.command.whitelist.disable;" +
                        "nukkit.command.whitelist.list;" +
                        "nukkit.command.whitelist.add;" +
                        "nukkit.command.whitelist.remove"
        );
        this.commandParameters.clear();
        this.commandParameters.put("1arg", new CommandParameter[]{
                CommandParameter.newEnum("action", new CommandEnum("WhitelistAction", "on", "off", "list", "reload"))
        });
        this.commandParameters.put("2args", new CommandParameter[]{
                CommandParameter.newEnum("action", new CommandEnum("WhitelistPlayerAction", "add", "remove")),
                CommandParameter.newType("player", CommandParamType.TARGET)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (args.length == 0 || args.length > 2) {
            sendUsage(sender);
            return true;
        }

        if (args.length == 1) {
            if (this.badPerm(sender, args[0].toLowerCase())) {
                return false;
            }
            switch (args[0].toLowerCase()) {
                case "reload":
                    sender.getServer().reloadWhitelist();
                    Command.broadcastCommandMessage(sender, TranslationKey.Commands.WHITELIST_RELOADED.container());

                    return true;
                case "on":
                    sender.getServer().setPropertyBoolean("white-list", true);
                    Command.broadcastCommandMessage(sender, TranslationKey.Commands.WHITELIST_ENABLED.container());

                    return true;
                case "off":
                    sender.getServer().setPropertyBoolean("white-list", false);
                    Command.broadcastCommandMessage(sender, TranslationKey.Commands.WHITELIST_DISABLED.container());

                    return true;
                case "list":
                    StringBuilder result = new StringBuilder();
                    int count = 0;
                    for (String player : sender.getServer().getWhitelist().getAll().keySet()) {
                        result.append(player).append(", ");
                        ++count;
                    }
                    sender.sendMessage(TranslationKey.Commands.WHITELIST_LIST.with(Integer.toString(count), Integer.toString(count)));
                    sender.sendMessage(result.length() > 0 ? result.substring(0, result.length() - 2) : "");

                    return true;

                case "add":
                    sender.sendMessage(TranslationKey.Commands.GENERIC_USAGE.with("%commands.whitelist.add.usage"));
                    return true;

                case "remove":
                    sender.sendMessage(TranslationKey.Commands.GENERIC_USAGE.with("%commands.whitelist.remove.usage"));
                    return true;
            }
        } else if (args.length == 2) {
            if (this.badPerm(sender, args[0].toLowerCase())) {
                return false;
            }
            switch (args[0].toLowerCase()) {
                case "add": {
                    @SuppressWarnings("deprecation")
                    final IPlayer offlinePlayer = sender.getServer().getOfflinePlayer(args[1]);
                    offlinePlayer.setWhitelisted(true);
                    Command.broadcastCommandMessage(sender, TranslationKey.Commands.WHITELIST_ADD_SUCCESS.with(args[1]));

                    return true;
                }
                case "remove": {
                    @SuppressWarnings("deprecation")
                    final IPlayer offlinePlayer = sender.getServer().getOfflinePlayer(args[1]);
                    offlinePlayer.setWhitelisted(false);
                    Command.broadcastCommandMessage(sender, TranslationKey.Commands.WHITELIST_REMOVE_SUCCESS.with(args[1]));

                    return true;
                }
            }
        }

        return true;
    }

    private boolean badPerm(CommandSender sender, String perm) {
        if (!sender.hasPermission("nukkit.command.whitelist." + perm)) {
            sendNoPermissionMessage(sender);

            return true;
        }

        return false;
    }
}
