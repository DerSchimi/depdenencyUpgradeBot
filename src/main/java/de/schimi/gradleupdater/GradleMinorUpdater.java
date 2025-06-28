package de.schimi.gradleupdater;

import de.schimi.dependencyupdater.DependencyUpdateBot;

/**
 * @deprecated Use DependencyUpdateBot instead for better multi-project support
 */
@Deprecated
public class GradleMinorUpdater {

    public static void main(String[] args) {
        // Delegate to the new main class for backwards compatibility
        DependencyUpdateBot.main(new String[]{"gradle"});
    }
}
