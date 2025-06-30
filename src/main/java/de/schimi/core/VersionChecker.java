package de.schimi.core;

/**
 * Interface for checking if newer versions of dependencies are available.
 */
public interface VersionChecker {
    
    /**
     * Find a newer minor version for the given dependency.
     * @param group the group/organization ID
     * @param artifact the artifact ID
     * @param currentVersion the current version
     * @return newer minor version if available, null otherwise
     */
    String findNewerMinorVersion(String group, String artifact, String currentVersion);
}