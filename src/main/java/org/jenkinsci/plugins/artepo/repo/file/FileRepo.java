package org.jenkinsci.plugins.artepo.repo.file;

import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.kohsuke.stapler.DataBoundConstructor;

public class FileRepo extends AbstractRepo {
    private String path;

    @DataBoundConstructor
    public FileRepo(String path) {
        this.path = path;
    }

    public FileRepo() {
        this(null);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    protected AbstractRepoImpl createImpl(RepoInfoProvider infoProvider) {
        return new FileRepoImpl(infoProvider, path);
    }
}
