package cn.revaria.seeder.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class SeederConfig extends MidnightConfig {
    public static final String LOCATE_CATEGORY = "locate";

    @Comment(category = LOCATE_CATEGORY, centered = true)
    public static Comment locateBiome;

    @Entry(category = LOCATE_CATEGORY)
    public static int locateBiomeDefaultRadius = 6400;

    @Entry(category = LOCATE_CATEGORY)
    public static int locateBiomeDefaultHorizontalBlockCheckInterval = 32;

    @Entry(category = LOCATE_CATEGORY)
    public static int locateBiomeDefaultVerticalBlockCheckInterval = 64;
}
