package org.jenkinsci.plugins.artepo.repo.file;

import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.kohsuke.stapler.DataBoundConstructor;

public class FileRepo extends AbstractRepo {
    private String path;
    private SubfolderStrategy subfolderStrategy;

    @DataBoundConstructor
    public FileRepo(String path, SubfolderStrategy subfolderStrategy) {
        this.path = path;
        this.subfolderStrategy = subfolderStrategy;

        readResolve();
    }

    public FileRepo(String path) {
        this(path, null);
    }

    public FileRepo() {
        this(null, null);
    }

    Object readResolve() {
        if (subfolderStrategy==null)
            subfolderStrategy = SubfolderStrategy.DEFAULT;
        return this;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public SubfolderStrategy getSubfolderStrategy() {
        return subfolderStrategy;
    }

    public void setSubfolderStrategy(SubfolderStrategy subfolderStrategy) {
        this.subfolderStrategy = subfolderStrategy;
    }

    @Override
    protected AbstractRepoImpl createImpl(RepoInfoProvider infoProvider) {
        return new FileRepoImpl(infoProvider, path, subfolderStrategy);
    }

    @Override
    public String toString() {
        return "file repo "+path;
    }
}
