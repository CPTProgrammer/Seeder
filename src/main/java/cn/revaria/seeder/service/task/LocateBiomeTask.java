package cn.revaria.seeder.service.task;

import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class LocateBiomeTask implements Runnable {

    private final BiomeSource biomeSource;
    private final BlockPos origin;
    private final int radius;
    private final int horizontalBlockCheckInterval;
    private final int verticalBlockCheckInterval;
    private final RegistryEntry.Reference<Biome> registryEntry;
    private final MultiNoiseUtil.MultiNoiseSampler noiseSampler;
    private final ServerWorld world;
    private final Callback callback;

    private final int threadCount;
    private final int threadOrdinal;

    private final AtomicBoolean stopFlag;
    private final AtomicLong progress;
    private final AtomicInteger finishedCount;

    public LocateBiomeTask(Callback outsideCallback, BlockPos origin, int radius,
                           int horizontalBlockCheckInterval, int verticalBlockCheckInterval,
                           RegistryEntry.Reference<Biome> registryEntry, MultiNoiseUtil.MultiNoiseSampler noiseSampler,
                           ServerWorld world, BiomeSource biomeSource, int threadCount, int threadOrdinal, AtomicBoolean stopFlag,
                           AtomicLong progress, AtomicInteger finishedCount
    ) {
        this.biomeSource = biomeSource;
        this.callback = outsideCallback;

        this.origin = origin;
        this.radius = radius;
        this.horizontalBlockCheckInterval = horizontalBlockCheckInterval;
        this.verticalBlockCheckInterval = verticalBlockCheckInterval;
        this.registryEntry = registryEntry;
        this.noiseSampler = noiseSampler;
        this.world = world;

        this.threadCount = threadCount;
        this.threadOrdinal = threadOrdinal; // 0 ~ threadCount - 1

        this.stopFlag = stopFlag;
        this.progress = progress;
        this.finishedCount = finishedCount;
    }

    @Override
    public void run() {
        // I don't know why using a predicate here won't match the biome (different reference?)
        Set<RegistryEntry<Biome>> set = this.biomeSource.getBiomes().stream().filter(biomeRegistryEntry ->
            ((RegistryEntry.Reference<Biome>) biomeRegistryEntry).registryKey().getValue().toString().equals(this.registryEntry.registryKey().getValue().toString())
        ).collect(Collectors.toUnmodifiableSet());
        if (set.isEmpty()) {
            if (stopFlag.get()) return;
            stopFlag.set(true);
            this.callback.run(null);
            return;
        }
        int threadHorizontalBlockCheckInterval = this.horizontalBlockCheckInterval * this.threadCount;
        int i = Math.floorDiv(radius, threadHorizontalBlockCheckInterval);
        int[] is = MathHelper.stream(origin.getY(), world.getBottomY() + 1, world.getTopY(),
            verticalBlockCheckInterval).toArray();
        for (BlockPos.Mutable mutable : BlockPos.iterateInSquare(BlockPos.ORIGIN, i, Direction.EAST,
            Direction.SOUTH)) {
            if (stopFlag.get()) return;
            int j = origin.getX() + mutable.getX() * threadHorizontalBlockCheckInterval + this.horizontalBlockCheckInterval * this.threadOrdinal;
            int k = origin.getZ() + mutable.getZ() * threadHorizontalBlockCheckInterval + this.horizontalBlockCheckInterval * this.threadOrdinal;
            int l = BiomeCoords.fromBlock(j);
            int m = BiomeCoords.fromBlock(k);
            for (int n : is) {
                if (stopFlag.get()) return;
                int o = BiomeCoords.fromBlock(n);
                RegistryEntry<Biome> registryEntry = this.biomeSource.getBiome(l, o, m, noiseSampler);
                if (!set.contains(registryEntry)) continue;
                if (stopFlag.get()) return;
                stopFlag.set(true);
                this.callback.run(Pair.of(new BlockPos(j, n, k), registryEntry));
                return;
            }
            progress.addAndGet(1);
        }
        if (stopFlag.get()) return;
        if (finishedCount.get() == threadCount - 1) {
            this.callback.run(null);
        }
        finishedCount.addAndGet(1);
        return;
    }

    public interface Callback {
        void run(Pair<BlockPos, RegistryEntry<Biome>> pair);
    }
}