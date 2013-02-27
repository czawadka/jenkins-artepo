package org.jenkinsci.plugins.artepo.repo.svn;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.RegularExpressionMatcher;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImpl;
import org.jenkinsci.plugins.artepo.repo.AbstractRepoImplTest;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.jenkinsci.plugins.artepo.ArtepoUtil.toFile;
import static org.junit.Assert.assertThat;

public class SvnRepoImplTest extends AbstractRepoImplTest {
    TestSvnHelper svnHelper;

    @Before
    public void setUp() throws IOException, InterruptedException, SVNException {
        svnHelper = new TestSvnHelper();
    }

    @Override
    protected Object createRealRepository() throws IOException, InterruptedException {
        try {
            return svnHelper.createRepository(toFile(util.createTempSubDir(null)));
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object prepareNonExistingRealRepository() throws IOException, InterruptedException {
        try {
            SVNURL url = (SVNURL)createRealRepository();
            return url.appendPath("notYetExistingFolder", true);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void addRealRepositoryFiles(Object realRepository, String buildTag, String... files)
            throws IOException, InterruptedException {
        try {
            SVNURL url = (SVNURL)realRepository;
            if (files!=null && files.length>0) {
                FilePath wcPath = util.createTempSubDir("addRealRepositoryFiles");
                svnHelper.checkout(toFile(wcPath), url, null);

                for (String file : files) {
                    FilePath filePath = wcPath.child(file);
                    filePath.getParent().mkdirs();
                    filePath.write(file, "UTF-8");
                }

                svnHelper.deleteMissingAddUnversioned(toFile(wcPath));

                String commitMessage = prepareCommitMessage(buildTag);
                svnHelper.commit(toFile(wcPath), commitMessage);
            }
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected AbstractRepoImpl createRepoImpl(Object realRepository) throws IOException, InterruptedException {
        String svnUrl = realRepository.toString();
        RepoInfoProvider infoProvider = createInfoProvider();
        return new SvnRepoImpl(infoProvider, svnUrl, null, null);
    }

    String prepareCommitMessage(String buildTag) {
        return new SvnRepoImpl(null, null, null, null).prepareCommitMessage(buildTag);
    }

    @Override
    protected List<String> listRealRepository(Object realRepository, String buildTag) throws IOException, InterruptedException {
        try {
            SVNURL url = (SVNURL)realRepository;
            SVNRevision revision = new SvnRepoImpl(createInfoProvider(),
                    url.toString(), null, null).findRevisionFromBuildTag(buildTag);
            return svnHelper.list(url, revision, true);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void svnLogContainsShortPath() throws IOException, InterruptedException, SVNException {
        // prepare repo
        Object realRepository = createRealRepositoryWithFiles("101", "a.txt");

        AbstractRepoImpl impl = createRepoImpl(realRepository);
        impl.prepareSource(null);

        String log = getLog();
        assertThat(log, RegularExpressionMatcher.matchesPattern("(?m)^update_add a.txt$"));
    }
}
