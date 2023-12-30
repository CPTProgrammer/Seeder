package cn.revaria.seeder.service;

import cn.revaria.seeder.Seeder;
import cn.revaria.seeder.config.SeederConfig;
import cn.revaria.seeder.service.task.LocateBiomeTask;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class LocateService {

    private static LocateBiomeTask locateBiomeTask;
    private static ExecutorService locateBiomeThreadPool;
    public static final AtomicBoolean locateBiomeStopFlag = new AtomicBoolean();
    public static final AtomicLong locateBiomeProgress = new AtomicLong();
    public static final AtomicLong locateBiomeTotal = new AtomicLong();

    public static void locateBiome(LocateBiomeTask.Callback callback, BlockPos origin, int radius,
                                   int horizontalBlockCheckInterval, int verticalBlockCheckInterval,
                                   Predicate<RegistryEntry<Biome>> predicate,
                                   MultiNoiseUtil.MultiNoiseSampler noiseSampler,
                                   ServerWorld world
    ) {
        // locateBiomeTask = new LocateBiomeTask(callback, origin, radius, horizontalBlockCheckInterval, verticalBlockCheckInterval, predicate, noiseSampler, world, 1, 0, locateBiomeStopFlag, locateBiomeProgress);
        // locateBiomeThread = new Thread(locateBiomeTask);
        int threadCount = SeederConfig.locateBiomeThreadCount;
        locateBiomeThreadPool = Executors.newFixedThreadPool(threadCount);

        locateBiomeStopFlag.set(false);
        locateBiomeProgress.set(0);
        long length = (Math.floorDiv(radius, horizontalBlockCheckInterval) * 2L + 1);
        locateBiomeTotal.set(length * length);
        Seeder.LOGGER.debug(String.valueOf(locateBiomeTotal.get()));

        try {
            for (int i = 0; i < threadCount; i++) {
                locateBiomeThreadPool.submit(new LocateBiomeTask(callback, origin, radius, horizontalBlockCheckInterval, verticalBlockCheckInterval, predicate, noiseSampler, world, threadCount, i, locateBiomeStopFlag, locateBiomeProgress));
            }
        } finally {
            locateBiomeThreadPool.shutdown();
        }
        // locateBiomeThread.start();
    }

    public static void stopLocateBiomeTask() {
        if (locateBiomeThreadPool == null) {
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
