package com.hazelcast.certification.util;

import org.joda.time.DateTime;

/**
 * Created by rahul on 30/07/15.
 */
public class MiscTest {

    public static void main(String args[]) {
        DateTime dt = new DateTime(System.currentTimeMillis());
        dt = dt.minusMinutes(2);

        DateTime dt2 = new DateTime(dt);
        dt2 = dt2.minusMinutes(5);

        System.out.println("1 -> "+dt.toDateTime());
        System.out.println("2 -> "+dt2.toDateTime());

    }
}
