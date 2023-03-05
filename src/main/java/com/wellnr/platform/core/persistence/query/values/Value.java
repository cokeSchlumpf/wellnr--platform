package com.wellnr.platform.core.persistence.query.values;

sealed public interface Value permits Field, ParameterReference, Select, StaticValue, Uppercase {
}
