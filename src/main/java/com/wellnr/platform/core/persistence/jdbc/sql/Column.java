package com.wellnr.platform.core.persistence.jdbc.sql;

import com.wellnr.platform.core.persistence.jdbc.sql.types.Type;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public class Column {

    /**
     * The name of the column.
     */
    String name;

    /**
     * The type of the column.
     */
    Type type;



}
