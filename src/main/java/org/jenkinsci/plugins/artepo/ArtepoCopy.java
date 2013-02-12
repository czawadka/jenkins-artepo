package org.jenkinsci.plugins.artepo;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
        if (!isCopyAllowed(build, launcher, listener)) {
            listener.getLogger().println("Artepo copy cannot be run due to unsuccessful build");
        } else {
            Repo sourceRepo = findSourceRepo(build, launcher, listener);
            Repo destinationRepo = getDestinationRepo();

            listener.getLogger().println("Copy "+copyPattern+" artifacts from "+sourceRepo+" to "+destinationRepo);

            FilePath tempPath = createTempPath(build.getProject().getRootProject());
            String buildTag = getResolvedBuildTag(build, listener);
            RepoInfoProvider infoProvider = createRepoInfoProvider(build, listener);

            FilePath sourcePath = sourceRepo.prepareSource(infoProvider, buildTag);
            destinationRepo.copyFrom(infoProvider, sourcePath, Arrays.asList(copyPattern), buildTag);
        }

        return true;
    }
}
