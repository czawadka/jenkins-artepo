package org.jenkinsci.plugins.artepo.repo.svn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;

import java.io.File;

public class TestSvnHelper extends SvnHelper {

    public SVNURL createRepository(File dst) throws SVNException {
        SVNAdminClient adminClient = clientManager.getAdminClient();
        return adminClient.doCreateRepository(dst, null, true, true);
    }

}
