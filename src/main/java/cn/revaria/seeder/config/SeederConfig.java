package cn.revaria.seeder.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class SeederConfig extends MidnightConfig {
    @Entry(category = "text")
    public static Comment locateBiome;

    @Entry(category = "numbers")
    public static int locateBiomeDefaultRadius = 6400;

    @Entry(category = "numbers")
    public static int locateBiomeDefaultHorizontalBlockCheckInterval = 32;

    @Entry(category = "numbers")
    public static int locateBiomeDefaultVerticalBlockCheckInterval = 64;
}
