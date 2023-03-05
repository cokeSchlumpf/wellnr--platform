package com.wellnr.platform.core.persistence.query;

import com.wellnr.platform.core.persistence.query.annotations.CustomQuery;
import com.wellnr.platform.core.persistence.query.annotations.Path;
import com.wellnr.platform.core.persistence.query.filter.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.wellnr.platform.core.persistence.query.Queries.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QueriesTest {

    @Test
    void fromMethodWithSingleAttribute() throws NoSuchMethodException {
        var method = TestRepo.class.getMethod("findStringByName", String.class);
        var query = Queries.fromMethod(method);

        assertEquals(match($("Name"), eq(p(0))), query);
    }

    @Test
    void fromMethodWithAnd() throws NoSuchMethodException {
        var method = TestRepo.class.getMethod("findStringByNameAndAge", String.class, int.class);
        var query = Queries.fromMethod(method);

        var expected = and(
            match($("Name"), eq(p(0))),
            match($("Age"), eq(p(1))));

        assertEquals(expected, query);
    }

    @Test
    void fromMethodWithAOr() throws NoSuchMethodException {
        var method = TestRepo.class.getMethod("findStringByNameOrAge", String.class, int.class);
        var query = Queries.fromMethod(method);

        var expected = or(
            match($("Name"), eq(p(0))),
            match($("Age"), eq(p(1))));

        assertEquals(expected, query);
    }

    @Test
    void fromMethodWithAndAndOr() throws NoSuchMethodException {
        var method = TestRepo.class.getMethod("findStringByNameOrAgeAndCity", String.class, int.class, String.class);
        var query = Queries.fromMethod(method);

        var expected = and(
            or(
                match($("Name"), eq(p(0))),
                match($("Age"), eq(p(1)))
            ),
            match($("City"), eq(p(2)))
        );

        assertEquals(expected, query);
    }

    @Test
    void fromMethodWithCustomQuery() throws NoSuchMethodException {
        var method = TestRepo.class.getMethod("findStringByCustomQuery");
        var query = Queries.fromMethod(method);

        var expected = TestRepoCustomQueries.findStringByCustomQuery();
        assertEquals(expected, query);
    }

    @Test
    void fromMethodWithPathAnnotationWithCustomName() throws NoSuchMethodException {
        var method = TestRepo.class.getMethod("findStringByCustomQueryWithOtherName");
        var query = Queries.fromMethod(method);

        var expected = TestRepoCustomQueries.customName();
        assertEquals(expected, query);
    }

    @Test
    void fromMethodWithPathAnnotation() throws NoSuchMethodException {
        var method = TestRepo.class.getMethod("findStringByAnnotatedName", String.class);
        var query = Queries.fromMethod(method);

        assertEquals(match($("properties.name"), eq(p(0))), query);
    }

    public interface TestRepo {

        List<String> findStringByName(String name);

        List<String> findStringByNameAndAge(String name, int age);

        List<String> findStringByNameOrAge(String name, int age);

        List<String> findStringByNameOrAgeAndCity(String name, int age, String city);

        List<String> findStringByAnnotatedName(
            @Path("properties.name") String name);

        @CustomQuery(TestRepoCustomQueries.class)
        List<String> findStringByCustomQuery();

        @CustomQuery(value = TestRepoCustomQueries.class, methodName = "customName")
        List<String> findStringByCustomQueryWithOtherName();

    }

    public static class TestRepoCustomQueries {

        public static Query findStringByCustomQuery() {
            return eq(v("custom"));
        }

        public static Query customName() {
            return eq(v("custom_name"));
        }

    }



}