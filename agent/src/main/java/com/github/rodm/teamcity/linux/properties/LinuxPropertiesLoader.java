/*
 * Copyright 2023 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rodm.teamcity.linux.properties;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.rodm.teamcity.linux.properties.LinuxProperties.OS_DESCRIPTION;
import static com.github.rodm.teamcity.linux.properties.LinuxProperties.OS_NAME;
import static com.github.rodm.teamcity.linux.properties.LinuxProperties.OS_VERSION;
import static java.nio.file.Files.exists;
import static jetbrains.buildServer.log.Loggers.AGENT_CATEGORY;

public class LinuxPropertiesLoader {

    private static final Logger LOG = Logger.getLogger(AGENT_CATEGORY + ".LinuxProperties");

    private static final Path OS_RELEASE = Paths.get("/etc/os-release");
    private static final Path CENTOS_RELEASE = Paths.get("/etc/centos-release");
    private static final Path REDHAT_RELEASE = Paths.get("/etc/redhat-release");

    public Properties loadProperties() {
        Properties properties = new Properties();
        if (exists(CENTOS_RELEASE)) {
            loadReleaseFile(CENTOS_RELEASE, properties);
        }
        else if (exists(OS_RELEASE)) {
            loadOsReleaseFile(properties);
        }
        else if (exists(REDHAT_RELEASE)) {
            loadReleaseFile(REDHAT_RELEASE, properties);
        }
        else {
            LOG.warn("Failed to identify Linux release");
        }
        return properties;
    }

    private void loadReleaseFile(Path releasePath, Properties properties) {
        try {
            List<String> contents = Files.readAllLines(releasePath);
            Pattern pattern = Pattern.compile("^(.+)(\\srelease\\s)(\\d+(\\.\\d+)+)(.+)$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(contents.get(0));
            if (matcher.find()) {
                String name = matcher.group(1);
                String version = matcher.group(3);
                properties.setProperty(OS_NAME, name);
                properties.setProperty(OS_VERSION, version);
            }
            String description = contents.get(0);
            if (exists(OS_RELEASE)) {
                Properties props = loadReleaseProperties();
                description = props.getProperty("PRETTY_NAME");
            }
            properties.setProperty(OS_DESCRIPTION, description);
        }
        catch (IOException e) {
            LOG.warn("Exception reading " + releasePath + " file: " + e.getMessage());
        }
    }

    private void loadOsReleaseFile(Properties properties) {
        try {
            Properties props = loadReleaseProperties();
            String name = props.getProperty("NAME");
            String version = props.getProperty("VERSION");
            if (version == null) {
                version = props.getProperty("VERSION_ID");
            }
            String description = props.getProperty("PRETTY_NAME");
            properties.setProperty(OS_NAME, name.replace("\"", ""));
            properties.setProperty(OS_VERSION, version.replace("\"", ""));
            properties.setProperty(OS_DESCRIPTION, description.replace("\"", ""));
        }
        catch (IOException e) {
            LOG.warn("Exception reading " + OS_RELEASE + " file: " + e.getMessage());
        }
    }

    private Properties loadReleaseProperties() throws IOException {
        try (Reader reader = Files.newBufferedReader(OS_RELEASE)) {
            Properties props = new Properties();
            props.load(reader);
            return props;
        }
    }
}
