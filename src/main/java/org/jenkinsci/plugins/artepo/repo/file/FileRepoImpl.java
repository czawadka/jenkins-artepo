package org.jenkinsci.plugins.artepo.repo.file;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.CopyPattern;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.BuildNotFoundException;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileRepoImpl extends AbstractRepoImpl {
    String path;
    SubfolderStrategy subfolderStrategy;

    public FileRepoImpl(RepoInfoProvider infoProvider, String path, SubfolderStrategy subfolderStrategy) {
        super(infoProvider);
        this.path = path;
        this.subfolderStrategy = subfolderStrategy;
    }

    public FilePath prepareSource(int buildNumber) throws InterruptedException, IOException {
        FilePath buildPath = findSourceBuildPath(getRepoPath(), buildNumber);
        if (buildPath==null)
            throw new BuildNotFoundException(buildNumber, path);
        return buildPath;
    }

    protected FilePath findSourceBuildPath(FilePath repoPath, int buildNumber) throws IOException, InterruptedException {
        FilePath buildPath = null;
        for(FilePath potentialBuildPath : subfolderStrategy.getPotentialSourcePaths(repoPath, buildNumber)) {
            if (potentialBuildPath.exists()) {
                buildPath = potentialBuildPath;
                break;
            }
        }
        return buildPath;
    }

    public void copyFrom(FilePath sourcePath, CopyPattern pattern, int buildNumber)
            throws InterruptedException, IOException {

        List<FilePath> destinationPaths = subfolderStrategy.getDestinationPaths(getRepoPath(), buildNumber);

        for (FilePath destinationPath : destinationPaths) {
            destinationPath.mkdirs();
            sync(destinationPath, sourcePath, pattern);
        }
    }

    protected FilePath getRepoPath() {
        return new FilePath(new File(path));
    }

    public String getPath() {
        return path;
    }
}
