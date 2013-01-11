package org.jenkinsci.plugins.artepo.repo.file;

import hudson.Extension;
import org.jenkinsci.plugins.artepo.repo.RepoDescriptor;

@Extension
public class FileRepoDescriptor extends RepoDescriptor {

    public FileRepoDescriptor() {
        super(FileRepo.class, "file");
    }
    public String getDisplayName() {
        return "File";
    }
}
