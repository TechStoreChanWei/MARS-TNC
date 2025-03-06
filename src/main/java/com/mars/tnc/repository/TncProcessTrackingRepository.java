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

package com.mars.tnc.repository;

import com.mars.tnc.model.TncProcessTracking;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TncProcessTrackingRepository extends JpaRepository<TncProcessTracking, Long> {

    @Modifying
    @Query(value = "INSERT INTO tnc_process_tracking (id, tnc_request_id, action, entity_name, status, created_by) " +
            "VALUES (:id, :tncRequestId, :action, :entityName, :status, :createdBy) " +
            "ON DUPLICATE KEY UPDATE " +
            "status = VALUES(status), updated_by = :updatedBy",
            nativeQuery = true)
    void insertOrUpdate(@Param("id") Long id,
                        @Param("tncRequestId") Long tncRequestId,
                        @Param("action") Integer action,
                        @Param("entityName") String entityName,
                        @Param("status") Integer status,
                        @Param("createdBy") Long createdBy,
                        @Param("updatedBy") Long updatedBy);
}

