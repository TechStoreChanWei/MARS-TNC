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

# Use an OpenJDK 17 base image
FROM openjdk:17

# Get build arguments
ARG TZ
ARG VERSION

# Set timezone
ENV TZ=${TZ}

# Set the working directory
WORKDIR /app

# Copy JAR file
COPY build/libs/MARS-TNC-${VERSION}.jar /app/app.jar

# Expose the service port
EXPOSE 8181

# Set minimum and maximum heap memory (e.g., min 512m, max 2g)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Start JAVA App with custom memory settings
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

