package cn.revaria.seeder;

import cn.revaria.seeder.command.Command;
import cn.revaria.seeder.config.SeederConfig;
import cn.revaria.seeder.service.LocateService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Seeder implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("seeder");

    @Override
    public void onInitialize() {
        Command.registerAll();
        SeederConfig.init("seeder", SeederConfig.class);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LocateService.forceStopLocateBiomeTask();
        });
    }
}