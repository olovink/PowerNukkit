package cn.nukkit.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.api.PowerNukkitDifference;
import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.command.data.*;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.lang.TranslationKey;
import cn.nukkit.permission.Permissible;
import cn.nukkit.utils.TextFormat;
import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import io.netty.util.internal.EmptyArrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public abstract class Command {

    private static CommandData defaultDataTemplate;

    protected CommandData commandData;

    private final String name;

    private String nextLabel;

    private String label;

    private String[] aliases;

    private String[] activeAliases;

    private CommandMap commandMap;

    protected String description;

    protected String usageMessage;

    private String permission;

    private String permissionMessage;

    protected Map<String, CommandParameter[]> commandParameters = new HashMap<>();

    public Timing timing;

    public Command(String name) {
        this(name, "", null, EmptyArrays.EMPTY_STRINGS);
    }

    public Command(String name, String description) {
        this(name, description, null, EmptyArrays.EMPTY_STRINGS);
    }

    public Command(String name, String description, String usageMessage) {
        this(name, description, usageMessage, EmptyArrays.EMPTY_STRINGS);
    }

    public Command(String name, String description, String usageMessage, String[] aliases) {
        this.commandData = new CommandData();
        this.name = name.toLowerCase(); // Uppercase letters crash the client?!?
        this.nextLabel = name;
        this.label = name;
        this.description = description;
        this.usageMessage = usageMessage == null ? "/" + name : usageMessage;
        this.aliases = aliases;
        this.activeAliases = aliases;
        this.timing = Timings.getCommandTiming(this);
        this.commandParameters.put("default", new CommandParameter[]{CommandParameter.newType("args", true, CommandParamType.RAWTEXT)});
    }

    /**
     * Returns an CommandData containing command data
     *
     * @return CommandData
     */
    public CommandData getDefaultCommandData() {
        return this.commandData;
    }

    public CommandParameter[] getCommandParameters(String key) {
        return commandParameters.get(key);
    }

    public Map<String, CommandParameter[]> getCommandParameters() {
        return commandParameters;
    }

    public void setCommandParameters(Map<String, CommandParameter[]> commandParameters) {
        this.commandParameters = commandParameters;
    }

    public void addCommandParameters(String key, CommandParameter[] parameters) {
        this.commandParameters.put(key, parameters);
    }

    /**
     * Generates modified command data for the specified player
     * for AvailableCommandsPacket.
     *
     * @param player player
     * @return CommandData|null
     */
    public CommandDataVersions generateCustomCommandData(Player player) {
        if (!this.testPermission(player)) {
            return null;
        }

        CommandData customData = this.commandData.clone();

        if (getAliases().length > 0) {
            List<String> aliases = new ArrayList<>(Arrays.asList(getAliases()));
            if (!aliases.contains(this.name)) {
                aliases.add(this.name);
            }

            customData.aliases = new CommandEnum(this.name + "Aliases", aliases);
        }

        customData.description = player.getServer().getLanguage().translateString(this.getDescription());
        this.commandParameters.forEach((key, par) -> {
            CommandOverload overload = new CommandOverload();
            overload.input.parameters = par;
            customData.overloads.put(key, overload);
        });
        if (customData.overloads.size() == 0) customData.overloads.put("default", new CommandOverload());
        CommandDataVersions versions = new CommandDataVersions();
        versions.versions.add(customData);
        return versions;
    }

    public Map<String, CommandOverload> getOverloads() {
        return this.commandData.overloads;
    }
    
    protected double parseTilde(String arg, double pos) {
        if (arg.equals("~")) {
            return pos;
        } else if (!arg.startsWith("~")) {
            return Double.parseDouble(arg);
        } else {
            return pos + Double.parseDouble(arg.substring(1));
        }
    }

    /**
     * Execute the the command on primary thread. It's the implementation responsibility to check
     * for permissions with {@link #testPermission(CommandSender)} or {@link #testPermissionSilent(CommandSender)}.
     * 
     * @implSpec The implementation must call {@link #testPermission(CommandSender)} 
     * or {@link #testPermissionSilent(CommandSender)} when appropriate.
     * @return {@code true} if the command was executed successfully, the sender had permission and everything has gone well.  
     */
    @PowerNukkitDifference(since = "FUTURE", info = "Default commands may return false as described in javadoc")
    public abstract boolean execute(CommandSender sender, String commandLabel, String[] args);

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean testPermission(CommandSender target) {
        if (this.testPermissionSilent(target)) {
            return true;
        }

        if (this.permissionMessage == null) {
            target.sendMessage(TranslationKey.Commands.GENERIC_UNKNOWN.with(TextFormat.RED, this.name));
        } else if (!this.permissionMessage.equals("")) {
            target.sendMessage(this.permissionMessage.replace("<permission>", this.permission));
        }

        return false;
    }

    public boolean testPermissionSilent(CommandSender target) {
        if (this.permission == null || this.permission.equals("")) {
            return true;
        }

        String[] permissions = this.permission.split(";");
        for (String permission : permissions) {
            if (target.hasPermission(permission)) {
                return true;
            }
        }

        return false;
    }

    public String getLabel() {
        return label;
    }

    public boolean setLabel(String name) {
        this.nextLabel = name;
        if (!this.isRegistered()) {
            this.label = name;
            this.timing = Timings.getCommandTiming(this);
            return true;
        }
        return false;
    }

    public boolean register(CommandMap commandMap) {
        if (this.allowChangesFrom(commandMap)) {
            this.commandMap = commandMap;
            return true;
        }
        return false;
    }

    public boolean unregister(CommandMap commandMap) {
        if (this.allowChangesFrom(commandMap)) {
            this.commandMap = null;
            this.activeAliases = this.aliases;
            this.label = this.nextLabel;
            return true;
        }
        return false;
    }

    public boolean allowChangesFrom(CommandMap commandMap) {
        return commandMap != null && !commandMap.equals(this.commandMap);
    }

    public boolean isRegistered() {
        return this.commandMap != null;
    }

    public String[] getAliases() {
        return this.activeAliases;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usageMessage;
    }

    public void setAliases(String[] aliases) {
        this.aliases = aliases;
        if (!this.isRegistered()) {
            this.activeAliases = aliases;
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
    }

    public void setUsage(String usageMessage) {
        this.usageMessage = usageMessage;
    }

    public static CommandData generateDefaultData() {
        if (defaultDataTemplate == null) {
            //defaultDataTemplate = new Gson().fromJson(new InputStreamReader(Server.class.getClassLoader().getResourceAsStream("command_default.json")));
        }
        return defaultDataTemplate.clone();
    }

    public static void broadcastCommandMessage(CommandSender source, String message) {
        broadcastCommandMessage(source, message, true);
    }

    public static void broadcastCommandMessage(CommandSender source, String message, boolean sendToSource) {
        Set<Permissible> users = source.getServer().getPluginManager().getPermissionSubscriptions(Server.BROADCAST_CHANNEL_ADMINISTRATIVE);

        TranslationContainer result = TranslationKey.Chat.TYPE_ADMIN.with(source.getName(), message);

        TranslationContainer colored = TranslationKey.Chat.TYPE_ADMIN.with(TextFormat.GRAY, TextFormat.ITALIC, source.getName(), message);

        if (sendToSource && !(source instanceof ConsoleCommandSender)) {
            source.sendMessage(message);
        }

        for (Permissible user : users) {
            if (user instanceof CommandSender) {
                if (user instanceof ConsoleCommandSender) {
                    ((ConsoleCommandSender) user).sendMessage(result);
                } else if (!user.equals(source)) {
                    ((CommandSender) user).sendMessage(colored);
                }
            }
        }
    }

    public static void broadcastCommandMessage(CommandSender source, TextContainer message) {
        broadcastCommandMessage(source, message, true);
    }

    public static void broadcastCommandMessage(CommandSender source, TextContainer message, boolean sendToSource) {
        TextContainer m = message.clone();
        String resultStr = "[" + source.getName() + ": " + (!m.getText().equals(source.getServer().getLanguage().get(m.getText())) ? "%" : "") + m.getText() + "]";

        Set<Permissible> users = source.getServer().getPluginManager().getPermissionSubscriptions(Server.BROADCAST_CHANNEL_ADMINISTRATIVE);

        String coloredStr = TextFormat.GRAY + "" + TextFormat.ITALIC + resultStr;

        m.setText(resultStr);
        TextContainer result = m.clone();
        m.setText(coloredStr);
        TextContainer colored = m.clone();

        if (sendToSource && !(source instanceof ConsoleCommandSender)) {
            source.sendMessage(message);
        }

        for (Permissible user : users) {
            if (user instanceof CommandSender) {
                if (user instanceof ConsoleCommandSender) {
                    ((ConsoleCommandSender) user).sendMessage(result);
                } else if (!user.equals(source)) {
                    ((CommandSender) user).sendMessage(colored);
                }
            }
        }
    }
    
    @PowerNukkitOnly
    @Since("FUTURE")
    public void sendUsage(@Nullable CommandSender sender) {
        if (sender != null) {
            sender.sendMessage(TranslationKey.Commands.GENERIC_USAGE.with(this.getUsage()));
        }
    }

    @PowerNukkitOnly
    @Since("FUTURE")
    public void sendNoPermissionMessage(@Nullable CommandSender sender) {
        if (sender == null) {
            return;
        }
        if (sender.getServer().isLanguageForced()) {
            // When the language is forced, we have the "commands.generic.usage", this was removed from the client
            sender.sendMessage(TextFormat.RED + "%commands.generic.usage");
        } else {
            // It's a message from the /tp command but can be reused easily because the actual message is:
            // You do not have permission to use this slash command.
            sender.sendMessage(TranslationKey.Commands.TP_PERMISSION.with(TextFormat.RED));
        }
    }

    @PowerNukkitOnly
    @Since("FUTURE")
    public void sendInGameMessage(@Nullable CommandSender sender) {
        if (sender != null) {
            sender.sendMessage(new TranslationContainer("commands.generic.ingame"));
        }
    }

    @PowerNukkitOnly
    @Since("FUTURE")
    protected boolean missingPermissionOrArgs(@Nonnull CommandSender sender, @Nonnull String[] args, int requiredArgsLen) {
        if (!this.testPermission(sender)) {
            return false;
        }
        
        if (args.length < requiredArgsLen) {
            this.sendUsage(sender);
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
