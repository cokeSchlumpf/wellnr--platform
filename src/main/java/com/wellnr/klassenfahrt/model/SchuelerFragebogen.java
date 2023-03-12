package com.wellnr.klassenfahrt.model;

import com.wellnr.klassenfahrt.model.discipline.DiszilplinKonfiguration;
import com.wellnr.klassenfahrt.model.discipline.Disziplin;
import com.wellnr.platform.common.guid.GUID;
import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.Set;

@Value
@AllArgsConstructor(staticName = "apply")
public class SchuelerFragebogen {

    GUID guid;

    GUID schueler;

    Disziplin disziplin;

    @Nullable
    DiszilplinKonfiguration diszilplinKonfiguration;

    Set<Ernaehrungsgewohnheiten> ernaehrungsgewohnheiten;

}
