/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.darylteo.gradle.plugins.maven

import org.gradle.api.*
import org.gradle.api.artifacts.maven.*
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.logging.*
import org.gradle.api.tasks.bundling.*

import com.darylteo.gradle.vertx.tasks.*

public class MavenPlugin implements org.gradle.api.Plugin<Project> {
  void apply(Project project) {
    project.with {
      // apply required plugins
      apply plugin: 'maven'
      apply plugin: 'signing'

      // apply conventions and extensions
      extensions.create 'maven', MavenPluginExtension, project
      maven.extensions.create 'releases', MavenPluginRepositoryExtension
      maven.extensions.create 'snapshots', MavenPluginRepositoryExtension

      // configure project
      configurations { archives }

      install {
        doFirst {
          repositories.mavenInstaller.pom.whenConfigured { println "Installing ${groupId}:${artifactId}:${version}" }
        }
      }

      uploadArchives {
        repositories {
          mavenDeployer {
            beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }

            setUniqueVersion(false)

            afterEvaluate {
              configuration = configurations.archives
              repository(url: maven.releases.repository) {
                authentication(userName: maven.releases.username, password: maven.releases.password)
              }

              snapshotRepository(url: maven.snapshots.repository) {
                authentication(userName: maven.snapshots.username, password: maven.snapshots.password)
              }
            }
          }
        }
      }

      task('version') << {  println "Project '${project.name}' Version '${project.version}'" }

      signing {
        required { project.maven.release && gradle.taskGraph.hasTask(uploadArchives) }
        sign configurations.archives
      }

    } // end .with
  }
}
