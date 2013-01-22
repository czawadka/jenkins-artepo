package org.jenkinsci.plugins.artepo.repo;

public class BuildTagNotFoundException extends RuntimeException {
    public BuildTagNotFoundException(String buildTag, String repository) {
        super("Cannot find build tag '"+buildTag+"' in repository "+repository);
    }
}
