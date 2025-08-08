package de.ole101.marketplace.common.configurations;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;

import java.util.List;

@Data
@Slf4j
public class MenuConfiguration {

    private List<Menu> menus;

    @Data
    public static class Menu {

        private String id;
        private String titleKey;
        private String[] layout;
        private List<MenuItem> items;
    }


    @Data
    public static class MenuItem {

        private Material material;
        private String displayNameKey;
        private String loreKey;
        private String id;
    }
}

