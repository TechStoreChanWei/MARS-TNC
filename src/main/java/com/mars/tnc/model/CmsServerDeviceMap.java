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

package com.mars.tnc.model;

import com.mars.tnc.model.base.BaseModel;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.SQLRestriction;

@Table(name = "adm_cms_server_device_map")
@Entity
@Getter
@Immutable
@SQLRestriction("is_deleted = false")
public class CmsServerDeviceMap extends BaseModel {

    @EmbeddedId
    private TncTypeDeviceTypeMapId id;
}
