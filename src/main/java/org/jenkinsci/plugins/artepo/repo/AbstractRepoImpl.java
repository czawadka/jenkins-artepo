package org.jenkinsci.plugins.artepo.repo;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.CopyPattern;

import java.io.IOException;
import java.util.List;

abstract public class AbstractRepoImpl {
    protected RepoInfoProvider infoProvider;

    protected AbstractRepoImpl(RepoInfoProvider infoProvider) {
        this.infoProvider = infoProvider;
    }

    abstract public FilePath prepareSource(String buildTag) throws InterruptedException, IOException, BuildTagNotFoundException;

    abstract public void copyFrom(FilePath source, List<CopyPattern> patterns, String buildTag)
            throws InterruptedException, IOException;

    public RepoInfoProvider getInfoProvider() {
        return infoProvider;
    }
}
