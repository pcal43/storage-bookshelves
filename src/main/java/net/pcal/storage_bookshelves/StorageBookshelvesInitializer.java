package net.pcal.storage_bookshelves;

import net.fabricmc.api.ModInitializer;
import java.io.IOException;
import java.util.List;

public class StorageBookshelvesInitializer implements ModInitializer {

    // ===================================================================================
    // ModInitializer implementation

    @Override
    public void onInitialize() {
        try {
            final List<ProxyBlockRule> rules = ProxyBlockConfigLoader.loadRulesFromConfig("storage-bookshelves");
            ProxyBlockService.getInstance().addRules(rules);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}