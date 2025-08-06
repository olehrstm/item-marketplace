package de.ole101.marketplace.services;

import com.google.inject.Inject;
import de.ole101.marketplace.common.configurations.MenuConfiguration;

public class MenuService {

    private final MenuConfiguration configuration;

    @Inject
    public MenuService(MenuConfiguration configuration) {
        this.configuration = configuration;
    }

    public MenuConfiguration.Menu getMenu(String menuId) {
        return this.configuration.getMenus().stream()
                .filter(menu -> menu.getId().equalsIgnoreCase(menuId))
                .findFirst()
                .orElseThrow();
    }
}