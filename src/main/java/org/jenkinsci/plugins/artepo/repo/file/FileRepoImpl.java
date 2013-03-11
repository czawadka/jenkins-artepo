package org.jenkinsci.plugins.artepo.repo.file;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.jenkinsci.plugins.artepo.CopyPattern;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.BuildTagNotFoundException;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileRepoImpl extends AbstractRepoImpl {
    String path;

    public FileRepoImpl(RepoInfoProvider infoProvider, String path) {
        super(infoProvider);
        this.path = path;
    }

    public FilePath prepareSource(String buildTag) throws InterruptedException, IOException {
        FilePath buildPath = new FilePath(new File(path));
        buildPath = buildPath.child(buildTag);
        if (!buildPath.exists())
            throw new BuildTagNotFoundException(buildTag, path);
        return buildPath;
    }

    public void copyFrom(FilePath sourcePath, CopyPattern pattern, String buildTag)
            throws InterruptedException, IOException {
        FilePath destinationPath = new FilePath(new File(path)).child(buildTag);
        destinationPath.mkdirs();

        ArtepoUtil.sync(destinationPath, sourcePath, pattern);
    }

    public String getPath() {
        return path;
    }
}
