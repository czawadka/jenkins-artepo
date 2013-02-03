package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;

import java.io.IOException;

public class CreateFilesBuilder extends Builder {
    private String[] paths;

    public CreateFilesBuilder(String... paths) {
        this.paths = paths;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        FilePath basePath = build.getWorkspace();
        for (String path : paths) {
            FilePath filePath = basePath.child(path);
            filePath.getParent().mkdirs();
            filePath.write(path, "UTF-8");
        }

        return true;
    }
}
