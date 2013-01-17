package org.jenkinsci.plugins.artepo.repo.svn;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import org.hamcrest.Matcher;
import org.jenkinsci.plugins.artepo.TestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SvnRepoTest extends TestBase {
    TestSvnHelper svnHelper;
    SVNURL dstRepoUrl;
    PrintStream logger;
    ByteArrayOutputStream loggerStream;

    @Before
    public void setUp() throws IOException, InterruptedException, SVNException {
        svnHelper = new TestSvnHelper();

        dstRepoUrl = svnHelper.createRepository(toFile(baseDir.child("dst")));
    }

    @Test
    public void backupCanAddFile() throws IOException, InterruptedException, SVNException {
        SVNURL svnUrl = dstRepoUrl.appendPath("someproject", true);

        String fileName = "a.txt";
        FilePath workspace = createWorkspace(fileName);

        backup(workspace, svnUrl, "13");

        SVNURL fileUrl = svnUrl.appendPath(fileName, true);
        SVNInfo info = svnHelper.info(fileUrl);
        assertNotNull(info);
        assertEquals(fileUrl, info.getURL());
    }

    protected FilePath createWorkspace(String...fileNames) throws IOException, InterruptedException {
        FilePath workspace = baseDir.child("workspace");
        workspace.mkdirs();

        for(String fileName : fileNames) {
            workspace.child(fileName).write(fileName, "UTF-8");
        }

        return workspace;
    }

    protected SvnRepo backup(FilePath workspaceDir, SVNURL svnUrl, String buildTag) throws IOException, InterruptedException {
        SvnRepo repo = new SvnRepo(svnUrl.toString(), null, null);

        repo.backup(
                createBuildMock(workspaceDir),
                createLauncherMock(),
                createBuildListenerMock(),
                buildTag,
                null
        );

        return repo;
    }

    private AbstractBuild<?, ?> createBuildMock(FilePath workspaceDir) {
        AbstractBuild build = mock(AbstractBuild.class);
        when(build.getResult())
                .thenReturn(Result.SUCCESS);
        when(build.getWorkspace())
                .thenReturn(workspaceDir);

        return build;
    }

    private Launcher createLauncherMock() {
        return null;
    }

    private BuildListener createBuildListenerMock() throws UnsupportedEncodingException {
        loggerStream = new ByteArrayOutputStream();
        logger = new PrintStream(loggerStream, true, "UTF-8");

        BuildListener buildListener = mock(BuildListener.class);
        when(buildListener.getLogger())
                .thenReturn(logger);

        return buildListener;
    }

    protected String getLog() throws UnsupportedEncodingException {
        logger.flush();
        return loggerStream.toString("UTF-8");
    }

}
