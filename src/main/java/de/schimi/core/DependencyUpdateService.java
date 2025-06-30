package de.schimi.core;

import java.util.List;

/**
 * Main service interface for updating dependencies across different build systems.
 */
public interface DependencyUpdateService {
    
    /**
     * Update all supported build files in the current directory and subdirectories.
     */
    void updateAllBuildFiles();
    
    /**
     * Get list of supported build file processors.
     * @return list of build file processors
     */
    List<BuildFileProcessor> getSupportedProcessors();
}