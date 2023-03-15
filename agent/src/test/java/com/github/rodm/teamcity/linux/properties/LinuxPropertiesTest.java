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

import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.util.EventDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.github.rodm.teamcity.linux.properties.LinuxProperties.OS_DESCRIPTION;
import static com.github.rodm.teamcity.linux.properties.LinuxProperties.OS_NAME;
import static com.github.rodm.teamcity.linux.properties.LinuxProperties.OS_VERSION;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LinuxPropertiesTest {

    private final EventDispatcher<AgentLifeCycleListener> eventDispatcher = EventDispatcher.create(AgentLifeCycleListener.class);

    private final BuildAgent agent = mock(BuildAgent.class);
    private final BuildAgentConfiguration configuration = mock(BuildAgentConfiguration.class);
    private final Map<String, String> parameters = new HashMap<>();
    
    @BeforeEach
    void init() {
        when(agent.getConfiguration()).thenReturn(configuration);
        when(configuration.getConfigurationParameters()).thenReturn(parameters);
    }

    @Test
    void linuxAgentConfigureWithOsProperties() {
        Properties properties = new Properties();
        properties.put(OS_NAME, "os name");
        properties.put(OS_VERSION, "os version");
        properties.put(OS_DESCRIPTION, "os description");
        LinuxPropertiesLoader loader = new LinuxPropertiesLoader() {
            @Override
            public Properties loadProperties() {
                return properties;
            }
        };
        parameters.put("teamcity.agent.jvm.os.name", "Linux");

        LinuxProperties linuxProperties = new LinuxProperties(eventDispatcher, loader);
        linuxProperties.beforeAgentConfigurationLoaded(agent);

        verify(configuration).addConfigurationParameter(OS_NAME, "os name");
        verify(configuration).addConfigurationParameter(OS_VERSION, "os version");
        verify(configuration).addConfigurationParameter(OS_DESCRIPTION, "os description");
    }

    @Test
    void linuxAgentNotConfigured() {
        LinuxPropertiesLoader loader = new LinuxPropertiesLoader() {
            @Override
            public Properties loadProperties() {
                // empty Properties return for a read failure or unrecognized operating system
                return new Properties();
            }
        };
        parameters.put("teamcity.agent.jvm.os.name", "Linux");

        LinuxProperties linuxProperties = new LinuxProperties(eventDispatcher, loader);
        linuxProperties.beforeAgentConfigurationLoaded(agent);

        verify(configuration, never()).addConfigurationParameter(anyString(), anyString());
    }

    @Test
    void nonLinuxAgentNotConfigured() {
        LinuxPropertiesLoader loader = new LinuxPropertiesLoader();
        parameters.put("teamcity.agent.jvm.os.name", "Windows");

        LinuxProperties linuxProperties = new LinuxProperties(eventDispatcher, loader);
        linuxProperties.beforeAgentConfigurationLoaded(agent);

        verify(configuration, never()).addConfigurationParameter(anyString(), anyString());
    }
}
