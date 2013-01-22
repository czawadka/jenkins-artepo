package org.jenkinsci.plugins.artepo.repo.workspace;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.kohsuke.stapler.DataBoundConstructor;

public class WorkspaceRepo extends AbstractRepo {
    @DataBoundConstructor
    public WorkspaceRepo() {
    }

    @Override
    protected AbstractRepoImpl createImpl(RepoInfoProvider infoProvider) {
        FilePath workspacePath = infoProvider.getWorkspacePath();
        return new WorkspaceRepoImpl(infoProvider, workspacePath.getRemote());
    }
}
