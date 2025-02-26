/*
 *  TECH-STORE CONFIDENTIAL
 *  Copyright 2024 Tech-Store Corporation All Rights Reserved.
 *  The source code contained or described herein and all documents related to the
 *  source code ("Material") are owned by Tech-Store Malaysia or its suppliers or
 *  licensors. Title to the Material remains with Tech-Store Malaysia or its suppliers
 *  and licensors. The Material contains trade secrets and proprietary and
 *  confidential information of Tech-Store or its suppliers and licensors. The Material
 *  is protected by worldwide copyright and trade secret laws and treaty provisions.
 *  No part of the Material may be used, copied, reproduced, modified, published,
 *  uploaded, posted, transmitted, distributed, or disclosed in any way without
 *  Tech-Store's prior express written permission.
 *
 *  No license under any patent, copyright, trade secret or other intellectual
 *  property right is granted to or conferred upon you by disclosure or delivery of
 *  the Materials, either expressly, by implication, inducement, estoppel or
 *  otherwise. Any license under such intellectual property rights must be express
 *  and approved by Tech-Store in writing.
 */

package com.mars.tnc.utils;

public class SnowflakeIdGenerator {
    private static final long EPOCH = 1609459200000L; // Custom epoch (e.g., 2021-01-01)
    private static final long MACHINE_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;

    private static long machineId = 1; // Default machine ID, adjust if needed
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    // Synchronized static method to generate unique IDs
    public static synchronized long generateId() {
        long timestamp = currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards.");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitUntilNextMillis(timestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }

    // Helper method to handle millisecond waiting
    private static long waitUntilNextMillis(long timestamp) {
        while (timestamp == lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    // Get the current time in milliseconds
    private static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    // Static method to set the machine ID
    public static void setMachineId(long id) {
        if (id > MAX_MACHINE_ID || id < 0) {
            throw new IllegalArgumentException(String.format("Machine ID can't be greater than %d or less than 0", MAX_MACHINE_ID));
        }
        machineId = id;
    }

    public static void main(String[] args) {
        // Example usage
        long uniqueId1 = SnowflakeIdGenerator.generateId();
        System.out.println("Generated Snowflake ID 1: " + uniqueId1);

        long uniqueId2 = SnowflakeIdGenerator.generateId();
        System.out.println("Generated Snowflake ID 2: " + uniqueId2);
    }
}
