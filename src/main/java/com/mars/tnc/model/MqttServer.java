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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.SQLRestriction;

@Table(name = "adm_mqtt_server")
@Entity
@Getter
@Immutable
@SQLRestriction("is_deleted = false")
public class MqttServer extends BaseModel {

    @Id
    private Long id;

    @Column(name = "host", nullable = false, insertable = false, updatable = false)
    @Size(max = 255)
    private String host;

    @Column(name = "port", nullable = false, insertable = false, updatable = false)
    private Integer port;

    @Column(name = "username", nullable = false, insertable = false, updatable = false, length = 100)
    @Size(max = 100)
    private String username;

    @Column(name = "password", nullable = false, insertable = false, updatable = false, length = 100)
    @Size(max = 100)
    private String password;

    @Column(name = "receiver_topic", nullable = false, insertable = false, updatable = false)
    @Size(max = 255)
    private String receiverTopic;

    @Column(name = "sender_topic", nullable = false, insertable = false, updatable = false)
    @Size(max = 255)
    private String senderTopic;
}
