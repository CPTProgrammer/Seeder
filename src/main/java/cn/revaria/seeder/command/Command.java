package cn.revaria.seeder.command;

import cn.revaria.seeder.command.argument.DefaultOrIntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.literal;

public class Command {
    public static void registerAll() {
        // Root command
        LiteralArgumentBuilder<ServerCommandSource> prefix = getRootCommand();

        ArgumentTypeRegistry.registerArgumentType(new Identifier("seeder", "default_or_integer"), DefaultOrIntegerArgumentType.class, ConstantArgumentSerializer.of(() -> DefaultOrIntegerArgumentType.integer()));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(prefix.then(LocateCommand.getCommand(registryAccess)));
            dispatcher.register(prefix.then(ConfigCommand.getCommand()));
            dispatcher.register(prefix.then(StopCommand.getCommand()));
        });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> getRootCommand() {
        return literal("seeder").requires(source -> source.hasPermissionLevel(2));
    }
}
