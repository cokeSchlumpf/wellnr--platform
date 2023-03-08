/**
 * This package contains support classes to provide simple ways to persist data in various databases.
 * The framework supports creation of collection-oriented repositories to persist state of Java objects.
 *
 * Packages
 * --------
 * **query** contains generic value classes to describe queries against a collection of entities. A simple querly
 * language DSL so to say.
 *
 * **inmemory** contains an engine to execute queries against Java objects as well as an InMemoryRepository(-Factory)
 * to dynamically create an in-memory implementation for a repository interface.
 *
 * Usage
 * -----
 * To use the query language use the static methods from {@link com.wellnr.platform.core.persistence.query.Queries}.
 */
package com.wellnr.platform.core.persistence;