package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.jenkinsci.plugins.artepo.repo.workspace.WorkspaceRepo;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Collections;

public class ArtepoRestore extends ArtepoBase {

    @DataBoundConstructor
    public ArtepoRestore(String sourcePromotionName) {
        super(sourcePromotionName);

        readResolve();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        if (!isBuildSuccessful(build, launcher, listener)) {
            listener.getLogger().println("Artepo restore cannot be run due to unsuccessful build");
        } else {
            ArtepoCopy mainArtepo = ArtepoUtil.findMainArtepo(build.getProject());
            if (mainArtepo==null) {
                listener.getLogger().println("Artepo restore cannot find main artepo");
                return false;
            }
            CopyPattern mainCopyPattern = mainArtepo.getCopyPattern();
            Repo sourceRepo = findSourceRepo(build, launcher, listener);
            Repo destinationRepo = new WorkspaceRepo(mainCopyPattern.getSubFolder());

            String buildTag = getResolvedBuildTag(build, listener);
            RepoInfoProvider infoProvider = createRepoInfoProvider(
                    build.getProject().getRootProject().getName(),
                    build.getWorkspace());

            listener.getLogger().println("Restore artifacts from " + sourceRepo + " to " + destinationRepo);
            copy(build.getBuiltOn(), destinationRepo, sourceRepo, null,
                    infoProvider, buildTag);
        }

        return true;
    }

}
