package com.wellnr.platform.core.persistence.query.filter;

/**
 * {@link Query} and {@link Filter} can be used as synonyms.
 */
sealed public interface Filter extends Query permits And, ElemMatch, Equals, False, IsIn, Or, True {
}
