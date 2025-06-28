package de.schimi.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation for finding files in the file system.
 */
public class DefaultFileFinder implements FileFinder {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultFileFinder.class);
    
    @Override
    public List<Path> findFiles(String filePattern) {
        try {
            return Files.walk(Paths.get("."))
                .filter(p -> p.getFileName().toString().equals(filePattern))
                .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Error while searching for {} files: {}", filePattern, e.getMessage());
            return List.of();
        }
    }
}