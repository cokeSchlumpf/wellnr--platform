package com.wellnr.platform.core.persistence.jdbc.sql;

import com.wellnr.platform.core.persistence.jdbc.sql.types.Type;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor(staticName = "apply")
public class Table {

    /**
     * The name of the table.
     */
    String name;

    /**
     * The list of columns for the table.
     */
    List<Column> columns;



}
