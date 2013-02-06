package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import hudson.model.*;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.Promotion;
import hudson.plugins.promoted_builds.PromotionProcess;
import hudson.plugins.promoted_builds.conditions.ManualCondition;
import hudson.tasks.Builder;
import org.jenkinsci.plugins.artepo.repo.file.FileRepo;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class IntegrationTestBase extends HudsonTestCase {
    FileUtil util = new FileUtil();

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        util.close();
    }

    protected FreeStyleProject createProjectWithBuilder(String... files) throws IOException {
        FreeStyleProject project = createFreeStyleProject();
        // main builder
        Builder mainBuilder = new CreateFilesBuilder(files);
        project.getBuildersList().add(mainBuilder);

        return project;
    }

    protected CreatedArtepo createMainArtepo(FreeStyleProject project) throws IOException, InterruptedException {
        CreatedArtepo artepo = createArtepo("main");
        project.getPublishersList().add(artepo.artepo);

        return artepo;
    }

    protected CreatedArtepo createArtepo(String subDir) throws IOException, InterruptedException {
        // promoted artepo
        FilePath repoPath = util.createTempSubDir(getName()+"-"+subDir);
        FileRepo repo = new FileRepo(repoPath.getRemote());
        ArtepoCopy artepo = new ArtepoCopy(repo, null, null);

        return new CreatedArtepo(artepo, repo, repoPath);
    }

    protected JobPropertyImpl createPromotionProperty(FreeStyleProject project) throws IOException, InterruptedException, Descriptor.FormException {
        JobPropertyImpl promotionProperty = project.getProperty(JobPropertyImpl.class);
        if (promotionProperty==null) {
            promotionProperty = new JobPropertyImpl(project);
            project.addProperty(promotionProperty);
        }
        return promotionProperty;
    }

    protected CreatedPromotion createPromotion(FreeStyleProject project, String promotionName) throws IOException, InterruptedException, Descriptor.FormException {
        JobPropertyImpl promotionProperty = createPromotionProperty(project);

        PromotionProcess promotion = promotionProperty.addProcess(promotionName);
        promotion.conditions.add(new ManualCondition());

        CreatedArtepo artepo = createArtepo(promotionName);
        promotion.getBuildSteps().add(artepo.artepo);

        return new CreatedPromotion(promotion, artepo);
    }

    protected BuildListener createBuildListener() {
        return new StreamBuildListener(new ByteArrayOutputStream(), Charset.forName("UTF-8"));
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

    static class CreatedArtepo {
        public ArtepoCopy artepo;
        public FileRepo repo;
        public FilePath repoPath;

        CreatedArtepo(ArtepoCopy artepo, FileRepo repo, FilePath repoPath) {
            this.artepo = artepo;
            this.repo = repo;
            this.repoPath = repoPath;
        }
    }
    static class CreatedPromotion extends CreatedArtepo {
        public PromotionProcess promotion;

        CreatedPromotion(PromotionProcess promotion, CreatedArtepo createdArtepo) {
            this(promotion, createdArtepo.artepo, createdArtepo.repo, createdArtepo.repoPath);
        }
        CreatedPromotion(PromotionProcess promotion, ArtepoCopy artepo, FileRepo repo, FilePath repoPath) {
            super(artepo, repo, repoPath);
            this.promotion = promotion;
        }
    }
}
