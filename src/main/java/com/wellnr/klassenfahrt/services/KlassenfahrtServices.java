package com.wellnr.klassenfahrt.services;

import com.wellnr.klassenfahrt.model.Student;
import com.wellnr.klassenfahrt.model.discipline.DiszilplinKonfiguration;
import com.wellnr.klassenfahrt.model.discipline.Disziplin;
import com.wellnr.klassenfahrt.model.units.Euro;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface KlassenfahrtServices {

    CompletionStage<Done> registerStudents(List<Student> students);

    CompletionStage<Done> disziplinAuswaehlen(GUID schueler, Disziplin disziplin);

    CompletionStage<Done> disziplinKonfigurieren(GUID schueler, DiszilplinKonfiguration konfiguration);

    CompletionStage<Done> zahlungEingegangen(GUID schueler, Euro betrag, Instant datum);

    default CompletionStage<Done> zahlungEingegangen(GUID schueler, Euro betrag) {
        return zahlungEingegangen(schueler, betrag);
    }

}
