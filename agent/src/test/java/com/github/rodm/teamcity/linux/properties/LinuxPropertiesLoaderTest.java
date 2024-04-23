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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static com.github.rodm.teamcity.linux.properties.LinuxProperties.OS_DESCRIPTION;
import static com.github.rodm.teamcity.linux.properties.LinuxProperties.OS_NAME;
import static com.github.rodm.teamcity.linux.properties.LinuxProperties.OS_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.BindMode.READ_WRITE;

class LinuxPropertiesLoaderTest {

    static class LinuxContainer extends GenericContainer<LinuxContainer> {
        LinuxContainer(DockerImageName imageName) {
            super(imageName);
        }
    }

    @TempDir
    private Path dir;

    private LinuxContainer container;

    @AfterEach
    void clean() throws IOException, InterruptedException {
        container.execInContainer("rm", "-rf", "/mnt/etc");
        container.stop();
    }

    @ParameterizedTest
    @CsvSource({
    //      image               name            version                         description
            "alpine:3.17.2,     Alpine Linux,   3.17.2,                         Alpine Linux v3.17",
            "alpine:3.19.1,     Alpine Linux,   3.19.1,                         Alpine Linux v3.19",
            "centos:6.10,       CentOS,         6.10,                           CentOS release 6.10 (Final)",
            "centos:7.9.2009,   CentOS Linux,   7.9.2009,                       CentOS Linux 7 (Core)",
            "centos:8.4.2105,   CentOS Linux,   8.4.2105,                       CentOS Linux 8",
            "opensuse/leap:15.5,openSUSE Leap,  15.5,                           openSUSE Leap 15.5",
            "ubuntu:18.04,      Ubuntu,         18.04.6 LTS (Bionic Beaver),    Ubuntu 18.04.6 LTS",
            "ubuntu:20.04,      Ubuntu,         20.04.6 LTS (Focal Fossa),      Ubuntu 20.04.6 LTS",
            "ubuntu:22.04,      Ubuntu,         22.04.4 LTS (Jammy Jellyfish),  Ubuntu 22.04.4 LTS"
    })
    void loadPropertiesFor(String image, String name, String version, String description) throws IOException, InterruptedException {
        container = new LinuxContainer(DockerImageName.parse(image))
                .withCommand("sleep 10")
                .withFileSystemBind(dir.toString(), "/mnt", READ_WRITE);
        container.start();
        assertThat(container.isRunning()).isTrue();

        container.execInContainer("cp", "-rL", "/etc", "/mnt");
        LinuxPropertiesLoader loader = new LinuxPropertiesLoader(dir);
        Properties properties = loader.loadProperties();
        assertThat(properties).isNotNull();
        assertThat(properties.getProperty(OS_NAME)).isEqualTo(name);
        assertThat(properties.getProperty(OS_VERSION)).isEqualTo(version);
        assertThat(properties.getProperty(OS_DESCRIPTION)).isEqualTo(description);
    }
}
