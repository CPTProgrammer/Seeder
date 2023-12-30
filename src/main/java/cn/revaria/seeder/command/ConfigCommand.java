package cn.revaria.seeder.command;

import cn.revaria.seeder.config.SeederConfig;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.Arrays;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ConfigCommand {
    private static final DynamicCommandExceptionType SET_CONFIGURATION_FAILURE_EXCEPTION = new DynamicCommandExceptionType(conf -> Text.translatable("seeder.command.config.failure.set", conf));
    private static final DynamicCommandExceptionType GET_CONFIGURATION_FAILURE_EXCEPTION = new DynamicCommandExceptionType(conf -> Text.translatable("seeder.command.config.failure.get", conf));

    public static LiteralArgumentBuilder<ServerCommandSource> getCommand() {
        Field[] configFields = Arrays.stream(SeederConfig.class.getFields())
            .filter(field -> field.isAnnotationPresent(MidnightConfig.Entry.class))
            .toArray(Field[]::new);

        Paginator paginator = new Paginator("Seeder", Text.translatable("seeder.paginator.title"), 5, "/seeder config page ", Arrays.stream(configFields).map(field ->
            Text.literal(field.getName() + " ").styled(style -> style.withColor(Formatting.GRAY)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Text.translatable("seeder.midnightconfig." + field.getName()).append("\n")
                                .append(Text.translatable("seeder.paginator.type").styled(style1 -> style1.withColor(Formatting.GRAY))).append(field.getType().getSimpleName())
                        )
                    ).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/seeder config " + field.getName() + " "))
                )
                .append(Text.literal("(" + field.getType().getSimpleName() + ")").styled(style -> style.withColor(Formatting.DARK_GRAY)))
        ).toArray(MutableText[]::new));

        LiteralArgumentBuilder<ServerCommandSource> configCommand = literal("config")
            .then(
                literal("page").then(
                    argument("page", IntegerArgumentType.integer(1, paginator.getPageCount()))
                        .executes(context -> {
                            paginator.print(context.getSource(), context.getArgument("page", Integer.class));
                            return 1;
                        })
                )
            )
            .executes(context -> {
                paginator.print(context.getSource(), 1);
                return 1;
            });

        for (Field field : configFields) {
            configCommand = configCommand.then(
                literal(field.getName()).then(
                    argument(field.getName(), switch (field.getType().getSimpleName()) {
                        case "int" ->
                            IntegerArgumentType.integer((int) field.getAnnotation(MidnightConfig.Entry.class).min(), (int) field.getAnnotation(MidnightConfig.Entry.class).max());
                        default -> null;
                    })
                        .executes(context -> {
                            try {
                                field.set(null, context.getArgument(field.getName(), switch (field.getType().getSimpleName()) {
                                    case "int" -> Integer.class;
                                    default -> null;
                                }));
                                context.getSource().sendFeedback(() ->
                                    Text.translatable("seeder.command.config.success",
                                        Text.literal(field.getName()).styled(style -> style.withColor(Formatting.GREEN)),
                                        Text.literal(String.valueOf(context.getArgument(field.getName(), switch (field.getType().getSimpleName()) {
                                            case "int" -> Integer.class;
                                            default -> null;
                                        }))).styled(style -> style.withColor(Formatting.GREEN))), false
                                );
                            } catch (IllegalAccessException e) {
                                throw SET_CONFIGURATION_FAILURE_EXCEPTION.create(field.getName());
                            }
                            return 1;
                        })
                ).executes(context -> {
                    try {
                        var value = field.get(null);
                        context.getSource().sendFeedback(() -> Text.translatable("seeder.command.config.current", Text.literal(field.getName()).styled(style -> style.withColor(Formatting.GREEN)), Text.literal(String.valueOf(value)).styled(style -> style.withColor(Formatting.GREEN))), false);
                    } catch (IllegalAccessException e) {
                        throw GET_CONFIGURATION_FAILURE_EXCEPTION.create(field.getName());
                    }
                    return 1;
                })
            );
        }

        return configCommand;
    }
}
