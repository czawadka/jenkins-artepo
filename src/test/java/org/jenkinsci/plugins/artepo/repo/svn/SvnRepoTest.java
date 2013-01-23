package org.jenkinsci.plugins.artepo.repo.svn;

import org.jenkinsci.plugins.artepo.repo.AbstractRepoTest;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.junit.Assert;
import org.junit.Test;

public class SvnRepoTest extends AbstractRepoTest {

    public SvnRepoTest() {
        super(SvnRepo.class);
    }

    @Test
    public void testCreateImpl() throws Exception {
        String url = "url";
        String user = "user";
        String password = "password";
        RepoInfoProvider info = createRepoInfoProvider();
        SvnRepo repo = new SvnRepo(url, user, password);

        SvnRepoImpl impl = (SvnRepoImpl)repo.createImpl(info);

        Assert.assertEquals(url, impl.getUrl());
        Assert.assertEquals(user, impl.getUser());
        Assert.assertEquals(password, impl.getPassword());
        Assert.assertSame(info, impl.getInfoProvider());
    }
}
