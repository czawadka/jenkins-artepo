package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import hudson.model.Descriptor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterValue;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.Promotion;
import hudson.plugins.promoted_builds.PromotionProcess;
import hudson.plugins.promoted_builds.conditions.ManualCondition;
import hudson.tasks.Builder;
import org.jenkinsci.plugins.artepo.repo.file.FileRepo;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class ArtepoIntegrationTest extends HudsonTestCase {
    FileUtil util = new FileUtil();

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        util.close();
    }

    public void testCopyDirectBuildArtifacts() throws Exception {
        FreeStyleProject project = createProjectWithBuilder("a.txt", "b.txt");
        FilePath mainPath = createMainRepo(project);

        // run & automatically self promote build
        FreeStyleBuild build = build(project);

        // verify copied paths after build
        List<String> paths = util.listDirPaths(mainPath.child(String.valueOf(build.getNumber())));
        assertThat(paths, containsInAnyOrder("a.txt", "b.txt"));
    }

    public void testCopyPromotedBuildArtifacts() throws Exception {
        FreeStyleProject project = createProjectWithBuilder("a.txt", "b.txt");
        createMainRepo(project);
        FilePath promotedPath = createPromotionRepo(project, "foo");

        // run & automatically self promote build
        FreeStyleBuild build = build(project);
        promote(build, "foo");

        // verify copied paths after build
        List<String> paths = util.listDirPaths(promotedPath.child(String.valueOf(build.getNumber())));
        assertThat(paths, containsInAnyOrder("a.txt", "b.txt"));
    }

    protected FreeStyleProject createProjectWithBuilder(String... files) throws IOException {
        FreeStyleProject project = createFreeStyleProject();
        // main builder
        Builder mainBuilder = new CreateFilesBuilder(files);
        project.getBuildersList().add(mainBuilder);

        return project;
    }

    protected FilePath createMainRepo(FreeStyleProject project) throws IOException, InterruptedException {
        FilePath mainPath = util.createTempSubDir(getName()+"-"+"main");
        ArtepoCopy mainArtepo = new ArtepoCopy(new FileRepo(mainPath.getRemote()), null);
        project.getPublishersList().add(mainArtepo);

        return mainPath;
    }

    protected FilePath createPromotionRepo(FreeStyleProject project, String promotionName) throws IOException, InterruptedException, Descriptor.FormException {
        JobPropertyImpl promotionProperty = new JobPropertyImpl(project);
        project.addProperty(promotionProperty);
        PromotionProcess promotionProcess = promotionProperty.addProcess(promotionName);
        promotionProcess.conditions.add(new ManualCondition());
        // promoted artepo
        FilePath promotedPath = util.createTempSubDir(getName()+"-"+promotionName);
        ArtepoCopy promotedArtepo = new ArtepoCopy(new FileRepo(promotedPath.getRemote()), null);
        promotionProcess.getBuildSteps().add(promotedArtepo);

        return promotedPath;
    }

    protected FreeStyleBuild build(FreeStyleProject project) throws ExecutionException, InterruptedException, IOException {
        // run & automatically self promote build
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        return build;
    }

    protected Promotion promote(FreeStyleBuild build, String promotionName) throws ExecutionException, InterruptedException, IOException {
        // add approval to build
        build.addAction(new ManualCondition.ManualApproval(promotionName, Collections.<ParameterValue>emptyList()));
        build.save();
        // check for promotion
        JobPropertyImpl promotionProperty = build.getProject().getProperty(JobPropertyImpl.class);
        PromotionProcess promotionProcess = promotionProperty.getItem(promotionName);
        Promotion promotion = promotionProcess.considerPromotion2(build).get();
        return promotion;
    }
}
