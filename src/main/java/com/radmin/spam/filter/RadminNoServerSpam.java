package com.radmin.spam.filter;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadminNoServerSpam implements ModInitializer {
	public static final String MOD_ID = "radmin-no-server-spam";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("mod loaded");
	}
}
