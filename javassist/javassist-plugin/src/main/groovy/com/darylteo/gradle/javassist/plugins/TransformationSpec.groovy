import org.gradle.api.tasks.SourceSet

public class TransformationSpec {
  public def name = "default"
  public def transformations = []

  public def sources = []
  public def classpath = []

  public def transformationTask

  public TransformationSpec(String name) {
    this.name = name
  }

  /* sources */
  public def from(SourceSet src) {
    this.sources.add(src.output.classesDir)
    this.classpath.addAll(src.compileClasspath.files)
    this.transformationTask.dependsOn()
  }

  /* selectors */
  public def all(Closure action) {
    return matching(~/.*/, action)
  }

  public def matching(def pattern, Closure action) {
    def transform = null
    this.transformations.add(transform)

    if(action) {
      action.resolveStrategy = Closure.DELEGATE_FIRST
      action.delegate = transform
      action(transform)
    }

    return transform
  }
}