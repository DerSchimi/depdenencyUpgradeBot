package de.schimi.dependencyupdater;

public enum ProjectType {
    GRADLE("build.gradle"),
    MAVEN("pom.xml");
    
    private final String buildFileName;
    
    ProjectType(String buildFileName) {
        this.buildFileName = buildFileName;
    }
    
    public String getBuildFileName() {
        return buildFileName;
    }
}