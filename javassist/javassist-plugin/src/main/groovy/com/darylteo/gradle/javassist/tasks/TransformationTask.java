package com.darylteo.gradle.javassist.tasks;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.tasks.AbstractCopyTask;

public class TransformationTask extends AbstractCopyTask {

  private String destinationDir;

  public String getDestinationDir() {
    return destinationDir;
  }

  public void setDestinationDir(String destinationDir) {
    this.destinationDir = destinationDir;
  }

  //  @Input
//  public List<File> sources = new LinkedList<>();
//  @Input
//  public List<Transformation> transformations = new LinkedList<>();
//  @OutputDirectory
//  public File outputDir;
//
//  private Pattern extensionPattern;
//
//  public TransformationTask() {
//    final TransformationTask that = this;
//    final Project project = this.getProject();
//
//    this.outputDir = project.file(String.format("%s/transformations/%s", project.getBuildDir(), this.getName()));
//
//    this.getInputs().property("sources", new Callable() {
//      @Override
//      public Object call() throws Exception {
//        return that.sources;
//      }
//
//    });
//    this.getOutputs().dir(new Callable() {
//      @Override
//      public Object call() throws Exception {
//        return that.outputDir;
//      }
//
//    });
//
//    project.afterEvaluate(new Action<Project>() {
//      @Override
//      public void execute(Project project) {
//        if (project.hasProperty("sourceSets")) {
//          for (SourceSet s : (SourceSetContainer) project.property("sourceSets")) {
//            System.out.println(s.getOutput());
//            System.out.println(Arrays.toString(s.getOutput().getFiles().toArray()));
//
//            that.from(s.getOutput().getFiles());
//            that.dependsOn(project.getTasks().getByName(s.getClassesTaskName()));
//          }
//        } else {
//          throw new RuntimeException(String.format("The project \"%s\" does not have any sourcesets. Make sure you have applied the required plugins", project.toString()));
//        }
//      }
//    });
//  }
//
//  public void from(Object... path) {
//    this.sources.addAll(this.getProject().files(path).getFiles());
//  }
//
//  public void into(Object path) {
//    this.outputDir = this.getProject().file(path);
//  }
//
//  public Transformation transform(final Closure action) {
//    // defer resolution of outputDir until run
//    Transformation transform = new Transformation(new ClosureBackedClassTransformation(action));
//    transformations.add(transform);
//
//    return transform;
//  }
//
//  @TaskAction
//  public void run() throws Exception {
//    this.extensionPattern = Pattern.compile("\\.(.*)$");
//
//    final ClassPool parent = new ClassPool(true);
//    final File output = this.outputDir;
//    output.mkdirs();
//
//    // set up the classpath for the classpool
//    for (File f : this.getProject().getConfigurations().getByName("compile").getFiles()) {
//      parent.appendClassPath(f.toString());
//    }
//
//    for (File src : this.sources) {
//      parent.appendClassPath(src.toString());
//    }
//
//    // identify all the classes we're manipulating (only those in the sources)
//    List<CtClass> classes = getClasses(parent, this.sources);
//
//    for (Transformation t : this.transformations) {
//      t.call(classes, this.outputDir);
//    }
//  }
//
//  private List<CtClass> getClasses(ClassPool parent, File root, Collection<File> sources) throws Exception {
//    List<CtClass> result = new LinkedList<>();
//
//    Collection<File> sources =
//
//    for (File src : sources) {
//      // find and match extension
//      Matcher matcher = this.extensionPattern.matcher(src.getName());
//      String extension = matcher.find() ? matcher.group(1) : "";
//
//      // extract archive types
//      switch (extension) {
//        case "jar":
//        case "zip":
//          result.addAll(getClasses(parent, "", this.getProject().zipTree(src).getFiles()));
//          break;
//
//        case "class":
//          //TODO:
//          System.out.println(parent.);
//          // result.add(parent.getCtClass(src.getName()));
//          break;
//
//        // directory
//        default:
//          result.addAll(getClasses(parent, root, this.getProject().fileTree(src).matching().getFiles()));
//          break;
//      }
//    }
//
//    return result;
//  }


  public TransformationTask() {
    final Project project = this.getProject();
    final TransformationTask _this = this;

    project.afterEvaluate(new Action<Project>() {
      @Override
      public void execute(Project project) {
      }
    });
  }

  @Override
  protected CopyAction createCopyAction() {
    String dir = this.destinationDir;
    if (dir == null) {
      dir = String.format("%s/transformations/%s", this.getProject().getBuildDir(), this.getName());
    }
    return new TransformationAction(dir, this.getSource().getFiles());
  }

}
