package eu.europa.ted.eforms.viewer;

import java.nio.file.Path;

/**
 * Extends {@link DependencyFactory} to allow the use of SNAPSHOT versions when
 * unit testing.
 */
public class DependencyFactoryForUnitTesting extends DependencyFactory {

    public DependencyFactoryForUnitTesting(Path sdkRoot) {
        super(sdkRoot, true);
    }
}