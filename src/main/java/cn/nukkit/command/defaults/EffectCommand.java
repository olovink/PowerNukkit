package cn.nukkit.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.TranslationKey;
import cn.nukkit.potion.Effect;
import cn.nukkit.potion.InstantEffect;
import cn.nukkit.utils.ServerException;
import cn.nukkit.utils.TextFormat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Snake1999 and Pub4Game
 * @since 2016/1/23
 */
public class EffectCommand extends Command {
    public EffectCommand(String name) {
        super(name, "%nukkit.command.effect.description", "%commands.effect.usage");
        this.setPermission("nukkit.command.effect");
        this.commandParameters.clear();

        List<String> effects = new ArrayList<>();
        for (Field field : Effect.class.getDeclaredFields()) {
            if (field.getType() == int.class && field.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) {
                effects.add(field.getName().toLowerCase());
            }
        }

        this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("player", CommandParamType.TARGET),
                CommandParameter.newEnum("effect", new CommandEnum("Effect", effects)),
                CommandParameter.newType("seconds", true, CommandParamType.INT),
                CommandParameter.newType("amplifier", true, CommandParamType.INT),
                CommandParameter.newEnum("hideParticle", true, CommandEnum.ENUM_BOOLEAN)
        });
        this.commandParameters.put("clear", new CommandParameter[]{
                CommandParameter.newType("player", CommandParamType.TARGET),
                CommandParameter.newEnum("clear", new CommandEnum("ClearEffects", "clear"))
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (missingPermissionOrArgs(sender, args, 2)) {
            return false;
        }
        Player player = sender.getServer().getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(TranslationKey.COMMANDS_GENERIC_PLAYER_NOTFOUND.with(TextFormat.RED));
            return false;
        }
        if (args[1].equalsIgnoreCase("clear")) {
            for (Effect effect : player.getEffects().values()) {
                player.removeEffect(effect.getId());
            }
            sender.sendMessage(TranslationKey.COMMANDS_EFFECT_SUCCESS_REMOVED_ALL.with(player.getDisplayName()));
            return false;
        }
        Effect effect;
        try {
            effect = Effect.getEffect(Integer.parseInt(args[1]));
        } catch (NumberFormatException | ServerException a) {
            try {
                effect = Effect.getEffectByName(args[1]);
            } catch (Exception e) {
                sender.sendMessage(TranslationKey.COMMANDS_EFFECT_NOTFOUND.with(args[1]));
                return false;
            }
        }
        int duration = 300;
        int amplification = 0;
        if (args.length >= 3) {
            try {
                duration = Integer.parseInt(args[2]);
            } catch (NumberFormatException a) {
                sender.sendMessage(TranslationKey.COMMANDS_GENERIC_USAGE.with(this.usageMessage));
                return false;
            }
            if (!(effect instanceof InstantEffect)) {
                duration *= 20;
            }
        } else if (effect instanceof InstantEffect) {
            duration = 1;
        }
        if (args.length >= 4) {
            try {
                amplification = Integer.parseInt(args[3]);
            } catch (NumberFormatException a) {
                sendUsage(sender);
                return false;
            }
        }
        if (args.length >= 5) {
            String v = args[4].toLowerCase();
            if (v.matches("(?i)|on|true|t|1")) {
                effect.setVisible(false);
            }
        }
        if (duration == 0) {
            if (!player.hasEffect(effect.getId())) {
                if (player.getEffects().size() == 0) {
                    sender.sendMessage(TranslationKey.COMMANDS_EFFECT_FAILURE_NOTACTIVE_ALL.with(player.getDisplayName()));
                } else {
                    sender.sendMessage(TranslationKey.COMMANDS_EFFECT_FAILURE_NOTACTIVE.with(effect.getName(), player.getDisplayName()));
                }
                return false;
            }
            player.removeEffect(effect.getId());
            sender.sendMessage(TranslationKey.COMMANDS_EFFECT_SUCCESS_REMOVED.with(effect.getName(), player.getDisplayName()));
        } else {
            effect.setDuration(duration).setAmplifier(amplification);
            player.addEffect(effect);
            Command.broadcastCommandMessage(sender, TranslationKey.COMMANDS_EFFECT_SUCCESS.with(effect.getName(), Integer.toString(effect.getAmplifier()), player.getDisplayName(), Integer.toString(effect.getDuration() / 20)));
        }
        return true;
    }
}
