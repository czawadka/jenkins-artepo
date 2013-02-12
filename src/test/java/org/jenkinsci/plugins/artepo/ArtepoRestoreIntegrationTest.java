package org.jenkinsci.plugins.artepo;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class ArtepoRestoreIntegrationTest extends IntegrationTestBase {
    FileUtil util = new FileUtil();

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        util.close();
    }

    public void testRestorePromotedBuildArtifacts() throws Exception {
        FreeStyleProject project = createProjectWithBuilder("a.txt", "b.txt");
        CreatedArtepo<ArtepoCopy> mainArtepo = createMainArtepo(project);
        CreatedPromotion<ArtepoRestore> fooPromotion = createPromotionWithArtepoRestore(project, "foo");

        // run & automatically self promote build
        FreeStyleBuild build = build(project);
        build.getWorkspace().deleteRecursive();
        build.getWorkspace().mkdirs();
        promote(build, fooPromotion.promotion.getName());

        // verify copied paths after build
        List<String> paths = util.listDirPaths(build.getWorkspace());
        assertThat(paths, containsInAnyOrder("a.txt", "b.txt"));
    }
}
