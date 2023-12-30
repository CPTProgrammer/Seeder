package cn.revaria.seeder.service;

import cn.revaria.seeder.config.SeederConfig;
import cn.revaria.seeder.service.task.LocateBiomeTask;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LocateService {

    private static ExecutorService locateBiomeThreadPool;
    public static final AtomicBoolean locateBiomeStopFlag = new AtomicBoolean();
    public static final AtomicInteger locateBiomeFinishedCount = new AtomicInteger();
    public static final AtomicLong locateBiomeProgress = new AtomicLong();
    public static final AtomicLong locateBiomeTotal = new AtomicLong();

    public static void locateBiome(LocateBiomeTask.Callback callback, BlockPos origin, int radius,
                                   int horizontalBlockCheckInterval, int verticalBlockCheckInterval,
                                   RegistryEntry.Reference<Biome> registryEntry,
                                   MultiNoiseUtil.MultiNoiseSampler noiseSampler,
                                   ServerWorld world
    ) {
        int threadCount = SeederConfig.locateBiomeThreadCount;
        locateBiomeThreadPool = Executors.newFixedThreadPool(threadCount);

        locateBiomeStopFlag.set(false);
        locateBiomeFinishedCount.set(0);
        locateBiomeProgress.set(0);
        long length = (Math.floorDiv(radius, horizontalBlockCheckInterval) * 2L + 1);
        locateBiomeTotal.set(length * length);

        try {
            BiomeSource biomeSource = world.getChunkManager().getChunkGenerator().getBiomeSource();
            for (int i = 0; i < threadCount; i++) {
                locateBiomeThreadPool.submit(new LocateBiomeTask(callback, origin, radius, horizontalBlockCheckInterval, verticalBlockCheckInterval, registryEntry, noiseSampler, world, biomeSource, threadCount, i, locateBiomeStopFlag, locateBiomeProgress, locateBiomeFinishedCount));
            }
        } finally {
            locateBiomeThreadPool.shutdown();
        }
    }

    public static void stopLocateBiomeTask(SimpleCommandExceptionType NoThreadPoolException, SimpleCommandExceptionType TimeoutException) throws CommandSyntaxException {
        if (locateBiomeThreadPool == null) {
            throw NoThreadPoolException.create();
        }
        if (locateBiomeThreadPool.isTerminated()) {
            throw NoThreadPoolException.create();
        }
        locateBiomeStopFlag.set(true);
        locateBiomeThreadPool.shutdownNow();
        try {
            boolean terminated = locateBiomeThreadPool.awaitTermination(3, TimeUnit.SECONDS);
            if (!terminated) {
                throw TimeoutException.create();
            }
        } catch (InterruptedException e) {
            throw TimeoutException.create();
        }
    }

    public static void forceStopLocateBiomeTask() {
        if (locateBiomeThreadPool == null) {
            return;
        }
        if (locateBiomeThreadPool.isTerminated()) {
            return;
        }
        locateBiomeStopFlag.set(true);
        locateBiomeThreadPool.shutdownNow();
    }

    public static boolean canLocateBiomeTask() {
        if (locateBiomeThreadPool == null) {
            return true;
        }
        return locateBiomeThreadPool.isTerminated();
    }

}
