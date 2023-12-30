package cn.revaria.seeder.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class SeederConfig extends MidnightConfig {
    public static final String LOCATE_CATEGORY = "locate";

    @Comment(category = LOCATE_CATEGORY, centered = true)
    public static Comment locateBiome;

    @Entry(category = LOCATE_CATEGORY, min = 1)
    public static int locateBiomeDefaultRadius = 6400;

    @Entry(category = LOCATE_CATEGORY, min = 1)
    public static int locateBiomeDefaultHorizontalBlockCheckInterval = 32;

    @Entry(category = LOCATE_CATEGORY, min = 1, max = 256)
    public static int locateBiomeDefaultVerticalBlockCheckInterval = 64;

    @Entry(category = LOCATE_CATEGORY, min = 1, max = 128)
    public static int locateBiomeThreadCount = 4;
}
