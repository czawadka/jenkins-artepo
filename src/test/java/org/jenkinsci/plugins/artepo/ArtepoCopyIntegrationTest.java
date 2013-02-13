package org.jenkinsci.plugins.artepo;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

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
        List<String> paths = util.listDirPaths(mainArtepo.repoPath.child(String.valueOf(build.getNumber())));
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
        List<String> paths = util.listDirPaths(fooPromotion.repoPath.child(String.valueOf(build.getNumber())));
        assertThat(paths, containsInAnyOrder("a.txt", "b.txt"));
    }
}
