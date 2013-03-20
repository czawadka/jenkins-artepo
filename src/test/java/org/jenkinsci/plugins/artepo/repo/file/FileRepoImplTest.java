package org.jenkinsci.plugins.artepo.repo.file;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImplTest;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class FileRepoImplTest extends AbstractRepoImplTest {

    @Override
    protected List<String> listRealRepository(Object realRepository, int buildNumber) throws IOException, InterruptedException {
        FilePath repoDir = (FilePath)realRepository;
        FilePath buildDir = SubfolderStrategy.getFormattedBuildSubfolder(repoDir, buildNumber);
        return util.listDirPaths(buildDir);
    }

    @Override
    protected FilePath createRealRepository() throws IOException, InterruptedException {
        return util.createTempSubDir(null);
    }

    @Override
    protected Object prepareNonExistingRealRepository() throws IOException, InterruptedException {
        FilePath dir = (FilePath) createRealRepository();
        dir.deleteRecursive();
        return dir;
    }

    @Override
    protected void addRealRepositoryFiles(Object realRepository, int buildNumber, String... files)
            throws IOException, InterruptedException {

        if (files!=null && files.length>0) {
            FilePath wcPath = (FilePath)realRepository;
            wcPath = wcPath.child(String.valueOf(buildNumber));

            for (String file : files) {
                FilePath filePath = wcPath.child(file);
                filePath.getParent().mkdirs();
                filePath.write(file, "UTF-8");
            }
        }
    }

    @Override
    protected FileRepoImpl createRepoImpl(Object realRepository) throws IOException, InterruptedException {
        return createRepoImpl(realRepository.toString(), false);
    }

    protected FileRepoImpl createRepoImpl(String path, boolean createLatest) throws IOException, InterruptedException {
        RepoInfoProvider infoProvider = createInfoProvider();
        return new FileRepoImpl(infoProvider, path, SubfolderStrategy.DEFAULT);
    }

    @Test
    public void testFormatBuildSubfolderWith5Digits() throws IOException, InterruptedException {
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "a.txt");
        Object realRepository = createRealRepository();
        AbstractRepoImpl impl = createRepoImpl(realRepository);
        impl.copyFrom(src, null, 13);

        FilePath sourcePath = impl.prepareSource(13);

        assertEquals("00013", sourcePath.getName());
    }

    @Test
    public void testSupportsNon5DigitBuildSubfolder() throws IOException, InterruptedException {
        FilePath repoPath = util.createTempSubDir("repo");
        repoPath.child("13").mkdirs();
        FileRepoImpl fileRepo = createRepoImpl(repoPath.getRemote());

        FilePath src = fileRepo.prepareSource(13);

        assertEquals("13", src.getName());
    }

    @Test
    public void testCopyFromMustCopyToAllDestinationsReturnedFromSubfolderStrategy() throws IOException, InterruptedException {
        FilePath repoPath = util.createTempSubDir("repo");
        FilePath sub1Path = util.createTempSubDir("sub1");
        FilePath sub2Path = util.createTempSubDir("sub2");
        FilePath srcPath = util.createTempSubDir("src");
        util.replaceFiles(srcPath, "a.txt", "b/c.txt");
        int buildNumber = 13;
        SubfolderStrategy subfolderStrategy = Mockito.mock(SubfolderStrategy.class);
        Mockito.when(subfolderStrategy.getDestinationPaths(repoPath, buildNumber))
                .thenReturn(Arrays.asList(sub1Path, sub2Path));
        FileRepoImpl fileRepo = createRepoImpl(repoPath.getRemote());
        fileRepo.subfolderStrategy = subfolderStrategy;

        fileRepo.copyFrom(srcPath, null, buildNumber);

        Mockito.verify(subfolderStrategy, Mockito.times(1)).getDestinationPaths(repoPath, buildNumber);
        assertThat(util.listDirPaths(sub1Path), containsInAnyOrder("a.txt", "b/", "b/c.txt"));
        assertThat(util.listDirPaths(sub2Path), containsInAnyOrder("a.txt", "b/", "b/c.txt"));
    }

    @Test
    public void testPrepareSourceMustReturnFirstExistingFolderGotFromSubfolderStrategy() throws IOException, InterruptedException {
        FilePath repoPath = createRealRepository();
        FilePath sub1Path = repoPath.child("sub1");
        FilePath sub2Path = util.createTempSubDir("sub2");
        int buildNumber = 13;
        SubfolderStrategy subfolderStrategy = Mockito.mock(SubfolderStrategy.class);
        Mockito.when(subfolderStrategy.getPotentialSourcePaths(repoPath, buildNumber))
                .thenReturn(Arrays.asList(sub1Path, sub2Path));
        FileRepoImpl fileRepo = createRepoImpl(repoPath);
        fileRepo.subfolderStrategy = subfolderStrategy;

        FilePath srcPath = fileRepo.prepareSource(buildNumber);

        Mockito.verify(subfolderStrategy, Mockito.times(1)).getPotentialSourcePaths(repoPath, buildNumber);
        assertEquals(srcPath, sub2Path);
    }
}
