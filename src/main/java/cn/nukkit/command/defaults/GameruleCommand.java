package cn.nukkit.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.TranslationKey;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;

import java.util.*;

public class GameruleCommand extends VanillaCommand {

    public GameruleCommand(String name) {
        super(name, "%nukkit.command.gamerule.description", "%nukkit.command.gamerule.usage");
        this.setPermission("nukkit.command.gamerule");
        this.commandParameters.clear();

        GameRules rules = GameRules.getDefault();
        List<String> boolGameRules = new ArrayList<>();
        List<String> intGameRules = new ArrayList<>();
        List<String> floatGameRules = new ArrayList<>();
        List<String> unknownGameRules = new ArrayList<>();

        rules.getGameRules().forEach((rule, value) -> {
            if (rule.isDeprecated()) {
                return;
            }
            switch (value.getType()) {
                case BOOLEAN:
                    boolGameRules.add(rule.getName().toLowerCase());
                    break;
                case INTEGER:
                    intGameRules.add(rule.getName().toLowerCase());
                    break;
                case FLOAT:
                    floatGameRules.add(rule.getName().toLowerCase());
                    break;
                case UNKNOWN:
                default:
                    unknownGameRules.add(rule.getName().toLowerCase());
                    break;
            }
        });
        
        String paramName = "rule";
        String paramValue = "value";

        if (!boolGameRules.isEmpty()) {
            this.commandParameters.put("boolGameRules", new CommandParameter[]{
                    CommandParameter.newEnum(paramName, new CommandEnum("BoolGameRule", boolGameRules)),
                    CommandParameter.newEnum(paramValue, true, CommandEnum.ENUM_BOOLEAN)
            });
        }
        if (!intGameRules.isEmpty()) {
            this.commandParameters.put("intGameRules", new CommandParameter[]{
                    CommandParameter.newEnum(paramName, new CommandEnum("IntGameRule", intGameRules)),
                    CommandParameter.newType(paramValue, true, CommandParamType.INT)
            });
        }
        if (!floatGameRules.isEmpty()) {
            this.commandParameters.put("floatGameRules", new CommandParameter[]{
                    CommandParameter.newEnum(paramName, new CommandEnum("FloatGameRule", floatGameRules)),
                    CommandParameter.newType(paramValue, true, CommandParamType.FLOAT)
            });
        }
        if (!unknownGameRules.isEmpty()) {
            this.commandParameters.put("unknownGameRules", new CommandParameter[]{
                    CommandParameter.newEnum(paramName, new CommandEnum("UnknownGameRule", unknownGameRules)),
                    CommandParameter.newType(paramValue, true, CommandParamType.STRING)
            });
        }
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (!sender.isPlayer()) {
            sendInGameMessage(sender);
            return false;
        }
        GameRules rules = ((Player) sender).getLevel().getGameRules();

        switch (args.length) {
            case 0:
                StringJoiner rulesJoiner = new StringJoiner(", ");
                for (GameRule rule: rules.getRules()) {
                    rulesJoiner.add(rule.getName().toLowerCase());
                }
                sender.sendMessage(rulesJoiner.toString());
                return true;
            case 1:
                Optional<GameRule> gameRule = GameRule.parseString(args[0]);
                if (!gameRule.isPresent() || !rules.hasRule(gameRule.get())) {
                    sender.sendMessage(TranslationKey.Commands.GENERIC_SYNTAX.with("/gamerule", args[0]));
                    return false;
                }

                sender.sendMessage(gameRule.get().getName() .toLowerCase()+ " = " + rules.getString(gameRule.get()));
                return true;
            default:
                Optional<GameRule> optionalRule = GameRule.parseString(args[0]);

                if (!optionalRule.isPresent()) {
                    sender.sendMessage(TranslationKey.Commands.GENERIC_SYNTAX.with(
                            "/gamerule ", args[0], " " + String.join(" ", Arrays.copyOfRange(args, 1, args.length))));
                    return false;
                }

                try {
                    rules.setGameRules(optionalRule.get(), args[1]);
                    sender.sendMessage(TranslationKey.Commands.GAMERULE_SUCCESS.with(optionalRule.get().getName().toLowerCase(), args[1]));
                    return true;
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(TranslationKey.Commands.GENERIC_SYNTAX.with("/gamerule "  + args[0] + " ", args[1], " " + String.join(" ", Arrays.copyOfRange(args, 2, args.length))));
                    return false;
                }
        }
    }
}
