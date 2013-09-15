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

import org.apache.maven.model.Dependency
import org.gradle.api.*
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.maven.*
import org.gradle.api.logging.*
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.*

import com.darylteo.gradle.vertx.tasks.*

public class MavenPlugin implements org.gradle.api.Plugin<Project> {
  void apply(Project project) {
    project.with {
      // apply required plugins
      apply plugin: 'java'    // maven plugin requires java plugin for install task to be configured
      apply plugin: 'maven-publish'
      apply plugin: 'signing'

      // apply conventions and extensions
      extensions.create 'maven', MavenPluginExtension, project

      // configure project
      configurations { archives }

      task('version') << {  println "Project '${project.name}' Version '${project.version}'"  }

      task('installSnapshot', dependsOn: install) {
        group = install.group
        description = "Installs a snapshot this artifact to your local maven repository"
      }

      task('uploadSnapshot', dependsOn: uploadArchives) {
        group = uploadArchives.group
        description = "Deploys a snapshot this artifact to your configured maven repository"
      }

      signing {
        required { gradle.taskGraph.hasTask(uploadArchives) && !gradle.taskGraph.hasTask(uploadSnapshot) }
        sign configurations.archives
      }


      //
      //        install {
      //          group 'maven'
      //          description 'Install this artifact into your local maven repository'
      //        }
      //
      //        uploadArchives {
      //          group 'maven'
      //          description = "Deploys a release of this artifact to your configured maven repository"
      //
      //          repositories {
      //            mavenDeployer {
      //              beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }
      //
      //              setUniqueVersion(false)
      //
      //              configuration = configurations.archives
      //              repository(url: maven.repository) {
      //                authentication(userName: maven.username, password: maven.password)
      //              }
      //
      //              snapshotRepository(url: maven.snapshotRepository) {
      //                authentication(userName: maven.username, password: maven.password)
      //              }
      //
      //              configurePom(project, pom)
      //            }
      //          }
      //        }


      publishing {
        def artifacts = []
        
        // Configure all required tasks for publishing
        task('sourcesJar', type: Jar) {
          classifier = 'sources'
          sourceSets.all { from allSource }
        }
        artifacts += sourcesJar

        if(tasks.findByName('javadoc')) {
          task('javadocJar', type:Jar, dependsOn: javadoc) {
            classifier = 'javadoc'
            from javadoc.destinationDir
          }

          artifacts += javadocJar
        }

        if(tasks.findByName('groovydoc')) {
          task('groovydocJar', type:Jar, dependsOn: groovydoc) {
            classifier = 'groovydoc'
            from groovydoc.destinationDir
          }

          artifacts += groovydocJar
        }

        publications {
          java(MavenPublication) {
            from components.java

            artifacts.each {
              artifact it 
            }
          }
        }
      }
      //
      //      gradle.taskGraph.whenReady { TaskExecutionGraph graph ->
      //        if(graph.hasTask(uploadSnapshot) || graph.hasTask(installSnapshot)){
      //          project.version = "${project.version}-SNAPSHOT"
      //        }
      //      }

    } // end .with
  }

  void configurePom(def project, MavenPom pom) {
    MavenPluginExtension maven = project.maven
    if(maven.groupId){
      pom.groupId = maven.groupId
    }
    if(maven.artifactId) {
      pom.artifactId = maven.artifactId
    }
    if(maven.version){
      pom.version = maven.version
    }

    // executes the configuration closures with the specified pom
    maven.pomConfigClosures?.each { closure -> pom.project closure }

    project.configurations.compile.allDependencies
      .withType(ProjectDependency)
      .matching{ dep ->
        dep.dependencyProject.plugins.hasPlugin(MavenPlugin)
      }
      .collect { dep -> dep.dependencyProject }
      .each { p ->
        if(p.state.executed) {
          def depPom = p.uploadArchives.repositories.mavenDeployer.pom.effectivePom
          def mavenDep = new Dependency()
          mavenDep.groupId = depPom.groupId
          mavenDep.artifactId = depPom.artifactId
          mavenDep.version = depPom.version
          mavenDep.scope = 'compile'

          pom.dependencies.add(mavenDep)
        } else {
          p.afterEvaluate {
            def depPom = p.uploadArchives.repositories.mavenDeployer.pom.effectivePom
            def mavenDep = new Dependency()
            mavenDep.groupId = depPom.groupId
            mavenDep.artifactId = depPom.artifactId
            mavenDep.version = depPom.version
            mavenDep.scope = 'compile'

            pom.dependencies.add(mavenDep)
          }
        }
      }

  }
}
