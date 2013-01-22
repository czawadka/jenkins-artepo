package org.jenkinsci.plugins.artepo.repo.svn;

import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SvnHelper {
    public enum WCExists {
        DIR_INVALID,
        URL_MISMATCH,
        URL_OK
    }

    protected SVNClientManager clientManager;

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
        Collection<File> unversionedFiles = missingUnversionedHandler.getUnversionedFiles();
        if (!unversionedFiles.isEmpty()) {
            wcClient.doAdd( unversionedFiles.toArray(new File[0]),
                    true, false, false, SVNDepth.INFINITY, true, false, true );
        }
    }

    public WCExists getWCExists(File wcPath, SVNURL wcUrl) throws SVNException {
        SVNInfo info = info(wcPath);
        if (info==null)
            return WCExists.DIR_INVALID;
        else if (info.getURL().equals(wcUrl))
            return WCExists.URL_OK;
        else
            return WCExists.URL_MISMATCH;
    }

    public SVNInfo info(SVNURL url) {
        SVNWCClient wcClient = clientManager.getWCClient();
        try {
            return wcClient.doInfo(url, SVNRevision.HEAD, SVNRevision.HEAD);
        } catch (SVNException e) {
            return null;
        }
    }

    public SVNInfo info(File path) {
        SVNWCClient wcClient = clientManager.getWCClient();
        try {
            return wcClient.doInfo(path, SVNRevision.HEAD);
        } catch (SVNException e) {
            return null;
        }
    }
    public void mkdir(SVNURL url) throws SVNException {
        SVNCommitClient commitClient = clientManager.getCommitClient();
        commitClient.doMkDir(new SVNURL[]{url}, "Created by "+ ArtepoUtil.PLUGIN_NAME);
    }

    public void checkout(File wcPath, SVNURL srcUrl, SVNRevision revision) throws IOException, InterruptedException, SVNException {
        clearWC(wcPath, srcUrl);

        if (revision==null)
            revision = SVNRevision.HEAD;
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

    public List<SVNLogEntry> log(SVNURL url, SVNRevision startRevision, int limit) throws SVNException {
        ArrayList<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>(limit);
        ISVNLogEntryHandler logHandler = new LogEntryHandler(logEntries);
        SVNLogClient logClient = clientManager.getLogClient();
        logClient.doLog(url, null, SVNRevision.HEAD, startRevision, null, true, false, limit, logHandler);

        return logEntries;
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

    private static class MissingUnversionedStatusHandler implements ISVNStatusHandler {
        private Collection<File> missingFiles;
        private Collection<File> unversionedFiles;

        public MissingUnversionedStatusHandler(Collection<File> missingFiles, Collection<File> unversionedFiles) {
            this.missingFiles = missingFiles;
            this.unversionedFiles = unversionedFiles;
        }

        public MissingUnversionedStatusHandler() {
            this(new ArrayList<File>(), new ArrayList<File>());
        }

        public Collection<File> getMissingFiles() {
            return missingFiles;
        }

        public Collection<File> getUnversionedFiles() {
            return unversionedFiles;
        }

        public void handleStatus(SVNStatus status) throws SVNException {
            SVNStatusType contentStatus = status.getContentsStatus();
            File file = status.getFile();
            if (contentStatus==SVNStatusType.STATUS_MISSING || !file.exists())
                missingFiles.add(file);
            else if (contentStatus==SVNStatusType.STATUS_UNVERSIONED
                    || contentStatus==SVNStatusType.STATUS_NONE)
                unversionedFiles.add(file);
        }
    }

    private static class LogEntryHandler implements ISVNLogEntryHandler {
        private final ArrayList<SVNLogEntry> logEntries;

        public LogEntryHandler(ArrayList<SVNLogEntry> logEntries) {
            this.logEntries = logEntries;
        }

        public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
            logEntries.add(logEntry);
        }
    }
}
