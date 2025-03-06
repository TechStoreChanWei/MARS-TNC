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
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TncRequestCreateDTO {

    @JsonProperty("tnc_type_id")
    @NotNull(message = "Tnc type id is required")
    private Long tncTypeId;

    @JsonProperty("device_id")
    @NotNull(message = "Device id is required")
    private Long deviceId;

    @JsonProperty("remarks")
    @NotNull(message = "Remarks is required")
    @Size(max = 255, message = "Remarks must not exceed 255 characters")
    private String remarks;
}
