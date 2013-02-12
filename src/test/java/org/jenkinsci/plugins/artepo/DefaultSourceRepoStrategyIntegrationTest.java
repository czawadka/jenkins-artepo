package org.jenkinsci.plugins.artepo;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.workspace.WorkspaceRepo;

public class DefaultSourceRepoStrategyIntegrationTest extends IntegrationTestBase {

    public void testGetSourceRepoFromPromotionArtepo() throws Exception {
        FreeStyleProject project = createProjectWithBuilder("a.txt", "b.txt");
        createMainArtepo(project);
        CreatedPromotion<ArtepoCopy> foo = createPromotionWithArtepoCopy(project, "foo");
        CreatedPromotion<ArtepoCopy> bar = createPromotionWithArtepoCopy(project, "bar");
        bar.artepo.setSourcePromotionName(foo.promotion.getName());

        // run & automatically self promote build
        FreeStyleBuild build = build(project);
        DefaultSourceRepoStrategy sourceRepoStrategy = new DefaultSourceRepoStrategy();
        Repo sourceRepo = sourceRepoStrategy.getSourceRepo(bar.artepo, build, null, createBuildListener());

        // verify copied paths after build
        assertSame(foo.artepo.getDestinationRepo(), sourceRepo);
    }

    public void testGetSourceRepoFromMainArtepo() throws Exception {
        FreeStyleProject project = createProjectWithBuilder("a.txt", "b.txt");
        CreatedArtepo<ArtepoCopy> mainArtepo = createMainArtepo(project);
        CreatedPromotion<ArtepoCopy> foo = createPromotionWithArtepoCopy(project, "foo");

        // run & automatically self promote build
        FreeStyleBuild build = build(project);
        DefaultSourceRepoStrategy sourceRepoStrategy = new DefaultSourceRepoStrategy();
        Repo sourceRepo = sourceRepoStrategy.getSourceRepo(foo.artepo, build, null, createBuildListener());

        // verify copied paths after build
        assertSame(mainArtepo.artepo.getDestinationRepo(), sourceRepo);
    }

    public void testGetSourceRepoFromWorkspace() throws Exception {
        FreeStyleProject project = createProjectWithBuilder("a.txt", "b.txt");
        CreatedArtepo mainArtepo = createMainArtepo(project);

        // run & automatically self promote build
        FreeStyleBuild build = build(project);
        DefaultSourceRepoStrategy sourceRepoStrategy = new DefaultSourceRepoStrategy();
        Repo sourceRepo = sourceRepoStrategy.getSourceRepo(mainArtepo.artepo, build, null, createBuildListener());

        // verify copied paths after build
        assertSame(WorkspaceRepo.class, sourceRepo.getClass());
    }
}
