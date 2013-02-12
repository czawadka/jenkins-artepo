package org.jenkinsci.plugins.artepo.repo.workspace;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.repo.AbstractRepo;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;

public class WorkspaceRepo extends AbstractRepo {
    private String subFolder;

    public WorkspaceRepo() {
        this(null);
    }

    public WorkspaceRepo(String subFolder) {
        this.subFolder = subFolder;
    }

    @Override
    protected AbstractRepoImpl createImpl(RepoInfoProvider infoProvider) {
        FilePath workspacePath = infoProvider.getWorkspacePath();
        if (subFolder!=null && subFolder.length()>0)
            workspacePath = workspacePath.child(subFolder);
        return new WorkspaceRepoImpl(infoProvider, workspacePath.getRemote());
    }

    @Override
    public String toString() {
        return "workspace "+(subFolder!=null && subFolder.length() > 0 ? "'"+subFolder+"'" : "")+"repo";
    }
}
