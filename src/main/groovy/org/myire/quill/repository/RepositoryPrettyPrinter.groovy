/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.repository

import org.myire.quill.common.GradlePrettyPrinter


/**
 * Pretty printer for {@code RepositorySpec}.
 */
class RepositoryPrettyPrinter extends GradlePrettyPrinter
{
    /**
     * Create a new {@code RepositoryPrettyPrinter}.
     *
     * @param pWriter The writer to delegate the printing to.
     *
     * @throws NullPointerException if {@code pWriter} is null.
     */
    RepositoryPrettyPrinter(PrintWriter pWriter)
    {
        super(pWriter)
    }


    /**
     * Pretty print a collection of repository specifications on Gradle repository closure format.
     *
     * @param pRepositories The repositories to print.
     */
    void printRepositories(Collection<RepositorySpec> pRepositories)
    {
        if (pRepositories.empty)
            return;

        printClosureStart('repositories');
        pRepositories.each { printRepository(it) }
        printClosureEnd();
    }


    /**
     * Pretty print a {@code RepositorySpec} on repository closure format.
     *
     * @param pRepository The repository.
     */
    void printRepository(RepositorySpec pRepository)
    {
        if (pRepository.credentials != null)
        {
            printClosureStart('maven');
            printKeyValue('url', quote(pRepository.url));

            printClosureStart('credentials');
            printKeyValue('username', quote(pRepository.credentials.username));
            printKeyValue('password', quote(pRepository.credentials.password));
            printClosureEnd();

            printClosureEnd();
        }
        else
            printSingleLineClosure('maven', 'url', quote(pRepository.url));
    }
}
