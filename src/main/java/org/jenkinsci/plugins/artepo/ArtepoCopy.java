package org.jenkinsci.plugins.artepo;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

public class ArtepoCopy extends ArtepoBase {
    private Repo destinationRepo;
    private List<CopyPattern> patterns;

    @DataBoundConstructor
    public ArtepoCopy(Repo destinationRepo, List<CopyPattern> patterns, String sourcePromotionName) {
        super(sourcePromotionName);
        this.destinationRepo = destinationRepo;
        this.patterns = patterns;
    }

    public Repo getDestinationRepo() {
        return destinationRepo;
    }

    public void setDestinationRepo(Repo destinationRepo) {
        this.destinationRepo = destinationRepo;
    }

    public List<CopyPattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<CopyPattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        if (!isCopyAllowed(build, launcher, listener)) {
            listener.getLogger().println("Artepo backup cannot be run due to unsuccessful build");
        } else {
            Repo sourceRepo = findSourceRepo(build, launcher, listener);
            Repo destinationRepo = getDestinationRepo();

            listener.getLogger().println("Copy "+patterns+" from "+sourceRepo+" to "+destinationRepo);

            FilePath tempPath = createTempPath(build.getProject().getRootProject());
            String buildTag = getResolvedBuildTag(build, listener);
            RepoInfoProvider infoProvider = createRepoInfoProvider(build, tempPath, listener);

            FilePath sourcePath = sourceRepo.prepareSource(infoProvider, buildTag);
            destinationRepo.copyFrom(infoProvider, sourcePath, patterns, buildTag);
        }

        return true;
    }

    private boolean isCopyAllowed(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        return build.getResult().isBetterOrEqualTo(Result.UNSTABLE);
    }
}
