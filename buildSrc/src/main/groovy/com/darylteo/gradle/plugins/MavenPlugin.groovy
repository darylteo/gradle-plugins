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
package com.darylteo.gradle.plugins

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
      project.extensions.create("maven", MavenPluginExtension)

      // configure project
      configurations { archives }

      task('sourcesJar', type: Jar) {
        classifier = 'sources'
        sourceSets.all { from allSource }
      }
      artifacts { archives sourcesJar }

      if(tasks.findByName('javadoc')) {
        task('javadocJar', type:Jar, dependsOn: javadoc) {
          classifier = 'javadoc'
          from javadoc.destinationDir
        }

        artifacts { archives javadocJar }
      }

      if(tasks.findByName('groovydoc')) {
        task('groovydocJar', type:Jar, dependsOn: groovydoc) {
          classifier = 'groovydoc'
          from groovydoc.destinationDir
        }

        artifacts { archives groovydocJar }
      }

      signing {
        required { gradle.taskGraph.hasTask(uploadArchives) && !gradle.taskGraph.hasTask(uploadSnapshot) }
        sign configurations.archives
      }

      install {
        group 'maven'
        description 'Install this artifact into your local maven repository'

        repositories {
          mavenInstaller {
            afterEvaluate {
              configurePom(project, pom);
            }
          }
        }
      }

      uploadArchives {
        group 'maven'
        description = "Deploys a release of this artifact to your configured maven repository"

        repositories {
          mavenDeployer {
            beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }

            setUniqueVersion(false)

            afterEvaluate {
              configuration = configurations.archives
              repository(url: maven.repository) {
                authentication(userName: maven.username, password: maven.password)
              }

              snapshotRepository(url: maven.snapshotRepository) {
                authentication(userName: maven.username, password: maven.password)
              }

              configurePom(project, pom)
            }
          }
        }
      }

      task('uploadSnapshot', dependsOn: uploadArchives) {
        group = uploadArchives.group
        description = "Deploys a snapshot this artifact to your configured maven repository"
      }

      gradle.taskGraph.whenReady { TaskExecutionGraph graph ->
        if(graph.hasTask(uploadSnapshot)){
          project.version = "${project.version}-SNAPSHOT"
        }
      }

    } // end .with
  }

  void configurePom(def project, def pom) {
    project.with {
      // executes the configuration closures with the specified pom
      project.maven.pomConfigClosures?.each { closure -> pom.project closure }
    }
  }
}
