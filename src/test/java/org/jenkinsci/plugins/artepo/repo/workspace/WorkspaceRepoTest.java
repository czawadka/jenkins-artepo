package org.jenkinsci.plugins.artepo.repo.workspace;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoTest;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class WorkspaceRepoTest extends AbstractRepoTest {

    public WorkspaceRepoTest() {
        super(WorkspaceRepo.class);
    }

    @Test
    public void testCreateImpl() throws Exception {
        final FilePath workspacePath = new FilePath(new File("."));
        RepoInfoProvider info = createRepoInfoProvider(true, null, null, workspacePath);
        WorkspaceRepo repo = new WorkspaceRepo();

        WorkspaceRepoImpl impl = (WorkspaceRepoImpl)repo.createImpl(info);

        Assert.assertEquals(workspacePath.getRemote(), impl.getWorkspacePath());
        Assert.assertSame(info, impl.getInfoProvider());
    }

    @Test
    public void testCreateImplWithSubFolder() throws Exception {
        final FilePath workspacePath = new FilePath(new File("."));
        RepoInfoProvider info = createRepoInfoProvider(true, null, null, workspacePath);
        WorkspaceRepo repo = new WorkspaceRepo("someSubFolder");

        WorkspaceRepoImpl impl = (WorkspaceRepoImpl)repo.createImpl(info);

        Assert.assertEquals(workspacePath.child("someSubFolder").getRemote(), impl.getWorkspacePath());
        Assert.assertSame(info, impl.getInfoProvider());
    }
}
