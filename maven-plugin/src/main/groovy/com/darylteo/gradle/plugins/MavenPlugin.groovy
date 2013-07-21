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
import org.gradle.api.logging.*
import org.gradle.api.tasks.bundling.*

import com.darylteo.gradle.vertx.tasks.*

public class MavenPlugin implements org.gradle.api.Plugin<Project> {
  void apply(Project project) {
    project.with {
      // apply required plugins
      apply plugin: 'java'
      apply plugin: 'maven'
      apply plugin: 'signing'

      // apply conventions and extensions
      project.extensions.create("maven", MavenPluginExtension)

      // maven tasks
      task('sourcesJar', type: Jar, dependsOn: classes) {
        classifier = 'sources'
        sourceSets.all {  from allSource }
      }

      task('javadocJar', type: Jar, dependsOn: javadoc) {
        classifier = 'javadoc'
        from javadoc.destinationDir
      }

      // configure project
      configurations { archives }

      artifacts { archives sourcesJar }
      artifacts { archives javadocJar }

      signing {
        required { maven.release && gradle.taskGraph.hasTask("uploadArchives") }
        sign configurations.archives
      }

      install {
        group 'maven'
        description 'Install this artifact into your local maven repository'


        repositories.mavenInstaller {
          afterEvaluate {
            configurePom(project, pom);
          }
        }
      }

      uploadArchives {
        group 'maven'
        description = "Deploys this artifact to your configured maven repository"

        repositories {
          mavenDeployer {
            project.maven.pom = pom
          }
        }

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

    } // end .with
  }

  void configurePom(def project, def pom) {
    project.with {
      if(!maven.release) {
        pom.version = "${project.version}-SNAPSHOT"
      }
    }
  }
}
