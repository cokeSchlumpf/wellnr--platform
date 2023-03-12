package com.wellnr.platform.core.persistence.jdbc.sql.types;

public sealed interface Type permits Bool, Decimal, Memo, Numeric, Text {
}
