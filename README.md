# Gradle Plugins

A Collection of plugins for Gradle

## MavenPlugin

### Including in your Gradle buildscript.

```groovy
import com.darylteo.gradle.plugins.*
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.darylteo:maven-plugin:1.1.0'
  }
}

apply plugin: 'java' // requires one of the following for it to create tasks
apply plugin: MavenPlugin

// configuration
maven {
  release = false // when !release, appends '-SNAPSHOT' to the end of your artifact versions
  
  // credentials for your chosen maven repository, if required
  username = 'username'
  password = 'password'
  
  repository = 'https://oss.sonatype.org/service/local/staging/deploy/maven2/'
  snapshotRepository = 'https://oss.sonatype.org/content/repositories/snapshots/'
  
  // these are the settings for this repository, as a sample
  project {
    url 'http://github.com/darylteo/gradle-plugins'

    scm { url 'http://github.com/darylteo/gradle-plugins' }

    developers {
      developer {
        id 'darylteo'
        name 'Daryl Teo'
        email 'i.am@darylteo.com'
      }
    }

    licenses {
      license {
        name 'The Apache Software License, Version 2.0'
        url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
        distribution 'repo'
      }
    }

    properties { setProperty('project.build.sourceEncoding', 'UTF8') }
  }
}
````

