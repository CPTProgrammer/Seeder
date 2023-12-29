package cn.revaria.seeder.service;

import cn.revaria.seeder.service.task.LocateBiomeTask;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.function.Predicate;

public class LocateService {

    private static LocateBiomeTask locateBiomeTask;
    private static Thread locateBiomeThread;

    public static void locateBiome(LocateBiomeTask.Callback callback, BlockPos origin, int radius,
                                   int horizontalBlockCheckInterval, int verticalBlockCheckInterval,
                                   Predicate<RegistryEntry<Biome>> predicate,
                                   MultiNoiseUtil.MultiNoiseSampler noiseSampler,
                                   ServerWorld world
    ) {
        locateBiomeTask = new LocateBiomeTask(callback, origin, radius, horizontalBlockCheckInterval, verticalBlockCheckInterval, predicate, noiseSampler, world);
        locateBiomeThread = new Thread(locateBiomeTask);
        locateBiomeThread.start();
    }

    public static void stopLocateBiomeTask() {
        if (locateBiomeTask == null) {
            return;
        }
        locateBiomeTask.stop();
    }

    public static boolean canLocateBiomeTask() {
        if (locateBiomeThread == null) {
            return true;
        }
        return !locateBiomeThread.isAlive();
    }

}
