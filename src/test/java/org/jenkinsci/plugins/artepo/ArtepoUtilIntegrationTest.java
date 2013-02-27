package org.jenkinsci.plugins.artepo;

import hudson.maven.MavenModuleSet;
import hudson.model.FreeStyleProject;

public class ArtepoUtilIntegrationTest extends IntegrationTestBase {

    public void testFindMainArtepoFromFreeStyleProject() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        CreatedArtepo mainArtepo = createMainArtepo(project);

        ArtepoCopy foundArtepo = ArtepoUtil.findMainArtepo(project);

        assertSame(mainArtepo.artepo, foundArtepo);
    }

    public void testFindMainArtepoFromMavenProject() throws Exception {
        MavenModuleSet project = createMavenProject();
        CreatedArtepo mainArtepo = createMainArtepo(project);

        ArtepoCopy foundArtepo = ArtepoUtil.findMainArtepo(project);

        assertSame(mainArtepo.artepo, foundArtepo);
    }
}
