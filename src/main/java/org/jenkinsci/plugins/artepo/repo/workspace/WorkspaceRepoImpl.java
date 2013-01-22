package org.jenkinsci.plugins.artepo.repo.workspace;

import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.jenkinsci.plugins.artepo.repo.file.FileRepoImpl;

public class WorkspaceRepoImpl extends FileRepoImpl {
    public WorkspaceRepoImpl(RepoInfoProvider infoProvider, String path) {
        super(infoProvider, path);
    }
}
