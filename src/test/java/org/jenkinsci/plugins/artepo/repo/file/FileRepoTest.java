package org.jenkinsci.plugins.artepo.repo.file;

import org.jenkinsci.plugins.artepo.repo.AbstractRepoTest;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.junit.Assert;
import org.junit.Test;

public class FileRepoTest extends AbstractRepoTest {

    public FileRepoTest() {
        super(FileRepo.class);
    }

    @Test
    public void testCreateImpl() throws Exception {
        String path = "path";
        RepoInfoProvider info = createRepoInfoProvider();
        FileRepo repo = new FileRepo(path);

        FileRepoImpl impl = (FileRepoImpl)repo.createImpl(info);

        Assert.assertEquals(path, impl.getPath());
        Assert.assertSame(info, impl.getInfoProvider());
    }
}
