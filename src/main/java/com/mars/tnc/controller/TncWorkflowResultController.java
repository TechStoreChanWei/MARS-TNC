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

package com.mars.tnc.controller;

import com.mars.tnc.constants.ApiConstants;
import com.mars.tnc.dto.request.TncWorkflowResultGenerateDTO;
import com.mars.tnc.dto.response.TncWorkflowResultDTO;
import com.mars.tnc.service.TncWorkflowResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.TNC_WORKFLOW_RESULT_BASE_URL)
public class TncWorkflowResultController {

    private final TncWorkflowResultService tncWorkflowResultService;

    @PostMapping(ApiConstants.GENERATE_ENDPOINT)
    public ResponseEntity<?> generate(@Valid @RequestBody TncWorkflowResultGenerateDTO tncWorkflowResultGenerateDTO) {
        List<TncWorkflowResultDTO> tncWorkflowResultDTOs = tncWorkflowResultService.generate(tncWorkflowResultGenerateDTO);
        return new ResponseEntity<>(tncWorkflowResultDTOs, HttpStatus.OK);
    }
}
