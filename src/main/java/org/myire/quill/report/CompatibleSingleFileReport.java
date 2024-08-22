/*
 * Copyright 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;

import org.gradle.api.model.ReplacedBy;
import org.gradle.api.reporting.SingleFileReport;


/**
 * The properties {@code enabled} and {@code destination} where removed from the {@code Report}
 * interface in Gradle 8. Implementations of that interface compiled against older versions of
 * Gradle will implement the associated methods, but when used with Gradle 8, those implemented
 * methods will not inherit the annotations from the {@code Report} interface (since they are no
 * longer present there). This will cause the build to complain about properties without
 * annotations. Extending/implementing this interface will add the appropriate annotations even when
 * running with Gradle 8.
 */
public interface CompatibleSingleFileReport extends SingleFileReport
{
    @ReplacedBy("required")
    boolean isEnabled();

    @ReplacedBy("outputLocation")
    File getDestination();
}
