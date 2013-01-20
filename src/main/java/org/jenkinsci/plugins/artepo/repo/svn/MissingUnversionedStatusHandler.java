package org.jenkinsci.plugins.artepo.repo.svn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class MissingUnversionedStatusHandler implements ISVNStatusHandler {
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
