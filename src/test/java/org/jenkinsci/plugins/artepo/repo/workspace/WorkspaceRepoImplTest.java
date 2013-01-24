package org.jenkinsci.plugins.artepo.repo.workspace;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImplTest;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WorkspaceRepoImplTest extends AbstractRepoImplTest {

    @Test
    @Ignore
    @Override
    public void prepareSourcesThrowsBuildTagNotFoundException() throws IOException, InterruptedException, SVNException {
        // intentionally empty, don't want to run test from super class
    }

    @Test
    public void prepareSourcesIgnoresBuildTag() throws IOException, InterruptedException, SVNException {
        // prepare repo
        Object realRepository = createRealRepository();

        AbstractRepoImpl impl = createRepoImpl(realRepository);
        FilePath source = impl.prepareSource("nonexisting");

        assertEquals(realRepository, source);
    }

    @Override
    protected List<String> listRealRepository(Object realRepository, String buildTag) throws IOException, InterruptedException {
        FilePath workspaceDir = (FilePath)realRepository;
        return util.listDirPaths(workspaceDir);
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
    protected void addRealRepositoryFiles(Object realRepository, String buildTag, String... files)
            throws IOException, InterruptedException {

        if (files!=null && files.length>0) {
            FilePath workspacePath = (FilePath)realRepository;

            for (String file : files) {
                FilePath filePath = workspacePath.child(file);
                filePath.getParent().mkdirs();
                filePath.write(file, "UTF-8");
            }
        }
    }

    @Override
    protected AbstractRepoImpl createRepoImpl(Object realRepository) throws IOException, InterruptedException {
        String path = realRepository.toString();
        RepoInfoProvider infoProvider = createInfoProvider();
        return new WorkspaceRepoImpl(infoProvider, path);
    }
}
