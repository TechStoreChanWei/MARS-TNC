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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mars.tnc.enums.EDeviceFile;
import com.mars.tnc.exception.InvalidArgumentException;
import com.mars.tnc.exception.NotFoundException;
import com.mars.tnc.model.Device;
import com.mars.tnc.repository.DeviceRepository;
import com.mars.tnc.service.FileService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final ObjectMapper objectMapper;
    private final DeviceRepository deviceRepository;
    private JsonNode configTemplateJson;

    @PostConstruct
    private void init() throws IOException {
        loadConfigTemplate();
    }

    private void loadConfigTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/config-template.json");
        String content = Files.readString(Path.of(resource.getURI()));
        configTemplateJson = objectMapper.readTree(content);
    }

    @Override
    @Transactional(readOnly = true)
    public String getConfigFile(String filename, String imei) {
        EDeviceFile deviceFile = EDeviceFile.getDeviceFile(filename)
                .orElseThrow(() -> new InvalidArgumentException("Invalid device file"));

        Device device = deviceRepository.findFirstByImei(imei)
                .orElseThrow(() -> new NotFoundException("Device not found"));

        switch (deviceFile) {
            case INITIATION:
                return getInitiationFile(device);
            default:
                throw new InvalidArgumentException("Invalid config file");
        }
    }

    private String getInitiationFile(Device device) {
        String firmwareVersion = "";
        String appVersion = "";
        if (Objects.nonNull(device.getDeviceFirmware())) {
            firmwareVersion = device.getDeviceFirmware().getVersion();
            appVersion = device.getDeviceFirmware().getVersion();
        }

        JsonNode clonedJson = configTemplateJson.deepCopy();
        ((ObjectNode) clonedJson).put("firmware_version", firmwareVersion);
        ((ObjectNode) clonedJson).put("app_version", appVersion);
        ((ObjectNode) clonedJson).put("device_id", device.getImei());
        ((ObjectNode) clonedJson).put("device_name", device.getDeviceName());
        ((ObjectNode) clonedJson).put("device_model", device.getDeviceModel());
        ((ObjectNode) clonedJson).put("atm_id", "none");
        ((ObjectNode) clonedJson).put("client_code", "none");
        ((ObjectNode) clonedJson).put("server_code", "");
        ((ObjectNode) clonedJson).put("server_api", "none");
        ((ObjectNode) clonedJson).put("mqtt_host", "none");
        ((ObjectNode) clonedJson).put("mqtt_port", -1);
        ((ObjectNode) clonedJson).put("mqtt_username", "none");
        ((ObjectNode) clonedJson).put("mqtt_password", "none");
        ((ObjectNode) clonedJson).put("mqtt_receiver_id", "none");
        ((ObjectNode) clonedJson).put("mqtt_sender_id", "none");

        return clonedJson.toString();
    }
}
