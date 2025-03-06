/*
 * TECH-STORE CONFIDENTIAL
 * Copyright 2024 Tech-Store Corporation All Rights Reserved.
 * The source code contained or described herein and all documents related to the
 * source code ("Material") are owned by Tech-Store Malaysia or its suppliers or
 * licensors. Title to the Material remains with Tech-Store Malaysia or its suppliers
 * and licensors. The Material contains trade secrets and proprietary and
 * confidential information of Tech-Store or its suppliers and licensors. The Material
 * is protected by worldwide copyright and trade secret laws and treaty provisions.
 * No part of the Material may be used, copied, reproduced, modified, published,
 * uploaded, posted, transmitted, distributed, or disclosed in any way without
 * Tech-Store's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other intellectual
 * property right is granted to or conferred upon you by disclosure or delivery of
 * the Materials, either expressly, by implication, inducement, estoppel or
 * otherwise. Any license under such intellectual property rights must be express
 * and approved by Tech-Store in writing.
 */

package com.mars.tnc.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum EDeviceFile {

    VERCHECK("vercheck.sh"),
    K3_AIOT("k3_aiot.sh"),
    SEED_AIOT("seed_aiot.sh"),
    K3APP("k3app"),
    CONFIG("config.json"),
    STAGING("staging_config.json"),
    INITIATION("initiation_config.json"),
    PRODUCTION("production_config.json");

    private final String key;

    public static Optional<EDeviceFile> getDeviceFile(String key) {
        return Arrays.stream(EDeviceFile.values())
                .filter(df -> df.key.equals(key))
                .findFirst();
    }
}
