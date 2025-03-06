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

package com.mars.tnc.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfigFileRequestDTO {

    @JsonProperty("filename")
    @NotNull(message = "Filename is required")
    private String filename;

    @JsonProperty("config_file_type")
    @NotNull(message = "Config file type is required")
    private Long tncTypeId;

    @JsonProperty("firmware_version")
    @NotNull(message = "Firmware version is required")
    private String firmwareVersion;

    @JsonProperty("app_version")
    @NotNull(message = "App version is required")
    private String appVersion;

    @JsonProperty("device_name")
    @NotNull(message = "Device name is required")
    private String deviceName;

    @JsonProperty("device_model")
    @NotNull(message = "Device model is required")
    private String deviceModel;

    @JsonProperty("imei")
    @NotNull(message = "Imei is required")
    private String imei;

    @JsonProperty("client_code")
    @NotNull(message = "Client code is required")
    private String clientCode;
}
