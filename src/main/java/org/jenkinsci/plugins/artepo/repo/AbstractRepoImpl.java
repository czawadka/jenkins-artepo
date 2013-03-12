package org.jenkinsci.plugins.artepo.repo;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.jenkinsci.plugins.artepo.CopyPattern;

import java.io.IOException;

abstract public class AbstractRepoImpl {
    protected RepoInfoProvider infoProvider;

    protected AbstractRepoImpl(RepoInfoProvider infoProvider) {
        this.infoProvider = infoProvider;
    }

    abstract public FilePath prepareSource(int buildNumber) throws InterruptedException, IOException, BuildNotFoundException;

    abstract public void copyFrom(FilePath source, CopyPattern pattern, int buildNumber)
            throws InterruptedException, IOException;

    public RepoInfoProvider getInfoProvider() {
        return infoProvider;
    }

    protected void sync(FilePath dst, FilePath src, CopyPattern pattern) throws IOException, InterruptedException {
        infoProvider.getLogger().println("Sync "+src+" to "+dst+" using pattern "+pattern);
        ArtepoUtil.sync(dst, src, pattern);
    }
}
