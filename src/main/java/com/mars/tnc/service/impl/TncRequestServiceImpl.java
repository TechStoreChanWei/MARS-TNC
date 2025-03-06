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

import com.mars.tnc.dto.request.TncRequestCreateDTO;
import com.mars.tnc.dto.response.TncRequestDTO;
import com.mars.tnc.enums.EDeviceFile;
import com.mars.tnc.enums.EReferenceType;
import com.mars.tnc.enums.ETncType;
import com.mars.tnc.exception.ConflictException;
import com.mars.tnc.exception.InvalidArgumentException;
import com.mars.tnc.exception.NotFoundException;
import com.mars.tnc.model.Device;
import com.mars.tnc.model.TncRequest;
import com.mars.tnc.model.TncTypeDeviceMap;
import com.mars.tnc.model.TncTypeDeviceTypeMapId;
import com.mars.tnc.repository.*;
import com.mars.tnc.service.TncRequestService;
import com.mars.tnc.utils.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TncRequestServiceImpl implements TncRequestService {

    private final TncRequestRepository tncRequestRepository;
    private final TncTypeDeviceTypeMapRepository tncTypeDeviceTypeMapRepository;
    private final TncTypeDeviceMapRepository tncTypeDeviceMapRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceFirmwareFileRepository deviceFirmwareFileRepository;
    private final CmsServerDeviceMapRepository cmsServerDeviceMapRepository;
    private final MqttServerDeviceMapRepository mqttServerDeviceMapRepository;
    private final EventTemplateMapRepository eventTemplateMapRepository;
    private final ConfigurationTemplateMapRepository configurationTemplateMapRepository;

    @Override
    @Transactional
    public TncRequestDTO create(TncRequestCreateDTO tncRequestCreateDTO, Long userId) {
        // Verify tnc type is valid
        Long tncTypeId = tncRequestCreateDTO.getTncTypeId();
        ETncType targetTncType = ETncType.getTncType(tncTypeId)
                .orElseThrow(() -> new InvalidArgumentException("Invalid request's tnc type"));
        // Verify that the device exists
        Long deviceId = tncRequestCreateDTO.getDeviceId();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NotFoundException("Device not found"));
        // Retrieve device current tnc type
        TncTypeDeviceMap tncTypeDeviceMap = tncTypeDeviceMapRepository.findById(device.getId())
                .orElseThrow(() -> new NotFoundException("Device's tnc type not found"));
        // Verify device's tnc type is valid
        ETncType currentTncType = ETncType.getTncType(tncTypeDeviceMap.getTncTypeId())
                .orElseThrow(() -> new InvalidArgumentException("Invalid device's tnc type"));
        // Ensure that the target TNC type is compatible with the device
        TncTypeDeviceTypeMapId tncTypeDeviceTypeMapId = new TncTypeDeviceTypeMapId(targetTncType.getKey(), device.getId());
        if (!tncTypeDeviceTypeMapRepository.existsById(tncTypeDeviceTypeMapId)) {
            throw new ConflictException("Request's tnc type is not compatible with the device");
        }
        // Perform request validation
        if (!canProcessRequest(device, currentTncType, targetTncType)) {
            throw new ConflictException("Tnc request is not permitted");
        }
        boolean success = processRequest(currentTncType, targetTncType);
        if (!success) {
            throw new ConflictException("Error while processing tnc request");
        }

        TncRequest tncRequest = TncRequest.builder()
                .id(SnowflakeIdGenerator.generateId())
                .tncTypeId(tncTypeId)
                .deviceId(deviceId)
                .remarks(tncRequestCreateDTO.getRemarks())
                .build();
        tncRequest.setCreatedBy(userId);
        tncRequest.setCreatedAt(LocalDateTime.now());
        tncRequest.setUpdatedAt(LocalDateTime.now());
        tncRequest = tncRequestRepository.save(tncRequest);

        return mapperDTO(tncRequest);
    }

    private boolean canProcessRequest(Device device, ETncType currentTncType, ETncType targetTncType) {
        return switch (targetTncType) {
            case STAGING -> canProcessStagingRequest(device, currentTncType);
            case INITIATION -> canProcessInitiationRequest(currentTncType);
            case MAINTENANCE -> canProcessMaintenanceRequest(device, currentTncType);
            case TESTING -> canProcessTestingRequest(currentTncType);
            case NORMAL -> canProcessNormalRequest(currentTncType);
            default -> false;
        };
    }

    private boolean canProcessStagingRequest(Device device, ETncType currentTncType) {
        if (!currentTncType.equals(ETncType.SEED)) {
            return false;
        }

        return isValidDeviceInfo(device)
                && hasRequiredFirmwareFiles(device)
                && hasValidCmsServer(device)
                && hasValidMqttServer(device)
                && hasValidEventTemplate(device)
                && hasValidConfigurationTemplate(device);
    }

    private boolean canProcessInitiationRequest(ETncType currentTncType) {
        return currentTncType.equals(ETncType.STAGING)
                || currentTncType.equals(ETncType.MAINTENANCE);
    }

    private boolean canProcessMaintenanceRequest(Device device, ETncType currentTncType) {
        if (!currentTncType.equals(ETncType.INITIATION)) {
            return false;
        }

        return isValidDeviceInfo(device)
                && hasRequiredFirmwareFiles(device)
                && hasValidCmsServer(device)
                && hasValidMqttServer(device)
                && hasValidEventTemplate(device)
                && hasValidConfigurationTemplate(device);
    }

    private boolean canProcessTestingRequest(ETncType currentTncType) {
        return currentTncType.equals(ETncType.MAINTENANCE)
                || currentTncType.equals(ETncType.NORMAL);
    }

    private boolean canProcessNormalRequest(ETncType currentTncType) {
        return currentTncType.equals(ETncType.TESTING);
    }

    private boolean isValidDeviceInfo(Device device) {
        return device.getImei().trim().length() == 15
                && device.getClientCode().trim().length() == 4
                && !device.getDeviceAtmMachines().isEmpty();
    }

    private boolean hasRequiredFirmwareFiles(Device device) {
        List<String> filenames = List.of(
                EDeviceFile.VERCHECK.getKey(),
                EDeviceFile.SEED_AIOT.getKey(),
                EDeviceFile.K3_AIOT.getKey(),
                EDeviceFile.K3APP.getKey()
        );

        return deviceFirmwareFileRepository.countByDeviceFirmwareIdAndFilenameIn(
                device.getFirmwareId(), filenames) == filenames.size();
    }

    private boolean hasValidCmsServer(Device device) {
        return cmsServerDeviceMapRepository.existsByDeviceId(device.getId());
    }

    private boolean hasValidMqttServer(Device device) {
        return mqttServerDeviceMapRepository.existsByDeviceId(device.getId());
    }

    private boolean hasValidEventTemplate(Device device) {
        return eventTemplateMapRepository.existsByReferenceTypeIdAndReferenceId(
                EReferenceType.DEVICE.getKey(), device.getId());
    }

    private boolean hasValidConfigurationTemplate(Device device) {
        return configurationTemplateMapRepository.existsByReferenceTypeIdAndReferenceId(
                EReferenceType.DEVICE.getKey(), device.getId());
    }

    private boolean processRequest(ETncType currentTncType, ETncType targetTncType) {
        switch (targetTncType) {
            case STAGING:
            case TESTING:
            case NORMAL:
                return true;
            case INITIATION:
                // send initiation mqtt
                return false;
            case MAINTENANCE:
                if (currentTncType.equals(ETncType.INITIATION)) {
                    // send maintenance sms
                    return false;
                } else {
                    return true;
                }
            default:
                return false;
        }
    }

    private TncRequestDTO mapperDTO(TncRequest tncRequest) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(tncRequest, TncRequestDTO.class);
    }
}
