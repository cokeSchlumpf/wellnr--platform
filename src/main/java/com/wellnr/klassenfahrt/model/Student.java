package com.wellnr.klassenfahrt.model;

import lombok.AllArgsConstructor;

import java.time.Instant;

@AllArgsConstructor(staticName = "apply")
public class Student {

    String firstName;

    String lastName;

    Instant birthday;

    Gender gender;

}
