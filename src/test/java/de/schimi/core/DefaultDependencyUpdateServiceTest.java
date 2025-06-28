package de.schimi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the default dependency update service.
 */
public class DefaultDependencyUpdateServiceTest {

    @Test
    void testServiceCreatesCorrectProcessors() {
        DefaultDependencyUpdateService service = new DefaultDependencyUpdateService();
        
        var processors = service.getSupportedProcessors();
        assertNotNull(processors);
        assertEquals(2, processors.size());
        
        // Check that we have both Gradle and Maven processors
        boolean hasGradle = processors.stream()
            .anyMatch(p -> "Gradle".equals(p.getBuildSystemName()));
        boolean hasMaven = processors.stream()
            .anyMatch(p -> "Maven".equals(p.getBuildSystemName()));
            
        assertTrue(hasGradle, "Should include Gradle processor");
        assertTrue(hasMaven, "Should include Maven processor");
    }

    @Test
    void testUpdateAllBuildFilesDoesNotThrow() {
        DefaultDependencyUpdateService service = new DefaultDependencyUpdateService();
        
        // This should not throw an exception even if no build files are found
        assertDoesNotThrow(() -> service.updateAllBuildFiles());
    }
}