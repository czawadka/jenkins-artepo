package org.jenkinsci.plugins.artepo.repo.file;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImplTest;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;

import java.io.IOException;
import java.util.List;

public class FileRepoImplTest extends AbstractRepoImplTest {

    @Override
    protected List<String> listRealRepository(Object realRepository, int buildNumber) throws IOException, InterruptedException {
        FilePath repoDir = (FilePath)realRepository;
        FilePath buildDir = repoDir.child(String.valueOf(buildNumber));
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
}
