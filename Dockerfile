# *****************************************************************************************
#
# TECH-STORE CONFIDENTIAL
# Copyright 2024 Tech-Store Corporation All Rights Reserved.
# The source code contained or described herein and all documents related to the
# source code ("Material") are owned by Tech-Store Malaysia or its suppliers or
# licensors. Title to the Material remains with Tech-Store Malaysia or its suppliers
# and licensors. The Material contains trade secrets and proprietary and
# confidential information of Tech-Store or its suppliers and licensors. The Material
# is protected by worldwide copyright and trade secret laws and treaty provisions.
# No part of the Material may be used, copied, reproduced, modified, published,
# uploaded, posted, transmitted, distributed, or disclosed in any way without
# Tech-Store's prior express written permission.
#
# No license under any patent, copyright, trade secret or other intellectual
# property right is granted to or conferred upon you by disclosure or delivery of
# the Materials, either expressly, by implication, inducement, estoppel or
# otherwise. Any license under such intellectual property rights must be express
# and approved by Tech-Store in writing.
#
# *****************************************************************************************

# Use an OpenJDK 17 base image for building
FROM openjdk:17 AS builder

# Get build arguments
ARG TZ
ARG VERSION
ARG SERVICE_NAME=mars-tnc

# Set timezone
ENV TZ=${TZ}

# Set the working directory
WORKDIR /app

# Copy JAR file
COPY build/libs/MARS-TNC-${VERSION}.jar /app/app.jar

# Generate the class list for AppCDS
RUN java -Xshare:off -XX:DumpLoadedClassList=classes.lst -jar app.jar --spring.main.lazy-initialization=true || true

# Create the AppCDS archive with a unique name
RUN java -Xshare:dump -XX:SharedClassListFile=classes.lst -XX:SharedArchiveFile=${SERVICE_NAME}-cds.jsa -jar app.jar || true

# Use a clean JDK runtime for the final image
FROM openjdk:17

# Get build arguments
ARG TZ
ARG VERSION
ARG SERVICE_NAME=mars-tnc

# Set timezone
ENV TZ=${TZ}

# Set the working directory
WORKDIR /app

# Copy JAR file and AppCDS archive from the builder stage
COPY --from=builder /app/app.jar /app/app.jar
COPY --from=builder /app/${SERVICE_NAME}-cds.jsa /app/${SERVICE_NAME}-cds.jsa

# Expose the service port
EXPOSE 9810

# Set minimum and maximum heap memory and enable AppCDS
ENV JAVA_OPTS="-Xms256m -Xmx512m -Xshare:on -XX:SharedArchiveFile=/app/${SERVICE_NAME}-cds.jsa"

# Start the application with AppCDS optimization
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

