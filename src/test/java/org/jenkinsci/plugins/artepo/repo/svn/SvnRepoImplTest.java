package org.jenkinsci.plugins.artepo.repo.svn;

import hudson.FilePath;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jenkinsci.plugins.artepo.TestBase;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
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

public class SvnRepoImplTest extends TestBase {
    TestSvnHelper svnHelper;
    PrintStream logger;
    ByteArrayOutputStream loggerStream;

    @Before
    public void setUp() throws IOException, InterruptedException, SVNException {
        svnHelper = new TestSvnHelper();
    }

    @Test
    public void prepareSourcesFileExists() throws IOException, InterruptedException, SVNException {
        // prepare repo
        SVNURL svnUrl = createRepositoryWithFiles("a.txt");

        SvnRepoImpl impl = createSvnRepoImpl(svnUrl);
        FilePath sourcePath = impl.prepareSource(null);

        List<FilePath> sourceFiles = sourcePath.list();
        assertThat(sourceFiles, containsInAnyOrder(sourcePath.child(".svn"), sourcePath.child("a.txt")));
    }

    protected SvnRepoImpl createSvnRepoImpl(SVNURL svnUrl) throws UnsupportedEncodingException {
        RepoInfoProvider infoProvider = createInfoProvider();
        SvnRepoImpl impl = new SvnRepoImpl( infoProvider, svnUrl.toString(), null, null);
        return impl;
    }

    protected RepoInfoProvider createInfoProvider() throws UnsupportedEncodingException {
        final PrintStream logger = createLogger();
        RepoInfoProvider infoProvider = new RepoInfoProvider() {
            public boolean isBuildActive() {
                return true;
            }
            public FilePath getTempPath() {
                return createTempDir();
            }
            public PrintStream getLogger() {
                return logger;
            }
        };
        return infoProvider;
    }

    protected PrintStream createLogger() throws UnsupportedEncodingException {
        loggerStream = new ByteArrayOutputStream();
        logger = new PrintStream(loggerStream, true, "UTF-8");
        return logger;
    }

//    @Test
//    public void backupAddsFile() throws IOException, InterruptedException, SVNException {
//        // prepare repo
//        SVNURL svnUrl = createRepositoryWithFiles();
//        // prepare workspace
//        String workspaceFile = "b.txt";
//        FilePath workspace = createWorkspaceWithFiles(workspaceFile);
//
//        backup(workspace, svnUrl, "13");
//
//        List<String> repoFiles = svnHelper.list(svnUrl, null, true);
//        assertThat(repoFiles, containsInAnyOrder(workspaceFile));
//    }
//
//    @Test
//    public void backupRemovesFile() throws IOException, InterruptedException, SVNException {
//        // prepare repo
//        String repoFile = "a.txt";
//        SVNURL svnUrl = createRepositoryWithFiles(repoFile);
//        // prepare workspace
//        String workspaceFile = "b.txt";
//        FilePath workspace = createWorkspaceWithFiles(workspaceFile);
//
//        backup(workspace, svnUrl, "13");
//
//        List<String> repoFiles = svnHelper.list(svnUrl, null, true);
//        assertThat(repoFiles, containsInAnyOrder(workspaceFile));
//    }
//
//    @Test
//    public void backupCreateNonExistingSubPath() throws IOException, InterruptedException, SVNException {
//        // prepare repo
//        SVNURL svnUrl = createRepositoryWithFiles();
//        svnUrl = svnUrl.appendPath("subpath", true);
//        // prepare workspace
//        String workspaceFile = "b.txt";
//        FilePath workspace = createWorkspaceWithFiles(workspaceFile);
//
//        backup(workspace, svnUrl, "13");
//
//        List<String> repoFiles = svnHelper.list(svnUrl, null, true);
//        assertThat(repoFiles, containsInAnyOrder(workspaceFile));
//    }
//
//    @Test
//    public void backupSvnUrlHasChanged() throws IOException, InterruptedException, SVNException {
//        // prepare workspace
//        String workspaceFile = "b.txt";
//        FilePath workspace = createWorkspaceWithFiles(workspaceFile);
//        // prepare repo
//        SVNURL svnUrl = createRepositoryWithFiles();
//        // backup
//        backup(workspace, svnUrl, "13");
//
//        // prepare workspace
//        workspace.child(workspaceFile).delete();
//        String workspaceFile2 = "c.txt";
//        workspace.child(workspaceFile2).write(workspaceFile2, "UTF-8");
//        // prepare repo
//        SVNURL svnUrl2 = createRepositoryWithFiles();
//        // backup
//        backup(workspace, svnUrl2, "14");
//
//        List<String> repoFiles = svnHelper.list(svnUrl2, null, true);
//        assertThat(repoFiles, containsInAnyOrder(workspaceFile2));
//    }
//
//    @Test
//    public void checkoutTaggedVersion() throws IOException, InterruptedException, SVNException {
//        SVNURL svnUrl = createRepositoryWithFiles();
//        FilePath workspace = createWorkspaceWithFiles();
//
//        // commit build 12
//        replaceFiles(workspace, "a.txt");
//        backup(workspace, svnUrl, "12");
//
//        // commit build 13
//        replaceFiles(workspace, "b.txt", "c.txt");
//        SvnRepo repo = backup(workspace, svnUrl, "13");
//
//        // back to build 4
//        FilePath checkoutPath = repo.checkout(svnHelper, null, "12");
//
//        List<FilePath> workspaceFiles = checkoutPath.list();
//        assertThat(workspaceFiles, containsInAnyOrder(checkoutPath.child("a.txt")));
//    }

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

        replaceFiles(workspace, fileNames);

        return workspace;
    }

    protected FilePath replaceFiles(FilePath dir, String... fileNames) throws IOException, InterruptedException {
        List<FilePath> paths = dir.list();
        for (FilePath path : paths) {
            if (path.isDirectory())
                path.deleteRecursive();
            else
                path.delete();
        }

        for(String fileName : fileNames) {
            dir.child(fileName).write(fileName, "UTF-8");
        }

        return dir;
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
