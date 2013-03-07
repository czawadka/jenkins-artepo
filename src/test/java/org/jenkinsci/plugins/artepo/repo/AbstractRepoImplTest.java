package org.jenkinsci.plugins.artepo.repo;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.CopyPattern;
import org.jenkinsci.plugins.artepo.FileUtil;
import org.junit.After;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

abstract public class AbstractRepoImplTest {
    protected PrintStream logger;
    protected ByteArrayOutputStream loggerStream;
    protected FileUtil util = new FileUtil();

    @Test
    public void prepareSourcesCopiesFiles() throws IOException, InterruptedException, SVNException {
        // prepare repo
        Object realRepository = createRealRepositoryWithFiles("13", "a.txt");

        AbstractRepoImpl impl = createRepoImpl(realRepository);
        FilePath sourcePath = impl.prepareSource("13");

        List<String> sourcePaths = listSource(sourcePath);
        assertThat(sourcePaths, containsInAnyOrder("a.txt"));
    }

    @Test(expected = BuildTagNotFoundException.class)
    public void prepareSourcesThrowsBuildTagNotFoundException() throws IOException, InterruptedException, SVNException {
        // prepare repo
        Object realRepository = createRealRepository();

        AbstractRepoImpl impl = createRepoImpl(realRepository);
        impl.prepareSource("nonexisting");
    }

    @Test
    public void copyFromToEmptyRepository() throws IOException, InterruptedException, SVNException {
        Object realRepository = createRealRepository();
        FilePath source = util.createTempSubDir(null);
        util.replaceFiles(source, "a.txt", "b.txt");

        AbstractRepoImpl impl = createRepoImpl(realRepository);
        impl.copyFrom(source, null, "13");

        List<String> repositoryPaths = listRealRepository(realRepository, "13");
        assertThat(repositoryPaths, containsInAnyOrder("a.txt", "b.txt"));
    }

    @Test
    public void copyFromNonEmptyDeleteOldFiles() throws IOException, InterruptedException, SVNException {
        Object realRepository = createRealRepository();
        FilePath source = util.createTempSubDir(null);
        AbstractRepoImpl impl = createRepoImpl(realRepository);

        util.replaceFiles(source, "a.txt", "b.txt");
        impl.copyFrom(source, null, "13");
        util.replaceFiles(source, "c.txt");
        impl.copyFrom(source, null, "14");

        List<String> repositoryPaths = listRealRepository(realRepository, "14");
        assertThat(repositoryPaths, containsInAnyOrder("c.txt"));
    }

    @Test
    public void copyFromSubFolderChangedToDist() throws IOException, InterruptedException, SVNException {
        // case: "ups... I've commited . folder but should be commited only dist/ folder"
        Object realRepository = createRealRepository();
        FilePath source = util.createTempSubDir(null);
        AbstractRepoImpl impl = createRepoImpl(realRepository);
        util.replaceFiles(source, "build.xml", "dist/a.txt", "dist/b.txt");
        impl.copyFrom(source, null, "13");

        CopyPattern distCopyPattern = new CopyPattern("dist/", null, null);
        AbstractRepoImpl impl2 = createRepoImpl(realRepository);
        impl2.copyFrom(source, distCopyPattern, "14");

        List<String> repositoryPaths = listRealRepository(realRepository, "14");
        assertThat(repositoryPaths, containsInAnyOrder("a.txt", "b.txt"));
    }

    @Test
    public void copyFromCanCreateNonExistingPath() throws IOException, InterruptedException, SVNException {
        Object realRepository = prepareNonExistingRealRepository();
        FilePath source = util.createTempSubDir(null);
        util.replaceFiles(source, "a.txt", "b.txt");

        AbstractRepoImpl impl = createRepoImpl(realRepository);
        impl.copyFrom(source, null, "13");

        List<String> repositoryPaths = listRealRepository(realRepository, "13");
        assertThat(repositoryPaths, containsInAnyOrder("a.txt", "b.txt"));
    }

    @After
    public void tearDown() throws Exception {
        util.close();
    }

    protected List<String> listSource(FilePath source) throws IOException, InterruptedException {
        return util.listDirPaths(source);
    }

    abstract protected List<String> listRealRepository(Object realRepository, String buildTag)
            throws IOException, InterruptedException;

    protected abstract Object createRealRepository() throws IOException, InterruptedException;
    protected abstract Object prepareNonExistingRealRepository() throws IOException, InterruptedException;
    protected abstract void addRealRepositoryFiles(Object realRepository, String buildTag, String...files)
            throws IOException, InterruptedException;
    protected abstract AbstractRepoImpl createRepoImpl(Object realRepository) throws IOException, InterruptedException;

    protected Object createRealRepositoryWithFiles(String buildTag, String...files) throws IOException, InterruptedException {
        Object realRepository = createRealRepository();
        addRealRepositoryFiles(realRepository, buildTag, files);
        return realRepository;
    }

    protected RepoInfoProvider createInfoProvider() throws IOException, InterruptedException {
        return createInfoProvider(null);
    }
    protected RepoInfoProvider createInfoProvider(FilePath workspacePath) throws IOException, InterruptedException {
        final PrintStream logger = createLogger();
        final FilePath realWorkspacePath = workspacePath!=null ? workspacePath : util.createTempSubDir("workspace");
        return new RepoInfoProvider() {
            public boolean isBuildActive() {
                return true;
            }
            public FilePath getTempPath() {
                return util.getTempBaseDir();
            }
            public PrintStream getLogger() {
                return logger;
            }

            public FilePath getWorkspacePath() {
                return realWorkspacePath;
            }
        };
    }

    protected PrintStream createLogger() {
        try {
            loggerStream = new ByteArrayOutputStream();
            logger = new PrintStream(loggerStream, true, "UTF-8");
            return logger;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getLog() {
        logger.flush();
        try {
            return new String(loggerStream.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
