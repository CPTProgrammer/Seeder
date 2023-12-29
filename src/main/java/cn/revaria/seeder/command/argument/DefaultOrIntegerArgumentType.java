package cn.revaria.seeder.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class DefaultOrIntegerArgumentType implements ArgumentType<DefaultOrIntegerArgument> {
    public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());
        if (!reader.canRead(1)) {
            return builder.buildFuture();
        }
        if (reader.peek() == 'd') {
            builder.suggest("default");
            return builder.buildFuture();
        }
        return builder.buildFuture();
    };

    private static final Collection<String> EXAMPLES = Arrays.asList("default", "0", "123", "-123");

    private final int minimum;
    private final int maximum;

    private DefaultOrIntegerArgumentType(final int minimum, final int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public static DefaultOrIntegerArgumentType integer() {
        return integer(Integer.MIN_VALUE);
    }

    public static DefaultOrIntegerArgumentType integer(final int min) {
        return integer(min, Integer.MAX_VALUE);
    }

    public static DefaultOrIntegerArgumentType integer(final int min, final int max) {
        return new DefaultOrIntegerArgumentType(min, max);
    }

    public static int getInteger(final CommandContext<?> context, final String name) {
        return context.getArgument(name, int.class);
    }

    @Override
    public DefaultOrIntegerArgument parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        if (reader.peek() == 'd') {
            final String result = reader.readUnquotedString();
            if (!Objects.equals(result, "default")) {
                reader.setCursor(start);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, "default");
            }
            return new DefaultOrIntegerArgument(true, 0);
        }
        final int result = reader.readInt();
        if (result < minimum) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(reader, result, minimum);
        }
        if (result > maximum) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(reader, result, maximum);
        }
        return new DefaultOrIntegerArgument(false, result);
    }

    @Override
    public int hashCode() {
        return 31 * minimum + maximum;
    }

    @Override
    public String toString() {
        if (minimum == Integer.MIN_VALUE && maximum == Integer.MAX_VALUE) {
            return "default_or_integer()";
        } else if (maximum == Integer.MAX_VALUE) {
            return "default_or_integer(" + minimum + ")";
        } else {
            return "default_or_integer(" + minimum + ", " + maximum + ")";
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}