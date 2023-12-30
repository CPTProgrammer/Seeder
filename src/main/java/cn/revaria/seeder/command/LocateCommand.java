package cn.revaria.seeder.command;

import cn.revaria.seeder.command.argument.DefaultOrIntegerArgument;
import cn.revaria.seeder.command.argument.DefaultOrIntegerArgumentType;
import cn.revaria.seeder.config.SeederConfig;
import cn.revaria.seeder.service.LocateService;
import com.google.common.base.Stopwatch;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

import static net.minecraft.command.argument.RegistryEntryArgumentType.getRegistryEntry;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.LocateCommand.sendCoordinates;

public class LocateCommand {
    private static final DynamicCommandExceptionType STRUCTURE_INVALID_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("commands.locate.structure.invalid", id));
    private static final DynamicCommandExceptionType BIOME_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("commands.locate.biome.not_found", id));
    private static final SimpleCommandExceptionType BIOME_THREAD_OCCUPANCY_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("seeder.command.locate.thread_occupancy",
        Text.translatable("seeder.command.locate.thread_occupancy.stop").styled(style -> style.withColor(Formatting.DARK_RED).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/seeder stop locate biome")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("seeder.command.locate.thread_occupancy.stop.tooltip"))))
    ));

    public static LiteralArgumentBuilder<ServerCommandSource> getCommand(CommandRegistryAccess registryAccess) {
        return literal("locate").then(
            literal("biome").then(
                argument("biome", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BIOME))
                    .then(argument("radius", DefaultOrIntegerArgumentType.integer(1)).suggests(DefaultOrIntegerArgumentType.SUGGESTION_PROVIDER)
                        .then(argument("horizontalBlockCheckInterval", DefaultOrIntegerArgumentType.integer(1)).suggests(DefaultOrIntegerArgumentType.SUGGESTION_PROVIDER)
                            .then(argument("verticalBlockCheckInterval", DefaultOrIntegerArgumentType.integer(1)).suggests(DefaultOrIntegerArgumentType.SUGGESTION_PROVIDER)
                                .executes(context -> executeLocateBiome(context.getSource(), getRegistryEntry(context, "biome", RegistryKeys.BIOME),
                                    (context.getArgument("radius", DefaultOrIntegerArgument.class).isDefault ? SeederConfig.locateBiomeDefaultRadius : context.getArgument("radius", DefaultOrIntegerArgument.class).value),
                                    (context.getArgument("horizontalBlockCheckInterval", DefaultOrIntegerArgument.class).isDefault ? SeederConfig.locateBiomeDefaultHorizontalBlockCheckInterval : context.getArgument("horizontalBlockCheckInterval", DefaultOrIntegerArgument.class).value),
                                    (context.getArgument("verticalBlockCheckInterval", DefaultOrIntegerArgument.class).isDefault ? SeederConfig.locateBiomeDefaultVerticalBlockCheckInterval : context.getArgument("verticalBlockCheckInterval", DefaultOrIntegerArgument.class).value)))))
                        .executes(context -> executeLocateBiome(context.getSource(), getRegistryEntry(context, "biome", RegistryKeys.BIOME),
                            (context.getArgument("radius", DefaultOrIntegerArgument.class).isDefault ? SeederConfig.locateBiomeDefaultRadius : context.getArgument("radius", DefaultOrIntegerArgument.class).value))))
                    .executes(context -> executeLocateBiome(context.getSource(), getRegistryEntry(context, "biome", RegistryKeys.BIOME)))
            )
        );
    }

    private static int executeLocateBiome(ServerCommandSource source, RegistryEntry.Reference<Biome> registryEntry, int radius, int horizontalBlockCheckInterval, int verticalBlockCheckInterval) throws CommandSyntaxException {
        BlockPos blockPos = BlockPos.ofFloored(source.getPosition());
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);

        if (!LocateService.canLocateBiomeTask()) {
            throw BIOME_THREAD_OCCUPANCY_EXCEPTION.create();
        }
        LocateService.locateBiome((pair) -> {
            stopwatch.stop();
            if (pair == null) {
                source.sendError(Texts.toText(BIOME_NOT_FOUND_EXCEPTION.create(registryEntry.registryKey().getValue().toString()).getRawMessage()));
            }
            sendCoordinates(source, new EntryBased<>(registryEntry), blockPos, pair, "seeder.command.locate.biome.success", true, stopwatch.elapsed());
        }, blockPos, radius, horizontalBlockCheckInterval, verticalBlockCheckInterval, registryEntry, source.getWorld().getChunkManager().getNoiseConfig().getMultiNoiseSampler(), source.getWorld());
        return 1;
    }

    private static int executeLocateBiome(ServerCommandSource source, RegistryEntry.Reference<Biome> registryEntry) throws CommandSyntaxException {
        return executeLocateBiome(source, registryEntry, SeederConfig.locateBiomeDefaultRadius, SeederConfig.locateBiomeDefaultHorizontalBlockCheckInterval, SeederConfig.locateBiomeDefaultVerticalBlockCheckInterval);
    }

    private static int executeLocateBiome(ServerCommandSource source, RegistryEntry.Reference<Biome> registryEntry, int radius) throws CommandSyntaxException {
        return executeLocateBiome(source, registryEntry, radius, SeederConfig.locateBiomeDefaultHorizontalBlockCheckInterval, SeederConfig.locateBiomeDefaultVerticalBlockCheckInterval);
    }

    private record EntryBased<T>(
        RegistryEntry.Reference<T> value) implements RegistryEntryPredicateArgumentType.EntryPredicate<T> {
        EntryBased(RegistryEntry.Reference<T> value) {
            this.value = value;
        }

        public Either<RegistryEntry.Reference<T>, RegistryEntryList.Named<T>> getEntry() {
            return Either.left(this.value);
        }

        public <E> Optional<RegistryEntryPredicateArgumentType.EntryPredicate<E>> tryCast(RegistryKey<? extends Registry<E>> registryRef) {
            return this.value.registryKey().isOf(registryRef) ? Optional.of((RegistryEntryPredicateArgumentType.EntryPredicate<E>) this) : Optional.empty();
        }

        public boolean test(RegistryEntry<T> registryEntry) {
            return registryEntry.equals(this.value);
        }

        public String asString() {
            return this.value.registryKey().getValue().toString();
        }

        public RegistryEntry.Reference<T> value() {
            return this.value;
        }
    }
}
