package org.jenkinsci.plugins.artepo.repo;

public class BuildNotFoundException extends RuntimeException {
    public BuildNotFoundException(int buildNumber, String repository) {
        super("Cannot find build tag '"+buildNumber+"' in repository "+repository);
    }
}
