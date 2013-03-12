package org.jenkinsci.plugins.artepo.repo;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.jenkinsci.plugins.artepo.CopyPattern;

import java.io.IOException;
import java.util.List;

abstract public class AbstractRepoImpl {
    protected RepoInfoProvider infoProvider;

    protected AbstractRepoImpl(RepoInfoProvider infoProvider) {
        this.infoProvider = infoProvider;
    }

    abstract public FilePath prepareSource(String buildTag) throws InterruptedException, IOException, BuildTagNotFoundException;

    abstract public void copyFrom(FilePath source, CopyPattern pattern, String buildTag)
            throws InterruptedException, IOException;

    public RepoInfoProvider getInfoProvider() {
        return infoProvider;
    }

    protected void sync(FilePath dst, FilePath src, CopyPattern pattern) throws IOException, InterruptedException {
        infoProvider.getLogger().println("Sync "+src+" to "+dst+" using pattern "+pattern);
        ArtepoUtil.sync(dst, src, pattern);
    }
}
