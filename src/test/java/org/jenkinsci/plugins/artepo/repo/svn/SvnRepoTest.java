package org.jenkinsci.plugins.artepo.repo.svn;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jenkinsci.plugins.artepo.TestBase;
import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SvnRepoTest extends TestBase {
    TestSvnHelper svnHelper;
    PrintStream logger;
    ByteArrayOutputStream loggerStream;

    @Before
    public void setUp() throws IOException, InterruptedException, SVNException {
        svnHelper = new TestSvnHelper();
    }

    @Test
    public void backupAddsFile() throws IOException, InterruptedException, SVNException {
        // prepare repo
        SVNURL svnUrl = createRepositoryWithFiles();
        // prepare workspace
        String workspaceFile = "b.txt";
        FilePath workspace = createWorkspaceWithFiles(workspaceFile);

        backup(workspace, svnUrl, "13");

        List<String> repoFiles = svnHelper.list(svnUrl, null, true);
        assertThat(repoFiles, containsInAnyOrder(workspaceFile));
    }

    @Test
    public void backupRemovesFile() throws IOException, InterruptedException, SVNException {
        // prepare repo
        String repoFile = "a.txt";
        SVNURL svnUrl = createRepositoryWithFiles(repoFile);
        // prepare workspace
        String workspaceFile = "b.txt";
        FilePath workspace = createWorkspaceWithFiles(workspaceFile);

        backup(workspace, svnUrl, "13");

        List<String> repoFiles = svnHelper.list(svnUrl, null, true);
        assertThat(repoFiles, containsInAnyOrder(workspaceFile));
    }

    @Test
    public void backupCreateNonExistingSubPath() throws IOException, InterruptedException, SVNException {
        // prepare repo
        SVNURL svnUrl = createRepositoryWithFiles();
        svnUrl = svnUrl.appendPath("subpath", true);
        // prepare workspace
        String workspaceFile = "b.txt";
        FilePath workspace = createWorkspaceWithFiles(workspaceFile);

        backup(workspace, svnUrl, "13");

        List<String> repoFiles = svnHelper.list(svnUrl, null, true);
        assertThat(repoFiles, containsInAnyOrder(workspaceFile));
    }

    @Test
    public void backupSvnUrlHasChanged() throws IOException, InterruptedException, SVNException {
        // prepare workspace
        String workspaceFile = "b.txt";
        FilePath workspace = createWorkspaceWithFiles(workspaceFile);
        // prepare repo
        SVNURL svnUrl = createRepositoryWithFiles();
        // backup
        backup(workspace, svnUrl, "13");

        // prepare workspace
        workspace.child(workspaceFile).delete();
        String workspaceFile2 = "c.txt";
        workspace.child(workspaceFile2).write(workspaceFile2, "UTF-8");
        // prepare repo
        SVNURL svnUrl2 = createRepositoryWithFiles();
        // backup
        backup(workspace, svnUrl2, "14");

        List<String> repoFiles = svnHelper.list(svnUrl2, null, true);
        assertThat(repoFiles, containsInAnyOrder(workspaceFile2));
    }

    protected SvnRepo backup(FilePath workspaceDir, SVNURL svnUrl, String buildTag) throws IOException, InterruptedException {
        SvnRepo repo = new SvnRepo(svnUrl.toString(), null, null);
        FilePath tempPath = createTempSubDir(".artepo");

        repo.backup(
                createBuildMock(workspaceDir),
                createLauncherMock(),
                createBuildListenerMock(),
                tempPath,
                workspaceDir,
                buildTag,
                null
        );

        return repo;
    }

    private AbstractBuild<?, ?> createBuildMock(FilePath workspaceDir) {
        AbstractBuild build = mock(AbstractBuild.class);
        when(build.getResult())
                .thenReturn(Result.SUCCESS);
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

    protected SVNURL createRepositoryWithFiles(String...files)
            throws InterruptedException, SVNException, IOException {

        SVNURL svnUrl = svnHelper.createRepository(toFile(createTempSubDir(null)));

        String commitMessage = "Add "+ Arrays.asList(files);
        addRepositoryFiles(svnUrl, commitMessage, files);

        return svnUrl;
    }

    protected FilePath addRepositoryFiles(SVNURL svnUrl, String commitMessage, String...files) throws IOException, InterruptedException, SVNException {
        FilePath wcPath = null;
        if (files!=null && files.length>0) {
            wcPath = createTempSubDir("wc");
            svnHelper.checkout(toFile(wcPath), svnUrl, null);

            for (String file : files) {
                FilePath filePath = wcPath.child(file);
                filePath.getParent().mkdirs();
                filePath.write(file, "UTF-8");
            }

            svnHelper.deleteMissingAddUnversioned(toFile(wcPath));
            svnHelper.commit(toFile(wcPath), commitMessage);
        }
        return wcPath;
    }

    protected FilePath createWorkspaceWithFiles(String... fileNames) throws IOException, InterruptedException {
        FilePath workspace = createTempSubDir("workspace");
        workspace.mkdirs();

        for(String fileName : fileNames) {
            workspace.child(fileName).write(fileName, "UTF-8");
        }

        return workspace;
    }
    protected FilePath createTempSubDir(String subFolder) throws IOException, InterruptedException {
        FilePath subDir = subFolder==null ? baseDir.createTempDir("svn", "") : baseDir.child(subFolder);
        subDir.mkdirs();
        return subDir;
    }

    public Matcher repoItemExists() {
        return new BaseMatcher() {
            public boolean matches(Object item) {
                return svnHelper.info((SVNURL)item)!=null;
            }

            public void describeTo(Description description) {
                description.appendText("svn item exists");
            }
        };
    }
}
