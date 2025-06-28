package de.schimi;

import de.schimi.core.DefaultDependencyUpdateService;
import de.schimi.core.DependencyUpdateService;

/**
 * Main entry point for the dependency upgrade bot that supports multiple build systems.
 */
public class DependencyUpgradeBot {

    public static void main(String[] args) {
        DependencyUpdateService service = new DefaultDependencyUpdateService();
        service.updateAllBuildFiles();
    }
}