/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import org.gradle.api.Action;
import org.gradle.util.Configurable;


/**
 * A {@code ReportingEntity} produces one or more reports encapsulated in a container type that
 * allows configuring the reports through a {@code Closure} or {@code Action}.
 *<p>
 * This interface is variant of {@code org.gradle.api.reporting.Reporting} that doesn't depend on
 * {@code org.gradle.api.reporting.ReportContainer}. See
 * <a href="https://github.com/gradle/gradle/issues/7063">https://github.com/gradle/gradle/issues/7063</a>
 * for a discussion on why external plugins shouldn't use {@code Reporting} and
 * {@code ReportContainer} (and don't miss
 * <a href="https://github.com/gradle/gradle/issues/7063#issuecomment-455796224">this comment</a>).
 *
 * @param <T>   The report container type.
 */
public interface ReportingEntity<T extends ReportSet & Configurable<T>>
{
    /**
     * Get the reports created by this entity.
     *
     * @return  The reports, never null.
     */
    T getReports();

    /**
     * Configure the reports created by this entity.
     *
     * @param pClosure  A closure that configures the reports.
     *
     * @return  The configured reports, never null.
     */
    T reports(@DelegatesTo(type="T", strategy = Closure.DELEGATE_FIRST) Closure pClosure);

    /**
     * Configure the reports created by this entity.
     *
     * @param pConfigureAction  An action that configures the reports.
     *
     * @return  The configured reports, never null.
     */
    T reports(Action<? super T> pConfigureAction);
}
