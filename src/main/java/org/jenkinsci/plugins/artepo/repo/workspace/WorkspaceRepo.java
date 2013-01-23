package org.jenkinsci.plugins.artepo.repo.workspace;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;

public class WorkspaceRepo extends AbstractRepo {

    public WorkspaceRepo() {
    }

    @Override
    protected AbstractRepoImpl createImpl(RepoInfoProvider infoProvider) {
        FilePath workspacePath = infoProvider.getWorkspacePath();
        return new WorkspaceRepoImpl(infoProvider, workspacePath.getRemote());
    }

    @Override
    public String toString() {
        return "workspace repo";
    }
}
