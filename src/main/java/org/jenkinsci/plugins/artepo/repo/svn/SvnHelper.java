package org.jenkinsci.plugins.artepo.repo.svn;

import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.io.IOException;

public class SvnHelper {
    public enum WCExists {
        DIR_INVALID,
        URL_MISMATCH,
        URL_OK
    }

    private SVNClientManager clientManager;

    public SvnHelper() {
        setupSvnProtocols();
        this.clientManager = SVNClientManager.newInstance();
    }

    public void deleteMissingAddUnversioned(File wcPath) throws SVNException {
        MissingUnversionedStatusHandler missingUnversionedHandler = new MissingUnversionedStatusHandler();
        SVNStatusClient statusClient = clientManager.getStatusClient();
        statusClient.doStatus(wcPath, SVNRevision.HEAD, SVNDepth.INFINITY,
                false, false, false, false, missingUnversionedHandler, null);

        SVNWCClient wcClient = clientManager.getWCClient();
        // delete missing
        for(File missingFile : missingUnversionedHandler.getMissingFiles()) {
            wcClient.doDelete(missingFile, true, false);
        }
        // add unversioned files
        wcClient.doAdd( (File[])missingUnversionedHandler.getUnversionedFiles().toArray(),
                true, false, false, SVNDepth.INFINITY, true, false, true );
    }

    public WCExists getWCExists(File wcPath, SVNURL wcUrl) throws SVNException {
        SVNInfo info = info(wcPath);
        if (info!=null)
            return WCExists.DIR_INVALID;
        else if (info.getURL().equals(wcUrl))
            return WCExists.URL_OK;
        else
            return WCExists.URL_MISMATCH;
    }

    public SVNInfo info(SVNURL url) throws SVNException {
        SVNWCClient wcClient = clientManager.getWCClient();
        return wcClient.doInfo(url, SVNRevision.HEAD, SVNRevision.HEAD);
    }

    public SVNInfo info(File path) throws SVNException {
        SVNWCClient wcClient = clientManager.getWCClient();
        return wcClient.doInfo(path, SVNRevision.HEAD);
    }
    public void mkdir(SVNURL url) throws SVNException {
        SVNCommitClient commitClient = clientManager.getCommitClient();
        commitClient.doMkDir(new SVNURL[]{url}, "Created by "+ ArtepoUtil.PLUGIN_NAME);
    }

    public void checkout(File wcPath, SVNURL srcUrl, SVNRevision revision) throws IOException, InterruptedException, SVNException {
        clearWC(wcPath, srcUrl);

        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.doCheckout(srcUrl, wcPath, revision, revision, SVNDepth.INFINITY, true);
    }

    public void clearWC(File wcPath, SVNURL wcUrl) throws IOException, InterruptedException, SVNException {
        WCExists exists = getWCExists(wcPath, wcUrl);
        if (exists==WCExists.DIR_INVALID) {
            wcPath.mkdirs();
        } else if (exists==WCExists.URL_MISMATCH) {
            ArtepoUtil.deleteRecursive(wcPath);
            wcPath.mkdirs();
        } else { // URL_OK
            // revert just in case
            SVNWCClient wcClient = clientManager.getWCClient();
            wcClient.doRevert(new File[]{wcPath}, SVNDepth.INFINITY, null);
        }
    }

    public void commit(File wcPath, String commitMessage) throws SVNException {
        SVNCommitClient commitClient = clientManager.getCommitClient();
        commitClient.doCommit(new File[]{wcPath}, false, commitMessage, null,
                null, false, true, SVNDepth.INFINITY);
    }

    public void dispose() {
        clientManager.dispose();
    }

    public void setAuthentication(String user, String password) {
        ISVNAuthenticationManager authManager = user!=null && user.length()>0
                ? SVNWCUtil.createDefaultAuthenticationManager(user, password)
                : SVNWCUtil.createDefaultAuthenticationManager();
        clientManager.setAuthenticationManager(authManager);
    }

    public void setEventHandler(ISVNEventHandler handler) {
        clientManager.setEventHandler(handler);
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    static public void setupSvnProtocols() {
        // http and https
        DAVRepositoryFactory.setup();
        // svn
        SVNRepositoryFactoryImpl.setup();
        // file
        FSRepositoryFactory.setup();
    }
}
