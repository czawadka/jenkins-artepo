package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.remoting.Callable;
import hudson.util.IOException2;
import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Arrays;

public class ArtepoCopy extends ArtepoBase {
    private Repo destinationRepo;
    private CopyPattern copyPattern;

    @DataBoundConstructor
    public ArtepoCopy(Repo destinationRepo, CopyPattern copyPattern, String sourcePromotionName) {
        super(sourcePromotionName);
        this.destinationRepo = destinationRepo;
        this.copyPattern = copyPattern;
    }

    public Repo getDestinationRepo() {
        return destinationRepo;
    }

    public void setDestinationRepo(Repo destinationRepo) {
        this.destinationRepo = destinationRepo;
    }

    public CopyPattern getCopyPattern() {
        return copyPattern;
    }

    public void setCopyPattern(CopyPattern copyPattern) {
        this.copyPattern = copyPattern;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        if (!isBuildSuccessful(build, launcher, listener)) {
            listener.getLogger().println("Artepo copy cannot be run due to unsuccessful build");
        } else {
            Repo sourceRepo = findSourceRepo(build, launcher, listener);
            Repo destinationRepo = getDestinationRepo();

            listener.getLogger().println("Copy from "+sourceRepo+" to "+destinationRepo+" using pattern "+copyPattern);
            copy(build, listener, destinationRepo, sourceRepo, copyPattern);
        }

        return true;
    }
}
