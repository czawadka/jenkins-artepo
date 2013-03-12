package org.jenkinsci.plugins.artepo.repo.file;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImplTest;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class FileRepoImplTest extends AbstractRepoImplTest {

    @Override
    protected List<String> listRealRepository(Object realRepository, int buildNumber) throws IOException, InterruptedException {
        FilePath repoDir = (FilePath)realRepository;
        FilePath buildDir = repoDir.child(FileRepoImpl.formatBuildNumber(buildNumber));
        return util.listDirPaths(buildDir);
    }

    @Override
    protected Object createRealRepository() throws IOException, InterruptedException {
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
    protected AbstractRepoImpl createRepoImpl(Object realRepository) throws IOException, InterruptedException {
        String path = realRepository.toString();
        RepoInfoProvider infoProvider = createInfoProvider();
        return new FileRepoImpl(infoProvider, path);
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
        FileRepoImpl fileRepo = new FileRepoImpl(createInfoProvider(repoPath), repoPath.getRemote());

        FilePath src = fileRepo.prepareSource(13);

        assertEquals("13", src.getName());
    }
}
