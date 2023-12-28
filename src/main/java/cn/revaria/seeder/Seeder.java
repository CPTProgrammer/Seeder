package cn.revaria.seeder;

import cn.revaria.seeder.command.Command;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Seeder implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("seeder");

	@Override
	public void onInitialize() {
		Command.registerAll();
	}
}