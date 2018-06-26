/*
 * Copyright 2018 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rodm.teamcity.linux.properties;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.util.EventDispatcher;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jetbrains.buildServer.log.Loggers.AGENT_CATEGORY;

public class LinuxProperties extends AgentLifeCycleAdapter {

    private static Logger LOG = Logger.getLogger(AGENT_CATEGORY + ".LinuxProperties");

    private static final String OS_NAME = "linux.os.name";
    private static final String OS_VERSION = "linux.os.version";
    private static final String OS_DESCRIPTION = "linux.os.description";

    private static final String OS_RELEASE = "/etc/os-release";
    private static final String CENTOS_RELEASE = "/etc/centos-release";
    private static final String REDHAT_RELEASE = "/etc/redhat-release";

    public LinuxProperties(EventDispatcher<AgentLifeCycleListener> eventDispatcher) {
        eventDispatcher.addListener(this);
    }

    @Override
    public void beforeAgentConfigurationLoaded(@NotNull BuildAgent agent) {
        BuildAgentConfiguration configuration = agent.getConfiguration();
        Map<String, String> parameters = configuration.getConfigurationParameters();
        if ("Linux".equalsIgnoreCase(parameters.get("teamcity.agent.jvm.os.name"))) {

            if (new File(CENTOS_RELEASE).exists()) {
                configureUsingReleaseFile(configuration, CENTOS_RELEASE);
            }
            else if (new File(OS_RELEASE).exists()) {
                configureUsingOsReleaseFile(configuration);
            }
            else if (new File(REDHAT_RELEASE).exists()) {
                configureUsingReleaseFile(configuration, REDHAT_RELEASE);
            }
            else {
                LOG.warn("Failed to identify Linux release");
            }
        }
    }

    private void configureUsingReleaseFile(BuildAgentConfiguration configuration, String releaseName) {
        try {
            List<String> contents = Files.readAllLines(Paths.get(releaseName));
            Pattern pattern = Pattern.compile("^(.+)(\\srelease\\s)(\\d+(\\.\\d+)+)(.+)$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(contents.get(0));
            if (matcher.find()) {
                String name = matcher.group(1);
                String version = matcher.group(3);
                configuration.addConfigurationParameter(OS_NAME, name);
                configuration.addConfigurationParameter(OS_VERSION, version);
            }
            String description = contents.get(0);
            if (new File(OS_RELEASE).exists()) {
                Properties props = new Properties();
                props.load(new FileReader(OS_RELEASE));
                description = props.getProperty("PRETTY_NAME");
            }
            configuration.addConfigurationParameter(OS_DESCRIPTION, description);
        }
        catch (IOException e) {
            LOG.warn("Exception reading " + releaseName + " file: " + e.getMessage());
        }
    }
    
    private void configureUsingOsReleaseFile(BuildAgentConfiguration configuration) {
        try {
            Properties props = new Properties();
            props.load(new FileReader(OS_RELEASE));
            String name = props.getProperty("NAME");
            String version = props.getProperty("VERSION");
            if (version == null) {
                version = props.getProperty("VERSION_ID");
            }
            String description = props.getProperty("PRETTY_NAME");
            configuration.addConfigurationParameter(OS_NAME, name.replace("\"", ""));
            configuration.addConfigurationParameter(OS_VERSION, version.replace("\"", ""));
            configuration.addConfigurationParameter(OS_DESCRIPTION, description.replace("\"", ""));
        }
        catch (IOException e) {
            LOG.warn("Exception reading " + OS_RELEASE + " file: " + e.getMessage());
        }
    }
}
