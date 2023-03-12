package com.wellnr.klassenfahrt.model.units;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class Euro {

    int euro;

    int cents;

}
