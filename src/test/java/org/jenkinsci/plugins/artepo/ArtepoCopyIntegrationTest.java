package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.jenkinsci.plugins.artepo.repo.file.FileRepoImpl;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class ArtepoCopyIntegrationTest extends IntegrationTestBase {
    FileUtil util = new FileUtil();

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        util.close();
    }

    public void testCopyDirectBuildArtifacts() throws Exception {
        FreeStyleProject project = createProjectWithBuilder("a.txt", "b.txt");
        CreatedArtepo mainArtepo = createMainArtepo(project);

        // run & automatically self promote build
        FreeStyleBuild build = build(project);

        // verify copied paths after build
        List<String> paths = util.listDirPaths(getBuildPath(mainArtepo.repoPath, build.getNumber()));
        assertThat(paths, containsInAnyOrder("a.txt", "b.txt"));
    }

    public void testCopyPromotedBuildArtifacts() throws Exception {
        FreeStyleProject project = createProjectWithBuilder("a.txt", "b.txt");
        createMainArtepo(project);
        CreatedPromotion fooPromotion = createPromotionWithArtepoCopy(project, "foo");

        // run & automatically self promote build
        FreeStyleBuild build = build(project);
        promote(build, fooPromotion.promotion.getName());

        // verify copied paths after build
        List<String> paths = util.listDirPaths(getBuildPath(fooPromotion.repoPath, build.getNumber()));
        assertThat(paths, containsInAnyOrder("a.txt", "b.txt"));
    }

    public void testCopyGeneratesLogs() throws Exception {
        FreeStyleProject project = createProjectWithBuilder("a.txt", "b.txt");
        CreatedArtepo mainArtepo = createMainArtepo(project);

        // run & automatically self promote build
        FreeStyleBuild build = build(project);

        // verify copied paths after build
        String log = build.getLog();
        assertThat(log, RegularExpressionMatcher.matchesPattern("(?mi)^Sync .* to .* using pattern .*$"));
    }

    protected FilePath getBuildPath(FilePath repoPath, int buildNumber) {
        return repoPath.child(FileRepoImpl.formatBuildNumber(buildNumber));
    }

}
