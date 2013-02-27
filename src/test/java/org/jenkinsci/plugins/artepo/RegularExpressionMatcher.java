package org.jenkinsci.plugins.artepo;

import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class RegularExpressionMatcher extends TypeSafeMatcher {

    private final Pattern pattern;

    public RegularExpressionMatcher(String pattern) {
        this(Pattern.compile(pattern));
    }
    public RegularExpressionMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("matches regular expression ").appendValue(pattern);
    }

    @Override
    public boolean matchesSafely(Object item) {
        return pattern.matcher(item!=null ? item.toString() : "").find();
    }


    @Factory
    public static Matcher matchesPattern(Pattern pattern) {
        return new RegularExpressionMatcher(pattern);
    }
    @Factory
    public static Matcher matchesPattern(String pattern) {
        return new RegularExpressionMatcher(pattern);
    }

}