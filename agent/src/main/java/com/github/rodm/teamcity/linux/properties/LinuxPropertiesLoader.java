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
import org.jetbrains.annotations.NotNull;

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
import static jetbrains.buildServer.log.Loggers.AGENT_CATEGORY;

public class LinuxPropertiesLoader {

    private static final Logger LOG = Logger.getLogger(AGENT_CATEGORY);

    private static final Path OS_RELEASE = Paths.get("etc/os-release");
    private static final Path CENTOS_RELEASE = Paths.get("etc/centos-release");
    private static final Path REDHAT_RELEASE = Paths.get("etc/redhat-release");

    private static final Pattern RELEASE_PATTERN = Pattern.compile("^(.+)\\srelease\\s((\\d+)(\\.\\d+){1,2}+)(.*)$", Pattern.CASE_INSENSITIVE);

    private final Path root;

    public LinuxPropertiesLoader() {
        this(Paths.get("/"));
    }

    LinuxPropertiesLoader(Path root) {
        this.root = root;
    }

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

    private boolean exists(Path path) {
        return Files.exists(root.resolve(path));
    }

    private void loadReleaseFile(Path releasePath, Properties properties) {
        LOG.info("Loading Linux properties from " + root.resolve(releasePath));
        try {
            List<String> contents = Files.readAllLines(root.resolve(releasePath));
            Matcher matcher = RELEASE_PATTERN.matcher(contents.get(0));
            if (matcher.find()) {
                String name = matcher.group(1);
                String version = matcher.group(2);
                properties.setProperty(OS_NAME, sanitize(name));
                properties.setProperty(OS_VERSION, sanitize(version));
            }
            String description;
            if (exists(OS_RELEASE)) {
                Properties props = loadReleaseProperties();
                description = props.getProperty("PRETTY_NAME");
            } else {
                description = contents.get(0);
            }
            properties.setProperty(OS_DESCRIPTION, sanitize(description));
        }
        catch (IOException e) {
            LOG.warn("Exception reading " + releasePath + " file: " + e.getMessage());
        }
    }

    private void loadOsReleaseFile(Properties properties) {
        LOG.info("Loading Linux properties from " + root.resolve(OS_RELEASE));
        try {
            Properties props = loadReleaseProperties();
            String name = props.getProperty("NAME");
            String version = props.getProperty("VERSION", props.getProperty("VERSION_ID"));
            String description = props.getProperty("PRETTY_NAME");
            properties.setProperty(OS_NAME, sanitize(name));
            properties.setProperty(OS_VERSION, sanitize(version));
            properties.setProperty(OS_DESCRIPTION, sanitize(description));
        }
        catch (IOException e) {
            LOG.warn("Exception reading " + OS_RELEASE + " file: " + e.getMessage());
        }
    }

    private Properties loadReleaseProperties() throws IOException {
        try (Reader reader = Files.newBufferedReader(root.resolve(OS_RELEASE))) {
            Properties props = new Properties();
            props.load(reader);
            return props;
        }
    }

    @NotNull
    private static String sanitize(String value) {
        return value.replace("\"", "");
    }
}
