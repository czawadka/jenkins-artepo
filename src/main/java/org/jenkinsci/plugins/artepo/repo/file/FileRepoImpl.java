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
        FilePath repoPath = new FilePath(new File(path));
        FilePath buildPath = findBuildPath(repoPath, buildNumber);
        if (buildPath==null)
            throw new BuildNotFoundException(buildNumber, path);
        return buildPath;
    }

    protected FilePath findBuildPath(FilePath repoPath, int buildNumber) throws IOException, InterruptedException {
        FilePath buildPath;
        String formattedBuildNumber = formatBuildNumber(buildNumber);
        buildPath = repoPath.child(formattedBuildNumber);
        if (!buildPath.exists()) {
            buildPath = repoPath.child(String.valueOf(buildNumber));
            if (!buildPath.exists()) {
                buildPath = null;
            }
        }
        return buildPath;
    }

    protected String formatBuildNumber(int buildNumber) {
        return String.format("%05d", buildNumber);
    }

    public void copyFrom(FilePath sourcePath, CopyPattern pattern, int buildNumber)
            throws InterruptedException, IOException {

        String formattedBuildNumber = formatBuildNumber(buildNumber);
        FilePath destinationPath = new FilePath(new File(path)).child(formattedBuildNumber);
        destinationPath.mkdirs();

        sync(destinationPath, sourcePath, pattern);
    }

    public String getPath() {
        return path;
    }
}
