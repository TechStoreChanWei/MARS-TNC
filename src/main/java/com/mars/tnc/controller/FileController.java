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
import com.mars.tnc.enums.EProcessAction;
import com.mars.tnc.enums.EProcessStatus;
import com.mars.tnc.service.FileService;
import com.mars.tnc.service.TncProcessTrackingService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.FILE_BASE_URL)
public class FileController {

    private final FileService fileService;
    private final TncProcessTrackingService tncProcessTrackingService;

    @GetMapping("/{imei}/{filename:.+}")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("imei") String imei,
                                                              @PathVariable("filename") String filename,
                                                              HttpServletResponse response) {
        try {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            String content = fileService.getConfigFile(filename, imei);
            StreamingResponseBody stream = outputStream -> {
                try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                    writer.write(content);
                    writer.flush();
                } catch (IOException e) {
                    // Ignored exception
                }
            };

            return ResponseEntity.ok(stream);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{imei}/{tncRequestId}/{filename:.+}")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("imei") String imei,
                                                              @PathVariable("tncRequestId") Long tncRequestId,
                                                              @PathVariable("filename") String filename,
                                                              HttpServletResponse response) {
        try {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            String content = fileService.getConfigFile(filename, imei);
            StreamingResponseBody stream = outputStream -> {
                try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                    writer.write(content);
                    writer.flush();

                    tncProcessTrackingService.insertOrUpdate(tncRequestId, EProcessAction.DOWNLOAD,
                            filename, EProcessStatus.SUCCESS);
                } catch (IOException e) {
                    tncProcessTrackingService.insertOrUpdate(tncRequestId, EProcessAction.DOWNLOAD,
                            filename, EProcessStatus.FAIL);
                }
            };

            return ResponseEntity.ok(stream);
        } catch (Exception e) {
            tncProcessTrackingService.insertOrUpdate(tncRequestId, EProcessAction.DOWNLOAD,
                    filename, EProcessStatus.FAIL);
            return ResponseEntity.internalServerError().build();
        }
    }
}
