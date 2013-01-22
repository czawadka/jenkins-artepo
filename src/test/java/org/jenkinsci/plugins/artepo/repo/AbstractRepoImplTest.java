package org.jenkinsci.plugins.artepo.repo;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.AbstractTest;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

abstract public class AbstractRepoImplTest extends AbstractTest {
    protected PrintStream logger;
    protected ByteArrayOutputStream loggerStream;

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
        FilePath source = createTempSubDir(null);
        replaceFiles(source, "a.txt", "b.txt");

        AbstractRepoImpl impl = createRepoImpl(realRepository);
        impl.copyFrom(source, null, "13");

        List<String> repositoryPaths = listRealRepository(realRepository, "13");
        assertThat(repositoryPaths, containsInAnyOrder("a.txt", "b.txt"));
    }

    @Test
    public void copyFromCanDeleteOldFiles() throws IOException, InterruptedException, SVNException {
        Object realRepository = createRealRepository();
        FilePath source = createTempSubDir(null);
        AbstractRepoImpl impl = createRepoImpl(realRepository);

        replaceFiles(source, "a.txt", "b.txt");
        impl.copyFrom(source, null, "13");
        replaceFiles(source, "c.txt");
        impl.copyFrom(source, null, "14");

        List<String> repositoryPaths = listRealRepository(realRepository, "14");
        assertThat(repositoryPaths, containsInAnyOrder("c.txt"));
    }

    @Test
    public void copyFromCanCreateNonExistingPath() throws IOException, InterruptedException, SVNException {
        Object realRepository = prepareNonExistingRealRepository();
        FilePath source = createTempSubDir(null);
        replaceFiles(source, "a.txt", "b.txt");

        AbstractRepoImpl impl = createRepoImpl(realRepository);
        impl.copyFrom(source, null, "13");

        List<String> repositoryPaths = listRealRepository(realRepository, "13");
        assertThat(repositoryPaths, containsInAnyOrder("a.txt", "b.txt"));
    }

    protected List<String> listSource(FilePath source) throws IOException, InterruptedException {
        return listDir(source);
    }

    protected List<String> listDir(FilePath source) throws IOException, InterruptedException {
        FilePath[] files = listDirFiles(source);
        return filesToPaths(source, files);
    }

    protected FilePath[] listDirFiles(FilePath source) throws IOException, InterruptedException {
        return source.list("**/*", null, true);
    }

    protected List<String> filesToPaths(FilePath source, FilePath[] files) throws IOException, InterruptedException {
        ArrayList<String> paths = new ArrayList<String>(files.length);
        String sourceName = source.getRemote();
        for (FilePath file : files) {
            String path = file.getRemote();
            path = path.substring(sourceName.length()+1);
            if (file.isDirectory())
                path += "/";
            paths.add(path);
        }
        return paths;
    }

    abstract protected List<String> listRealRepository(Object realRepository, String buildTag)
            throws IOException, InterruptedException;

    protected abstract Object createRealRepository() throws IOException, InterruptedException;
    protected abstract Object prepareNonExistingRealRepository() throws IOException, InterruptedException;
    protected abstract void addRealRepositoryFiles(Object realRepository, String buildTag, String...files)
            throws IOException, InterruptedException;
    protected abstract AbstractRepoImpl createRepoImpl(Object realRepository) throws UnsupportedEncodingException;

    protected Object createRealRepositoryWithFiles(String buildTag, String...files) throws IOException, InterruptedException {
        Object realRepository = createRealRepository();
        addRealRepositoryFiles(realRepository, buildTag, files);
        return realRepository;
    }

    protected FilePath replaceFiles(FilePath dir, String... newPaths) throws IOException, InterruptedException {
        List<FilePath> paths = dir.list();
        for (FilePath path : paths) {
            if (path.isDirectory())
                path.deleteRecursive();
            else
                path.delete();
        }

        for(String fileName : newPaths) {
            dir.child(fileName).write(fileName, "UTF-8");
        }

        return dir;
    }

    protected RepoInfoProvider createInfoProvider() {
        final PrintStream logger = createLogger();
        return new RepoInfoProvider() {
            public boolean isBuildActive() {
                return true;
            }
            public FilePath getTempPath() {
                return getTempBaseDir();
            }
            public PrintStream getLogger() {
                return logger;
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

}
