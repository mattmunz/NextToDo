package mattmunz.todo.cli;

import static mattmunz.time.DayHelper.today;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import mattmunz.lang.SystemHelper;
import mattmunz.todo.Task;

import com.google.common.collect.Ordering;

/** 
 * A replacement for the unix sort command suitable for use with todo.txt and the todo 
 * system. 
 */
public class Sorter
{
  public static void main(String[] arguments) throws IOException { new Sorter().run(); }

  private final DayOfWeek sortDay;
  
  public Sorter(DayOfWeek sortDay) { this.sortDay = sortDay; }

  Sorter() { this(today()); }

  /**
   * 1) Read in stdin to a stream of lines
   * 2) Sort the lines using a comparator which gives the desired ordering
   * 3) Print out all sorted lines to Sys.out
   */
  private void run() throws IOException
  {
    sort(new SystemHelper().readLinesFromSystemIn(1000000).map(Task::new))
      .map(Task::getLineText).forEach(System.out::println);
  }

  public Stream<Task> sort(Stream<Task> tasks)
  {
    /*
     * Sort order: DayHelper, tod, context, project, priority. Empty fields are given highest order.
     */

    Ordering<Task> dayOrdering = getOptionalOrdering(this::getDayNumber);
    
    Ordering<Task> taskOrdering 
      = getOrdering(Task::isCompleted)
          .compound(dayOrdering)
          .compound(getOptionalOrdering(Task::getTimeOfDay))
          .compound(getSetOrdering(Task::getContexts))
          .compound(getSetOrdering(Task::getProjects))
          .compound(getOptionalOrdering(Task::getPriority));
    
    return tasks.sorted(taskOrdering);
  }
  
  private Optional<Integer> getDayNumber(Task task)
  {
    return task.getDay().map(this::getDayNumber);
  }

  /**
   * @return An integer between 1 and 7 for this day representing the sort order. I.e. if the 
   *         sort day is W and the input is F, the this method returns 3 and if the input is 
   *         M, returns 6.
   */
  public int getDayNumber(DayOfWeek day)
  {
    int dayOrdinal = day.getValue();
    int sortDayOrdinal = sortDay.getValue();
    
    // TODO This is hard to read. Maybe use modulo arithmetic? 
    return dayOrdinal >= sortDayOrdinal ? dayOrdinal - sortDayOrdinal + 1 
                                        : 8 - (sortDayOrdinal - dayOrdinal);
  }

  private <T, C extends Comparable<C>> Ordering<T> getOrdering(Function<T, C> comparableSelector) 
  {
    return getOptionalOrdering(comparableSelector.andThen(Optional::ofNullable));
  }
  
  private <T, C extends Comparable<C>> Ordering<T> 
    getOptionalOrdering(Function<T, Optional<C>> comparableSelector) 
  {
    return new Ordering<T>() 
    {
      @Override
      public int compare(T left, T right)
      {
        Optional<C> leftComparable = comparableSelector.apply(left);
        Optional<C> rightComparable = comparableSelector.apply(right);
        
        if (!leftComparable.isPresent()) { return rightComparable.isPresent() ? -1 : 0; }
        
        if (!rightComparable.isPresent()) { return 1; }
        
        return leftComparable.get().compareTo(rightComparable.get());
      }      
    };
  }
  
  private <T, C extends Comparable<C>> Ordering<T> getSetOrdering(Function<T, Set<C>> setSelector) 
  {
    return new Ordering<T>() 
    {
      @Override
      public int compare(T left, T right)
      {
        return compareSets(setSelector.apply(left), setSelector.apply(right));
      }      
    };
  }

  /**
   * Treats empty set as highest order.
   * Members of sets are sorted and then compared in order (lexicographically)/
   * 
   * TODO Move to a more general module/package.
   */
  private <C extends Comparable<C>> int compareSets(Set<C> left, Set<C> right)
  {
    if (left == right) { return 0; }
    if (left == null) { return -1; }
    if (right == null) { return 1; }
    
    if (left.isEmpty() && right.isEmpty()) { return 0; }
    if (left.isEmpty()) { return -1; }
    if (right.isEmpty()) { return 1; }
    
    ArrayList<C> sortedLeft = new ArrayList<C>(left);
    Collections.sort(sortedLeft);

    ArrayList<C> sortedRight = new ArrayList<C>(right);
    Collections.sort(sortedRight);
    
    Iterator<C> leftIterator = sortedLeft.iterator();
    Iterator<C> rightIterator = sortedRight.iterator();
    
    while (leftIterator.hasNext() && rightIterator.hasNext()) 
    {
      C leftItem = leftIterator.next();
      C rightItem = rightIterator.next();
      
      int comparison = leftItem.compareTo(rightItem);
      
      if (comparison != 0) { return comparison; }
    }
    
    return leftIterator.hasNext() ? 1 : rightIterator.hasNext() ? -1 : 0;
  }
}
