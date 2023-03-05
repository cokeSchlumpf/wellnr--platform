package com.wellnr.platform.common.guid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GUIDTest {

    @Test
    public void test() {
        var example_1 = "/modules/users/user[id='abc']";
        var example_2 = "/modules/school-management/course[id='abc',name='foo']/students/student[id='xyz']";

        var guid_1 = GUID.fromString(example_1);
        assertEquals(example_1, guid_1.toString());

        var guid_2 = GUID.fromString(example_2);
        assertEquals(example_2, guid_2.toString());

        assertEquals("abc", guid_2.getAttribute("course#id"));
        assertEquals("xyz", guid_2.getAttribute("student#id"));
    }

}