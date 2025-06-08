package de.schimi.gradleupdater;

public class GradleMinorUpdater {

    public static void main(String[] args) {
        GradleUpdateService service = new GradleUpdateService();
        service.updateAllBuildGradleFiles();

    }
}
