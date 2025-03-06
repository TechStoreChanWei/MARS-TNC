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
import com.mars.tnc.dto.request.TncWorkflowResultGenerateDTO;
import com.mars.tnc.dto.response.TncWorkflowResultDTO;
import com.mars.tnc.enums.EPayloadParameter;
import com.mars.tnc.enums.EResultStatus;
import com.mars.tnc.enums.ERuleAction;
import com.mars.tnc.exception.NotFoundException;
import com.mars.tnc.model.*;
import com.mars.tnc.repository.*;
import com.mars.tnc.service.TncWorkflowResultService;
import com.mars.tnc.utils.SnowflakeIdGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TncWorkflowResultServiceImpl implements TncWorkflowResultService {

    private final TncRequestRepository tncRequestRepository;
    private final TncWorkflowRepository tncWorkflowRepository;
    private final TncWorkflowStepRepository tncWorkflowStepRepository;
    private final TncWorkflowRuleRepository tncWorkflowRuleRepository;
    private final TncWorkflowResultRepository tncWorkflowResultRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public List<TncWorkflowResultDTO> generate(TncWorkflowResultGenerateDTO tncWorkflowResultGenerateDTO) {
        Long tncRequestId = tncWorkflowResultGenerateDTO.getTncRequestId();
        TncRequest tncRequest = tncRequestRepository.findById(tncRequestId)
                .orElseThrow(() -> new NotFoundException("Tnc request not found"));

        List<TncWorkflow> tncWorkflows = tncWorkflowRepository.findByTncTypeId(tncRequest.getTncTypeId());
        // Collect all results first
        List<TncWorkflowResult> tncWorkflowResults = tncWorkflows.stream()
                .flatMap(tncWorkflow -> generateResults(tncRequest, tncWorkflow.getId()).stream())
                .toList();
        // Delete previous results
        tncWorkflowResultRepository.deleteByTncRequestId(tncRequestId, CommonConstants.SYS_USER_ID);
        // Bulk insert to db
        if (!tncWorkflowResults.isEmpty()) {
            tncWorkflowResultRepository.saveAll(tncWorkflowResults);
            return tncWorkflowResults
                    .stream()
                    .map(this::mapperDTO)
                    .toList();
        }
        return new ArrayList<>();
    }

    private List<TncWorkflowResult> generateResults(TncRequest tncRequest, Long tncWorkflowId) {
        List<TncWorkflowStep> tncWorkflowSteps = tncWorkflowStepRepository.findByTncWorkflowId(tncWorkflowId);
        return tncWorkflowSteps.stream()
                .map(tncWorkflowStep -> generateResult(tncRequest, tncWorkflowStep))
                .collect(Collectors.toList());
    }

    private TncWorkflowResult generateResult(TncRequest tncRequest, TncWorkflowStep tncWorkflowStep) {
        List<TncWorkflowRule> tncWorkflowRules = tncWorkflowRuleRepository.findByTncWorkflowStepId(tncWorkflowStep.getId());
        EResultStatus resultStatus = EResultStatus.NONE;
        for (TncWorkflowRule tncWorkflowRule : tncWorkflowRules) {
            if (executeRule(tncRequest, tncWorkflowRule)) {
                resultStatus = EResultStatus.SUCCESS;
            } else {
                resultStatus = EResultStatus.FAIL;
                break;
            }
        }

        TncWorkflowResult tncWorkflowResult = TncWorkflowResult.builder()
                .id(SnowflakeIdGenerator.generateId())
                .tncRequestId(tncRequest.getId())
                .tncWorkflowId(tncWorkflowStep.getTncWorkflowId())
                .tncWorkflowStepId(tncWorkflowStep.getId())
                .deviceId(tncRequest.getDeviceId())
                .status(resultStatus.getKey())
                .build();
        tncWorkflowResult.setCreatedBy(CommonConstants.SYS_USER_ID);
        tncWorkflowResult.setCreatedAt(LocalDateTime.now());
        tncWorkflowResult.setUpdatedAt(LocalDateTime.now());
        return tncWorkflowResult;
    }

    public boolean executeRule(TncRequest tncRequest, TncWorkflowRule tncWorkflowRule) {
        Optional<ERuleAction> optionalRuleAction = ERuleAction.getRuleAction(tncWorkflowRule.getAction());
        if (optionalRuleAction.isEmpty()) {
            return false;
        }
        ERuleAction ruleAction = optionalRuleAction.get();

        return switch (ruleAction) {
            case PING -> executePingRule(tncRequest, tncWorkflowRule);
            case DB_QUERY -> executeDBQueryRule(tncRequest, tncWorkflowRule);
        };
    }

    private boolean executePingRule(TncRequest tncRequest, TncWorkflowRule tncWorkflowRule) {
        return false;
    }

    private boolean executeDBQueryRule(TncRequest tncRequest, TncWorkflowRule tncWorkflowRule) {
        String[] parameters = Arrays.stream(tncWorkflowRule.getRequestParameter().split(","))
                .map(String::trim)
                .toArray(String[]::new);

        String sqlQuery = tncWorkflowRule.getRequestPayload();
        for (String parameter : parameters) {
            Optional<EPayloadParameter> optionalPayloadParameter = EPayloadParameter.getPayloadParameter(parameter);
            if (optionalPayloadParameter.isEmpty()) {
                break;
            }
            EPayloadParameter payloadParameter = optionalPayloadParameter.get();

            sqlQuery = switch (payloadParameter) {
                case TNC_REQUEST_ID -> replaceRequestPayload(sqlQuery, payloadParameter.getValue(), tncRequest.getId().toString());
                case TNC_TYPE_ID -> replaceRequestPayload(sqlQuery, payloadParameter.getValue(), tncRequest.getTncTypeId().toString());
                case DEVICE_ID -> replaceRequestPayload(sqlQuery, payloadParameter.getValue(), tncRequest.getDeviceId().toString());
            };
        }

        String result = executeDynamicQuery(sqlQuery);
        return result.equals(tncWorkflowRule.getExpectedResult());
    }

    private String replaceRequestPayload(String payload, String oldValue, String newValue) {
        return payload.replace(String.format("${%s}", oldValue), newValue);
    }

    private String executeDynamicQuery(String sqlQuery) {
        try {
            Query query = entityManager.createNativeQuery(sqlQuery);
            Object result = query.getSingleResult();
            return Objects.nonNull(result) ? result.toString() : "";
        } catch (NoResultException e) {
            return ""; // No result found, return an empty string
        } catch (Exception e) {
            log.error("Error while executing dynamic query. Exception: {}", e.getMessage());
            return "";
        }
    }

    private TncWorkflowResultDTO mapperDTO(TncWorkflowResult tncWorkflowResult) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(tncWorkflowResult, TncWorkflowResultDTO.class);
    }
}
