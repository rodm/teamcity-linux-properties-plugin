/*
 * Copyright 2018 Rod MacKenzie.
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

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Properties;

public class LinuxProperties extends AgentLifeCycleAdapter {

    static final String OS_NAME = "linux.os.name";
    static final String OS_VERSION = "linux.os.version";
    static final String OS_DESCRIPTION = "linux.os.description";

    public LinuxProperties(EventDispatcher<AgentLifeCycleListener> eventDispatcher) {
        eventDispatcher.addListener(this);
    }

    @Override
    public void beforeAgentConfigurationLoaded(@NotNull BuildAgent agent) {
        BuildAgentConfiguration configuration = agent.getConfiguration();
        Map<String, String> parameters = configuration.getConfigurationParameters();
        if ("Linux".equalsIgnoreCase(parameters.get("teamcity.agent.jvm.os.name"))) {
            LinuxPropertiesLoader loader = new LinuxPropertiesLoader();
            Properties properties = loader.loadProperties();
            configureProperties(configuration, properties);
        }
    }

    private static void configureProperties(BuildAgentConfiguration configuration, Properties properties) {
        if (properties.containsKey(OS_NAME)) {
            configuration.addConfigurationParameter(OS_NAME, properties.getProperty(OS_NAME));
        }
        if (properties.containsKey(OS_VERSION)) {
            configuration.addConfigurationParameter(OS_VERSION, properties.getProperty(OS_VERSION));
        }
        if (properties.containsKey(OS_DESCRIPTION)) {
            configuration.addConfigurationParameter(OS_DESCRIPTION, properties.getProperty(OS_DESCRIPTION));
        }
    }
}
