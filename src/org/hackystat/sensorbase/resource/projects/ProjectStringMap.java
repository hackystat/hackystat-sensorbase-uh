package org.hackystat.sensorbase.resource.projects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hackystat.sensorbase.resource.projects.jaxb.Project;

/**
 * Helper class to support Project to String mappings.
 * We need this because I don't know how to define JAXB Project instances 
 * with a custom equals() and hashCode() method such that equal Project instances are 
 * those with the same name and owner. 
 * 
 * @author Philip Johnson
 */
public class ProjectStringMap {
  
  /** The internal map. */
  private Map<Project, String> project2string = new HashMap<Project, String>();
  
  /**
   * Puts [project, info] into the map, after removing any current project instance from
   * the map with the same name and owner. 
   * @param project The Project to be added.
   * @param info The associated String. 
   */
  public void put(Project project, String info) {
    this.remove(project);
    project2string.put(project, info);
  }
  
  /**
   * Returns the string associated with Project, or null if not found.
   * @param project The project whose string is to be retrieved.
   * @return The string, or null if Project is not found in the map.
   */
  public String get(Project project) {
    String name = project.getName();
    String owner = project.getOwner();
    for (Project oldProject : this.project2string.keySet()) {
      if (oldProject.getName().equals(name) && oldProject.getOwner().equals(owner)) {
        return this.project2string.get(oldProject);
      }
    }
    throw new RuntimeException("Did not find project: " + name + " " + owner);
  }
  
  /**
   * Removes any projects with the same name and owner as Project from this data structure.
   * @param project A project specifying the projects to be removed by name and owner. 
   */
  public void remove(Project project) {
    String owner = project.getOwner();
    String name = project.getName();
    List<Project> projectsToRemove = new ArrayList<Project>();
    // First see if a Project with this name and owner exists in the internal map.
    Set<Project> oldProjects = this.project2string.keySet();
    for (Project oldProject : oldProjects) {
      if (oldProject.getName().equals(name) && oldProject.getOwner().equals(owner)) {
        projectsToRemove.add(oldProject);
      }
    }
    // If we found any, get rid of them.
    for (Project projectToRemove : projectsToRemove) {
      project2string.remove(projectToRemove);
    }
  }
  
  /**
   * Returns the strings in this map as a Collection.
   * @return The strings as a collection.
   */
  public Collection<String> values() {
    return this.project2string.values();
  }
}
