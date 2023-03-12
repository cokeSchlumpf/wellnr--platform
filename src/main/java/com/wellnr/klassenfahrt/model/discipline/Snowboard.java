package com.wellnr.klassenfahrt.model.discipline;

import com.wellnr.klassenfahrt.model.SkischuhAusleihe;
import com.wellnr.klassenfahrt.model.SnowboardAusleihe;
import com.wellnr.klassenfahrt.model.SnowboardBootsAusleihe;
import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nullable;

@Value
@AllArgsConstructor(staticName = "apply")
public class Snowboard implements DiszilplinKonfiguration {

    @Nullable
    SnowboardAusleihe snowboardAusleihe;

    @Nullable
    SnowboardBootsAusleihe snowboardBootsAusleihe;

    boolean helmAusleihe;

    Koennen koennen;

}
