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

abstract public class IntegrationTestBase extends HudsonTestCase {
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
        CreatedArtepo artepo = createArtepoCopy("main");
        project.getPublishersList().add(artepo.artepo);

        return artepo;
    }

    protected CreatedArtepo<ArtepoCopy> createArtepoCopy(String subDir) throws IOException, InterruptedException {
        // promoted artepo
        FilePath repoPath = util.createTempSubDir(getName()+"-"+subDir);
        FileRepo repo = new FileRepo(repoPath.getRemote());
        ArtepoCopy artepo = new ArtepoCopy(repo, new CopyPattern(null, null, null), null);

        return new CreatedArtepo<ArtepoCopy>(artepo, repo, repoPath);
    }

    protected CreatedArtepo<ArtepoRestore> createArtepoRestore() throws IOException, InterruptedException {
        // promoted artepo
        ArtepoRestore artepo = new ArtepoRestore(null);

        return new CreatedArtepo(artepo, null, null);
    }

    protected JobPropertyImpl createPromotionProperty(FreeStyleProject project) throws IOException, InterruptedException, Descriptor.FormException {
        JobPropertyImpl promotionProperty = project.getProperty(JobPropertyImpl.class);
        if (promotionProperty==null) {
            promotionProperty = new JobPropertyImpl(project);
            project.addProperty(promotionProperty);
        }
        return promotionProperty;
    }

    protected PromotionProcess createManualPromotion(FreeStyleProject project, String promotionName) throws IOException, InterruptedException, Descriptor.FormException {
        JobPropertyImpl promotionProperty = createPromotionProperty(project);

        PromotionProcess promotion = promotionProperty.addProcess(promotionName);
        promotion.conditions.add(new ManualCondition());

        return promotion;
    }

    protected CreatedPromotion<ArtepoCopy> createPromotionWithArtepoCopy(FreeStyleProject project, String promotionName) throws IOException, InterruptedException, Descriptor.FormException {
        PromotionProcess promotion = createManualPromotion(project, promotionName);

        CreatedArtepo<ArtepoCopy> artepo = createArtepoCopy(promotionName);
        promotion.getBuildSteps().add(artepo.artepo);

        return new CreatedPromotion(promotion, artepo);
    }

    protected CreatedPromotion<ArtepoRestore> createPromotionWithArtepoRestore(FreeStyleProject project, String promotionName) throws IOException, InterruptedException, Descriptor.FormException {
        PromotionProcess promotion = createManualPromotion(project, promotionName);

        CreatedArtepo<ArtepoRestore> artepo = createArtepoRestore();
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

    static class CreatedArtepo<T extends ArtepoBase> {
        public T artepo;
        public FileRepo repo;
        public FilePath repoPath;

        CreatedArtepo(T artepo, FileRepo repo, FilePath repoPath) {
            this.artepo = artepo;
            this.repo = repo;
            this.repoPath = repoPath;
        }
    }
    static class CreatedPromotion<T extends ArtepoBase> extends CreatedArtepo<T> {
        public PromotionProcess promotion;

        CreatedPromotion(PromotionProcess promotion, CreatedArtepo<T> createdArtepo) {
            this(promotion, createdArtepo.artepo, createdArtepo.repo, createdArtepo.repoPath);
        }
        CreatedPromotion(PromotionProcess promotion, T artepo, FileRepo repo, FilePath repoPath) {
            super(artepo, repo, repoPath);
            this.promotion = promotion;
        }
    }
}
