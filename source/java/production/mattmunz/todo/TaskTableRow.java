package mattmunz.todo;

import static java.time.format.TextStyle.NARROW;
import static java.util.Arrays.asList;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import mattmunz.time.TimeOfDay;

// TODO Rename
public class TaskTableRow
{
  // TODO Make these static methods non-static, perhaps using the builder pattern.
  
  /**
   * @return Original message minus all of the data in other columns
   */
  private static String getMessage(Task task)
  {
    // TODO Just for now. Really need to remove the cruft from this text...
    return task.getMessage();
  }

  private static String getContextsText(Task task)
  {
    // TODO Add sorting here. Same sort as used in the Sorter class.
    return task.getContexts().stream().collect(Collectors.joining(", "));
  }

  private static String getProjectsText(Task task)
  {
    // TODO Add sorting here. Same sort as used in the Sorter class.
    return task.getProjects().stream().collect(Collectors.joining(", "));
  }

  // TODO This could be made prettier with padding
  private static String getDayTimeText(Task task)
  {
    Optional<DayOfWeek> day = task.getDay();
    
    if (!day.isPresent()) { return " "; }
    
    String dayText = day.get().getDisplayName(NARROW, Locale.getDefault());
    
    Optional<TimeOfDay> timeOfDay = task.getTimeOfDay();
    
    return timeOfDay.isPresent() ? dayText + ":" + timeOfDay.get().getIdentifier() : dayText; 
  }

  private final Optional<String> colorCode;
  private final List<String> cells;
  
  public TaskTableRow(String taskLine) { this(new Task(taskLine)); }
  
  TaskTableRow(Task task)
  {
    this(task.getColorCode(), task.getIdentifier(), task.getPriority().orElse(" "), 
         getDayTimeText(task), getContextsText(task), getProjectsText(task), 
         getMessage(task)); 
  }
  
  public TaskTableRow(Optional<String> colorCode, List<String> cells)
  {
    this.colorCode = colorCode;
    this.cells = cells;
  }
  
  public TaskTableRow(Optional<String> colorCode, String... cells) { this(colorCode, asList(cells)); }
  
  public Optional<String> getColorCode() { return colorCode; }

  public List<String> getCells() { return cells; }
}