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

import com.mars.tnc.constants.ApiConstants;
import com.mars.tnc.enums.EDeviceFile;
import com.mars.tnc.enums.EProcessAction;
import com.mars.tnc.enums.EProcessStatus;
import com.mars.tnc.enums.ETncType;
import com.mars.tnc.model.Device;
import com.mars.tnc.model.TncRequest;
import com.mars.tnc.model.TncTypeDeviceMap;
import com.mars.tnc.repository.DeviceRepository;
import com.mars.tnc.repository.TncRequestRepository;
import com.mars.tnc.repository.TncTypeDeviceMapRepository;
import com.mars.tnc.service.SeedService;
import com.mars.tnc.service.TncProcessTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeedServiceImpl implements SeedService {

    private final DeviceRepository deviceRepository;
    private final TncTypeDeviceMapRepository tncTypeDeviceMapRepository;
    private final TncRequestRepository tncRequestRepository;
    private final TncProcessTrackingService tncProcessTrackingService;
    private final OkHttpClient okHttpClient;

    @Override
    @Transactional
    public Map<String, Object> verify(String imei) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "false");
        response.put("mode", "none");
        response.put("bundle", new ArrayList<>());
        try {
            // Validate the device existence using imei
            Optional<Device> optionalDevice = deviceRepository.findFirstByImei(imei);
            if (optionalDevice.isEmpty()) {
                response.put("message", "Device not found");
                return response;
            }
            Device device = optionalDevice.get();
            // Check if device's tnc request exists
            Optional<TncRequest> optionalTncRequest = tncRequestRepository.findFirstByDeviceId(device.getId());
            if (optionalTncRequest.isEmpty()) {
                response.put("message", "Tnc request not found");
                return response;
            }
            TncRequest tncRequest = optionalTncRequest.get();
            // Ensure the tnc type of request is staging
            if (!tncRequest.getTncTypeId().equals(ETncType.STAGING.getKey())) {
                response.put("message", "Unmatched tnc request");
                return response;
            }

            response.put("status", "true");
            response.put("mode", "seed");
            response.put("message", "Valid device: " + imei);
            response.put("bundle", getStagingBundles(device.getImei(), tncRequest.getId()));

            tncProcessTrackingService.insertOrUpdate(tncRequest.getId(), EProcessAction.GET_DOWNLOAD_LIST,
                    ETncType.STAGING.getDesc(), EProcessStatus.SUCCESS);
        } catch (Exception e) {
            response.put("message", e.getMessage());
        }
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> updateMode(String imei, String mode) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "1");
        try {
            // Re-route to chubb api (use in testing environment only)
            //rerouteToChubb(imei, mode);
            // Verify mode
            Optional<ETncType> optionalTncType = ETncType.getTncType(Long.parseLong(mode));
            if (optionalTncType.isEmpty()) {
                response.put("message", "Invalid mode");
                return response;
            }
            ETncType tncType = optionalTncType.get();
            // Verify imei
            Optional<Device> optionalDevice = deviceRepository.findFirstByImei(imei);
            if (optionalDevice.isEmpty()) {
                response.put("message", "Device not found");
                return response;
            }
            Device device = optionalDevice.get();
            // Update tnc type device map
            Optional<TncTypeDeviceMap> optionalTncTypeDeviceMap = tncTypeDeviceMapRepository.findById(device.getId());
            TncTypeDeviceMap tncTypeDeviceMap;
            if (optionalTncTypeDeviceMap.isPresent()) {
                tncTypeDeviceMap = optionalTncTypeDeviceMap.get();
                tncTypeDeviceMap.setTncTypeId(tncType.getKey());
            } else {
                tncTypeDeviceMap = TncTypeDeviceMap.builder()
                        .deviceId(device.getId())
                        .tncTypeId(tncType.getKey())
                        .build();
            }
            tncTypeDeviceMapRepository.save(tncTypeDeviceMap);

            response.put("status", "0");
            response.put("message", String.format("Set Mode: %s", tncType.getDesc()));
        } catch (Exception e) {
            response.put("message", e.getMessage());
        }
        return response;
    }

    private List<String> getStagingBundles(String imei, Long tncRequestId) {
        List<String> bundles = new ArrayList<>();
        String baseUrl = String.format("%s%s/%s/%s", "http://tsm.mars-iot.cloud:58182/api",
                ApiConstants.FILE_BASE_URL, imei, tncRequestId);
        bundles.add(String.format("%s/%s", baseUrl, EDeviceFile.VERCHECK.getKey()));
        bundles.add(String.format("%s/%s", baseUrl, EDeviceFile.K3_AIOT.getKey()));
        bundles.add(String.format("%s/%s", baseUrl, EDeviceFile.SEED_AIOT.getKey()));
        bundles.add(String.format("%s/%s", baseUrl, EDeviceFile.K3APP.getKey()));
        bundles.add(String.format("%s/%s", baseUrl, EDeviceFile.STAGING.getKey()));
        bundles.add(String.format("%s/%s", baseUrl, EDeviceFile.INITIATION.getKey()));
        return bundles;
    }

    private void rerouteToChubb(String imei, String mode) {
        try {
            if (Long.parseLong(mode) < ETncType.STAGING.getKey()) {
                return;
            }

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("imei", imei)
                    .addFormDataPart("mode", mode)
                    .build();

            Request request = new Request.Builder()
                    .url("http://chubb.mars-iot.cloud:58181/api/seed/mode")
                    .post(requestBody)
                    .build();

            okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            log.error("Error while rerouting to chubb. Error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error while rerouting to chubb. Exception: {}", e.getMessage());
        }
    }
}
