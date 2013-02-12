package org.jenkinsci.plugins.artepo.repo.workspace;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.jenkinsci.plugins.artepo.CopyPattern;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class WorkspaceRepoImpl extends AbstractRepoImpl {
    String workspacePath;

    public WorkspaceRepoImpl(RepoInfoProvider infoProvider, String workspacePath) {
        super(infoProvider);
        this.workspacePath = workspacePath;
    }

    public FilePath prepareSource(String buildTag) throws InterruptedException, IOException {
        return getWorkspaceFilePath();
    }

    public void copyFrom(FilePath sourcePath, List<CopyPattern> patterns, String buildTag)
            throws InterruptedException, IOException {
        FilePath destinationPath = getWorkspaceFilePath();

        ArtepoUtil.sync(destinationPath, sourcePath, patterns);
    }

    protected FilePath getWorkspaceFilePath() {
        return new FilePath(new File(this.workspacePath));
    }

    public String getWorkspacePath() {
        return workspacePath;
    }
}
