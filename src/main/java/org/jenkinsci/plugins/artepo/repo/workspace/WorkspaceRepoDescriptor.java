package org.jenkinsci.plugins.artepo.repo.workspace;

import hudson.Extension;
import org.jenkinsci.plugins.artepo.repo.RepoDescriptor;

@Extension
public class WorkspaceRepoDescriptor extends RepoDescriptor {

    public WorkspaceRepoDescriptor() {
        super(WorkspaceRepo.class, "workspace");
    }
    public String getDisplayName() {
        return "Workspace";
    }
}
