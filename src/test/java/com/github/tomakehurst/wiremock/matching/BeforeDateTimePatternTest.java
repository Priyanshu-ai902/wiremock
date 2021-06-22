package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.DateTimeUnit;
import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import static com.github.tomakehurst.wiremock.common.DateTimeTruncation.FIRST_DAY_OF_MONTH;
import static com.github.tomakehurst.wiremock.common.DateTimeTruncation.FIRST_HOUR_OF_DAY;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BeforeDateTimePatternTest {

    @Test
    public void matchesZonedISO8601BeforeZonedLiteralDateTime() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15Z");

        assertTrue(matcher.match("2021-06-01T15:15:15Z").isExactMatch());
        assertFalse(matcher.match("2021-07-01T23:59:59Z").isExactMatch());
    }

    @Test
    public void doesNotMatchLocalISO8601BeforeZonedLiteralDateTime() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15Z");

        // Shouldn't match even if it's apparently correct as a local -> zoned comparison does not make sense
        assertFalse(matcher.match("2021-06-01T15:15:15").isExactMatch());
        assertFalse(matcher.match("2021-07-01T23:59:59").isExactMatch());
    }

    @Test
    public void matchesLocalISO8601BeforeLocalLiteralDateTime() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15");

        assertTrue(matcher.match("2021-06-01T15:15:15").isExactMatch());
        assertFalse(matcher.match("2021-07-01T23:59:59").isExactMatch());
    }

    @Test
    public void matchesZonedISO8601BeforeLocalLiteralDateTime() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15");

        assertTrue(matcher.match("2021-06-01T15:15:15Z").isExactMatch());
        assertFalse(matcher.match("2021-07-01T23:59:59Z").isExactMatch());
    }

    @Test
    public void doesNotMatchWhenActualValueUnparseable() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15");
        assertFalse(matcher.match("2021-06-01T15:15:blahsdfj123").isExactMatch());
    }

    @Test
    public void doesNotMatchWhenExpectedValueUnparseable() {
        StringValuePattern matcher = WireMock.before("2021-06-wrongstuff:15:15");
        assertFalse(matcher.match("2021-06-01T15:15:15Z").isExactMatch());
    }

    @Test
    public void returnsAReasonableDistanceWhenNoMatchForZonedExpectedZonedActual() {
        StringValuePattern matcher = WireMock.before("2021-01-01T00:00:00Z");
        assertThat(matcher.match("2071-01-01T00:00:00Z").getDistance(), is(0.5));
        assertThat(matcher.match("2121-01-01T00:00:00Z").getDistance(), is(1.0));
        assertThat(matcher.match("2022-01-01T00:00:00Z").getDistance(), is(0.01));
    }

    @Test
    public void returnsAReasonableDistanceWhenNoMatchForLocalExpectedZonedActual() {
        StringValuePattern matcher = WireMock.before("2021-01-01T00:00:00");
        assertThat(matcher.match("2071-01-01T00:00:00Z").getDistance(), is(0.5));
        assertThat(matcher.match("2121-01-01T00:00:00Z").getDistance(), is(1.0));
        assertThat(matcher.match("2022-01-01T00:00:00Z").getDistance(), is(0.01));
    }

    @Test
    public void returnsAReasonableDistanceWhenNoMatchForLocalExpectedLocalActual() {
        StringValuePattern matcher = WireMock.before("2021-01-01T00:00:00");
        assertThat(matcher.match("2071-01-01T00:00:00").getDistance(), is(0.5));
        assertThat(matcher.match("2121-01-01T00:00:00").getDistance(), is(1.0));
        assertThat(matcher.match("2022-01-01T00:00:00").getDistance(), is(0.01));
    }

    @Test
    public void matchesZonedRFC1123ActualDate() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15Z");

        assertTrue(matcher.match("Tue, 01 Jun 2021 15:16:17 GMT").isExactMatch());
        assertFalse(matcher.match("Thu, 01 Jul 2021 15:16:17 GMT").isExactMatch());
    }

    @Test
    public void matchesZonedRFC1036ActualDate() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15Z");

        assertTrue(matcher.match("Tuesday, 01-Jun-21 14:14:14 GMT").isExactMatch());
        assertFalse(matcher.match("Thursday, 01-Jul-21 15:16:17 GMT").isExactMatch());
    }

    @Test
    public void matchesZonedSingleDigitDayAsctimeActualDate() {
        StringValuePattern matcher = WireMock.before("2021-06-14T01:01:01Z");

        assertTrue(matcher.match("Tue Jun  1 01:01:01 2021").isExactMatch());
        assertFalse(matcher.match("Thu Jul  1 01:01:01 2021").isExactMatch());
    }

    @Test
    public void matchesZonedDoubleDigitDayAsctimeActualDate() {
        StringValuePattern matcher = WireMock.before("2021-06-14T01:01:01Z");

        assertTrue(matcher.match("Thu Jun 10 01:01:01 2021").isExactMatch());
        assertFalse(matcher.match("Sat Jul 10 01:01:01 2021").isExactMatch());
    }

    @Test
    public void matchesNonUTCZonedISO8601ActualDate() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15Z");

        assertTrue(matcher.match("2021-06-14T15:15:15+01:00[Europe/London]").isExactMatch());
        assertFalse(matcher.match("2021-06-14T16:15:15+01:00[Europe/London]").isExactMatch());
    }

    @Test
    public void matchesActualDateAccordingToSpecifiedFormat() {
        StringValuePattern matcher = WireMock.before("2021-06-14").actualDateTimeFormat("dd/MM/yyyy");

        assertTrue(matcher.match("01/06/2021").isExactMatch());
        assertFalse(matcher.match("01/07/2021").isExactMatch());
    }

    @Test
    public void matchesAgainstNow() {
        StringValuePattern matcher = WireMock.beforeNow();

        String right = ZonedDateTime.now().minusDays(2).toString();
        assertTrue(matcher.match(right).isExactMatch());

        String wrong = ZonedDateTime.now().plusHours(4).toString();
        assertFalse(matcher.match(wrong).isExactMatch());
    }

    @Test
    public void matchesAgainstOffsetFromNow() {
        StringValuePattern matcher = WireMock.before("now -5 days");

        String right = ZonedDateTime.now().minusDays(7).toString();
        assertTrue(matcher.match(right).isExactMatch());

        String wrong = ZonedDateTime.now().minusDays(4).toString();
        assertFalse(matcher.match(wrong).isExactMatch());
    }

    @Test
    public void truncatesActualDateToSpecifiedUnitWhenUsingLiteralBound() {
        StringValuePattern matcher = WireMock.before("15 days").truncateActual(FIRST_DAY_OF_MONTH); // Before the 15th of this month

        TemporalAdjuster truncateToMonth = TemporalAdjusters.firstDayOfMonth();
        ZonedDateTime good = ZonedDateTime.now().with(truncateToMonth).plus(14, ChronoUnit.DAYS);
        ZonedDateTime bad = ZonedDateTime.now().with(truncateToMonth).plus(16, ChronoUnit.DAYS);

        assertTrue(matcher.match(good.toString()).isExactMatch());
        assertFalse(matcher.match(bad.toString()).isExactMatch());
    }

    @Test
    public void serialisesLiteralDateTimeAndFormatFormToJson() {
        StringValuePattern matcher = WireMock.before("2021-06-01T00:00:00").actualDateTimeFormat("dd/MM/yyyy");

        assertThat(Json.write(matcher), jsonEquals("{\n" +
            "  \"before\": \"2021-06-01T00:00:00\",\n" +
            "  \"format\": \"dd/MM/yyyy\"\n" +
            "}"));
    }

    @Test
    public void serialisesOffsetWithActualTruncationFormToJson() {
        StringValuePattern matcher = WireMock.beforeNow().offset(15, DateTimeUnit.DAYS).truncateActual(FIRST_DAY_OF_MONTH);

        assertThat(Json.write(matcher), jsonEquals("{\n" +
                "  \"before\": \"now +15 days\",\n" +
                "  \"truncateActual\": \"first day of month\"\n" +
                "}"));
    }

    @Test
    public void serialisesOffsetWithExpectedAndActualTruncationFormToJson() {
        StringValuePattern matcher = WireMock.beforeNow()
                .offset(15, DateTimeUnit.DAYS)
                .truncateExpected(FIRST_HOUR_OF_DAY)
                .truncateActual(FIRST_DAY_OF_MONTH);

        assertThat(Json.write(matcher), jsonEquals("{\n" +
                "  \"before\": \"now +15 days\",\n" +
                "  \"truncateExpected\": \"first hour of day\",\n" +
                "  \"truncateActual\": \"first day of month\"\n" +
                "}"));
    }

    @Test
    public void deserialisesLiteralDateAndTimeWithFormatFromJson() {
        StringValuePattern matcher = Json.read("{\n" +
                "  \"before\": \"2021-06-15T00:00:00\",\n" +
                "  \"format\": \"dd/MM/yyyy\"\n" +
                "}", BeforeDateTimePattern.class);

        assertTrue(matcher.match("01/06/2021").isExactMatch());
        assertFalse(matcher.match("01/07/2021").isExactMatch());
    }

    @Test
    public void deserialisesPositiveOffsetAndTruncateFormFromJson() {
        StringValuePattern matcher = Json.read("{\n" +
                "  \"before\": \"15 days\",\n" +
                "  \"truncateActual\": \"first day of month\"\n" +
                "}", BeforeDateTimePattern.class);

        TemporalAdjuster truncateToMonth = TemporalAdjusters.firstDayOfMonth();
        ZonedDateTime good = ZonedDateTime.now().with(truncateToMonth).plus(14, ChronoUnit.DAYS);
        ZonedDateTime bad = ZonedDateTime.now().with(truncateToMonth).plus(16, ChronoUnit.DAYS);

        assertTrue(matcher.match(good.toString()).isExactMatch());
        assertFalse(matcher.match(bad.toString()).isExactMatch());
    }

    @Test
    public void deserialisesNegativeOffsetFormFromJson() {
        StringValuePattern matcher = Json.read("{\n" +
                "  \"before\": \"-15 days\"\n" +
                "}", BeforeDateTimePattern.class);

        ZonedDateTime good = ZonedDateTime.now().minus(16, ChronoUnit.DAYS);
        ZonedDateTime bad = ZonedDateTime.now().minus(14, ChronoUnit.DAYS);

        assertTrue(matcher.match(good.toString()).isExactMatch());
        assertFalse(matcher.match(bad.toString()).isExactMatch());
    }

}
