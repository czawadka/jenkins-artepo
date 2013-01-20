package org.jenkinsci.plugins.artepo.repo.svn;

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestSvnHelper extends SvnHelper {

    public SVNURL createRepository(File dst) throws SVNException {
        SVNAdminClient adminClient = clientManager.getAdminClient();
        return adminClient.doCreateRepository(dst, null, true, true);
    }

    public List<String> list(SVNURL url, SVNRevision revision, boolean recursive) throws SVNException {
        final List<String> entries = new ArrayList<String>();
        ISVNDirEntryHandler dirEntryHandler = new ISVNDirEntryHandler() {
            public void handleDirEntry(SVNDirEntry dirEntry) throws SVNException {
                String path = dirEntry.getRelativePath();
                if (path.length()>0) {
                    if (dirEntry.getKind()==SVNNodeKind.DIR)
                        path += "/";
                    entries.add(path);
                }
            }
        };

        if (revision==null)
            revision = SVNRevision.HEAD;

        SVNLogClient logClient = clientManager.getLogClient();
        logClient.doList(url, revision, revision, false, recursive, dirEntryHandler);

        return entries;
    }

}
