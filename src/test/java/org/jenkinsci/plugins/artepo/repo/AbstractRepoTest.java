package org.jenkinsci.plugins.artepo.repo;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.BackupSource;
import org.junit.Test;
import org.mockito.Answers;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

abstract public class AbstractRepoTest {
    Class<? extends AbstractRepo> repoClass;

    AbstractRepoImpl implMock;
    AbstractRepo repo;

    protected AbstractRepoTest(Class<? extends AbstractRepo> repoClass) {
        this.repoClass = repoClass;
    }

    @Test
    public void testCalledImplPrepareSource() throws Exception {
        createRepoMock();

        String buildTag = "some build tag";
        repo.prepareSource(null, buildTag);

        verify(implMock, times(1)).prepareSource(buildTag);
    }

    @Test
    public void testCalledImplCopyFrom() throws Exception {
        createRepoMock();

        FilePath source = new FilePath(new File("."));
        List<BackupSource> patterns = new ArrayList<BackupSource>();
        String buildTag = "some build tag";
        repo.copyFrom(null, source, patterns, buildTag);

        verify(implMock, times(1)).copyFrom(source, patterns, buildTag);
    }

    protected void createImplMock() throws IOException, InterruptedException {
        implMock = mock(AbstractRepoImpl.class);
    }
    protected void createRepoMock() throws IOException, InterruptedException {
        createImplMock();

        repo = mock(repoClass);
        when(repo.createImpl(any(RepoInfoProvider.class)))
                .thenReturn(implMock);
        when(repo.prepareSource(any(RepoInfoProvider.class), anyString()))
                .thenAnswer(Answers.CALLS_REAL_METHODS.get());
        doAnswer(Answers.CALLS_REAL_METHODS.get())
                .when(repo)
                    .copyFrom(any(RepoInfoProvider.class), any(FilePath.class), anyList(), anyString());
    }

    protected RepoInfoProvider createRepoInfoProvider() {
        return createRepoInfoProvider(true, null, null, null);
    }
    protected RepoInfoProvider createRepoInfoProvider(final boolean buildActive, final FilePath tempPath,
                                                      final PrintStream logger, final FilePath workspacePath) {
        return new RepoInfoProvider() {
            public boolean isBuildActive() {
                return buildActive;
            }

            public FilePath getTempPath() {
                return tempPath;
            }

            public PrintStream getLogger() {
                return logger;
            }

            public FilePath getWorkspacePath() {
                return workspacePath;
            }
        };
    }
}