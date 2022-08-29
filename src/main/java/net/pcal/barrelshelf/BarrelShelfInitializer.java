package net.pcal.barrelshelf;

import net.fabricmc.api.ModInitializer;
import java.io.IOException;
import java.util.List;

public class BarrelShelfInitializer implements ModInitializer {

    // ===================================================================================
    // ModInitializer implementation

    @Override
    public void onInitialize() {
        try {
            final List<ProxyBlockRule> rules = ProxyBlockConfigLoader.loadRulesFromConfig("barrelshelf");
            ProxyBlockService.getInstance().addRules(rules);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}