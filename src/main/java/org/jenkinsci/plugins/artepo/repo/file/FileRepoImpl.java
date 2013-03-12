package org.jenkinsci.plugins.artepo.repo.file;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.CopyPattern;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.BuildNotFoundException;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;

import java.io.File;
import java.io.IOException;

public class FileRepoImpl extends AbstractRepoImpl {
    String path;

    public FileRepoImpl(RepoInfoProvider infoProvider, String path) {
        super(infoProvider);
        this.path = path;
    }

    public FilePath prepareSource(int buildNumber) throws InterruptedException, IOException {
        FilePath buildPath = new FilePath(new File(path));
        buildPath = buildPath.child(String.valueOf(buildNumber));
        if (!buildPath.exists())
            throw new BuildNotFoundException(buildNumber, path);
        return buildPath;
    }

    public void copyFrom(FilePath sourcePath, CopyPattern pattern, int buildNumber)
            throws InterruptedException, IOException {
        FilePath destinationPath = new FilePath(new File(path)).child(String.valueOf(buildNumber));
        destinationPath.mkdirs();

        sync(destinationPath, sourcePath, pattern);
    }

    public String getPath() {
        return path;
    }
}
