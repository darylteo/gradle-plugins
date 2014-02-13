# Gradle Watcher

Simple plugin for running tasks on filesystem changes.

## Sample

```groovy
buildscript {
	repositories {
		mavenCentral()
	}

	repositories {
		classpath 'com.darylteo.gradle:gradle-watcher:0.1.1'
	}
}

import com.darylteo.gradle.watcher.tasks.WatcherTask

apply plugin: 'java'
apply plugin: 'watcher'

// todo: configuration of underlying WatchService

task watchTask(type: WatcherTask) {
	// configure what you want your task to do when file system changes
	// by setting a list made up of Strings (task name) or Tasks
	tasks = ['build']

	// ant style includes and excludes
	includes = ['src/**'] // this is the default
	excludes = []

	// by default, this task will block on completion. 
	// if you want to use this task as part of a chain of tasks, set this to false 
	// and block elsewhere so that gradle doesn't exit
	block = true
}
````

## Usage

Just run the task you created.

```bash
gradle watchTask
````

Now make a change in your folders and watch the magic happen.