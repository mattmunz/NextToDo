package mattmunz.todo;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import mattmunz.property.PropertiedObject;
import mattmunz.property.Property;
import mattmunz.property.PropertyListBuilder;
import mattmunz.time.TimeOfDay;

/**
 * Task can have 0-n contexts or projects.
 * 
 * Priority (optional) always appears first and has a trailing space, and is upper case: (A). 
 * Context is preceding @, project has proceeding +.
 * 
 * Completed tasks start with lower case x followed by a space: x.
 * Each task line is preceded by a line number and a space.
 */
public class Task implements PropertiedObject
{
  private final Optional<String> colorCode;
  private final Optional<String> priority;
  private final Optional<DayOfWeek> day;
  private final Optional<TimeOfDay> timeOfDay;
  private final String identifier;
  private final String lineText;
  private final Set<String> projects;
  private final Set<String> contexts;
  private final boolean isCompleted;
  private final String message; 

  /**
   * @param lineText A line of todo.txt text that will be parsed into this object.
   */
  public Task(String lineText) { this(new TaskLineParser(lineText)); }
  
  public Task(String lineText, String identifier, boolean isCompleted, Optional<String> priority, 
       				Optional<String> colorCode, Optional<DayOfWeek> day, Optional<TimeOfDay> timeOfDay, 
       				Set<String> projects, Set<String> contexts, String message)
  {
    this.lineText = lineText;    
    this.identifier = identifier;  
    this.isCompleted = isCompleted; 
    this.priority = priority; 
    this.colorCode = colorCode; 
    this.day = day; 
    this.timeOfDay = timeOfDay; 
    this.projects = projects; 
    this.contexts = contexts; 
    this.message = message; 
  }
  
  private Task(TaskLineParser parser)
  {
    this(parser.getLineText(), parser.getIdentifier(), parser.getIsCompleted(), 
         parser.getPriority(), parser.getColorCode(), parser.getDay(), 
         parser.getTimeOfDay(), parser.getProjects(), parser.getContexts(), 
         parser.getMessage()); 
  }

  @Override
  public String toString() { return getToStringText(); }

  @Override
  public int hashCode() { return getHashCode(); }

  @Override
  public boolean equals(Object other) { return isEqualTo(other); }
  
  @Override
  public List<Property> getProperties()
  {
    return new PropertyListBuilder()
                .add("lineText", lineText).add("day", day).add("timeOfDay", timeOfDay)
                .add("contexts", contexts).add("projects", projects)
                .add("priority", priority).add("isCompleted", isCompleted).build();
  }

  public String getIdentifier() { return identifier; }

  public Optional<String> getColorCode() { return colorCode; }

  /**
   * @return This task as a Todo.txt formatted line of text.
   */
  public String getLineText() { return lineText; }
  
  public String getLineTextWithoutIdentifier() 
  {
  	return lineText.replaceFirst("^\\d+ ", "");
  }
  
  public Optional<DayOfWeek> getDay() { return day; }  

  public Optional<TimeOfDay> getTimeOfDay() { return timeOfDay; }

  public Set<String> getContexts() { return contexts; }

  public Set<String> getProjects() { return projects; } 

  public Optional<String> getPriority() { return priority; } 

  public boolean isCompleted() { return isCompleted; }

  /**
   * @return The task line stripped of any special tokens.
   */
  public String getMessage() { return message; }
}
