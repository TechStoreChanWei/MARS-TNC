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

package com.mars.tnc.service.impl;

import com.mars.tnc.constants.CommonConstants;
import com.mars.tnc.enums.EProcessAction;
import com.mars.tnc.enums.EProcessStatus;
import com.mars.tnc.repository.TncProcessTrackingRepository;
import com.mars.tnc.service.TncProcessTrackingService;
import com.mars.tnc.utils.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TncProcessTrackingServiceImpl implements TncProcessTrackingService {

    private final TncProcessTrackingRepository tncProcessTrackingRepository;

    @Transactional
    public void insertOrUpdate(Long tncRequestId, EProcessAction processAction,
                               String entityName, EProcessStatus processStatus) {
        try {
            tncProcessTrackingRepository.insertOrUpdate(SnowflakeIdGenerator.generateId(),
                    tncRequestId, processAction.getKey(), entityName, processStatus.getKey(),
                    CommonConstants.SYS_USER_ID, CommonConstants.SYS_USER_ID);
        } catch (Exception e) {
            log.error("Error while inserting/updating tnc process tracking. Exception: {}", e.getMessage());
        }
    }
}
