package cn.nukkit.command.defaults;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.TranslationKey;
import cn.nukkit.permission.BanEntry;
import cn.nukkit.permission.BanList;

import java.util.Iterator;

/**
 * @author xtypr
 * @since 2015/11/11
 */
public class BanListCommand extends VanillaCommand {
    public BanListCommand(String name) {
        super(name, "%nukkit.command.banlist.description", "%commands.banlist.usage");
        this.setPermission("nukkit.command.ban.list");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newEnum("type", true, new CommandEnum("BanListType", "ips", "players"))
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        BanList list;
        boolean ips = false;
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "ips":
                    list = sender.getServer().getIPBans();
                    ips = true;
                    break;
                case "players":
                    list = sender.getServer().getNameBans();
                    break;
                default:
                    sender.sendMessage(TranslationKey.COMMANDS_GENERIC_USAGE.with(this.usageMessage));
                    return false;
            }
        } else {
            list = sender.getServer().getNameBans();
        }

        StringBuilder builder = new StringBuilder();
        Iterator<BanEntry> itr = list.getEntires().values().iterator();
        while (itr.hasNext()) {
            builder.append(itr.next().getName());
            if (itr.hasNext()) {
                builder.append(", ");
            }
        }

        if (ips) {
            sender.sendMessage(TranslationKey.COMMANDS_BANLIST_IPS.with(Integer.toString(list.getEntires().size())));
        } else {
            sender.sendMessage(TranslationKey.COMMANDS_BANLIST_PLAYERS.with(Integer.toString(list.getEntires().size())));
        }
        sender.sendMessage(builder.toString());
        return true;
    }
}
