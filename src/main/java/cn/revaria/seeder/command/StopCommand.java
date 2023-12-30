package cn.revaria.seeder.command;

import cn.revaria.seeder.service.LocateService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class StopCommand {
    private static final SimpleCommandExceptionType NO_THREAD_POOL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("seeder.command.stop.failure.no_thread_pool"));
    private static final SimpleCommandExceptionType TIMEOUT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("seeder.command.stop.failure.timeout"));

    public static LiteralArgumentBuilder<ServerCommandSource> getCommand() {
        return literal("stop").then(
            literal("locate").then(
                literal("biome")
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        source.sendFeedback(() -> Text.translatable("seeder.command.stop.start").styled(style -> style.withColor(Formatting.BLUE)), false);
                        LocateService.stopLocateBiomeTask(NO_THREAD_POOL_EXCEPTION, TIMEOUT_EXCEPTION);
                        source.sendFeedback(() -> Text.translatable("seeder.command.stop.success").styled(style -> style.withColor(Formatting.GREEN)), false);
                        return 1;
                    })
            )
        );
    }
}
