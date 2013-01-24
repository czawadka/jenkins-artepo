package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.file.FileRepo;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class ArtepoCopyIntegrationTest extends HudsonTestCase {
    FileUtil util = new FileUtil();

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        util.close();
    }

    public void testFileRepoCopyWithoutPatterns() throws Exception {
        FilePath fileRepoPath = util.createTempSubDir(getName());
        Repo repo = new FileRepo(fileRepoPath.getRemote());
        ArtepoCopy artepoCopyPublisher = new ArtepoCopy(repo, null);

        Builder mainBuilder = new CreatePathsBuilder("a.txt");

        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(mainBuilder);
        project.getPublishersList().add(artepoCopyPublisher);

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        List<String> paths = util.listDirPaths(fileRepoPath);
        assertThat(paths, containsInAnyOrder(build.getNumber() + "/a.txt"));
    }

    static public class CreatePathsBuilder extends Builder {
        private String[] paths;

        public CreatePathsBuilder(String... paths) {
            this.paths = paths;
        }

        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
            FilePath basePath = build.getWorkspace();
            for (String path : paths) {
                FilePath filePath = basePath.child(path);
                filePath.getParent().mkdirs();
                filePath.write(path, "UTF-8");
            }

            return true;
        }
    }
}
