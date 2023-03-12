package com.wellnr.klassenfahrt.model.discipline;

import com.wellnr.klassenfahrt.model.SkischuhAusleihe;
import com.wellnr.klassenfahrt.model.SkiAusleihe;
import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nullable;

@Value
@AllArgsConstructor(staticName = "apply")
public class Ski implements DiszilplinKonfiguration {

    @Nullable
    SkiAusleihe skiAusleihe;

    @Nullable
    SkischuhAusleihe skischuhAusleihe;

    boolean helmAusleihe;

    Koennen koennen;

}
