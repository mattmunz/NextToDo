package mattmunz.todo;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.DayOfWeek.FRIDAY;
import static mattmunz.time.TimeOfDay.AFTERNOON;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class TaskTest
{
  /**
   * Task can have 0-n contexts or projects.
   * 
   * Priority (optional) always appears first and has a trailing space, and is uppercase: (A) 
   * Context is preceeding @, project has proceedign +
   * 
   * Completed tasks start with lowercase x followed by a space: x  
   */
  @Test
  public void parsingValidLines()
  { 
    String taskLine1 = "01 (C) @Computer +Fun Play mind-enhanicng computer games @Home +Health\n";
    String taskLine2 = "17 x (C) @Computer +Fun Play mind-enhanicng computer games @Home +Health\n";
    String taskLine3 = "55 @Computer +Fun Play mind-enhanicng computer games\n";
    String taskLine4 = "99 @Computer +Fun Play mind-enhanicng computer games day:W\n";
    String taskLine5 = "235 @Computer +Fun Play mind-enhanicng computer games day:F tod:A\n";
    String taskLine6 = "01 (D) 2016-03-05 Test\n";
    String taskLine7 = "01 (D) 2016-03-05 Test";
    String taskLine8 = "01 (D) tod:A Test day:W";
    
    Task task1 = new Task(taskLine1);
    assertEquals(taskLine1, task1.getLineText());
    assertEquals("C", task1.getPriority().get());
    assertSetEquals(new HashSet<String>(asList("Fun", "Health")), task1.getProjects());
    assertSetEquals(new HashSet<String>(asList("Computer", "Home")), task1.getContexts());
    assertFalse(task1.isCompleted());

    Task task2 = new Task(taskLine2);
    assertEquals(taskLine2, task2.getLineText());
    assertTrue(task2.isCompleted());

    Task task3 = new Task(taskLine3);
    assertEquals(taskLine3, task3.getLineText());
    assertFalse(task3.getPriority().isPresent());
    
    Task task4 = new Task(taskLine4);
    assertEquals(WEDNESDAY, task4.getDay().get());

    Task task5 = new Task(taskLine5);
    assertEquals(FRIDAY, task5.getDay().get());
    assertEquals(AFTERNOON, task5.getTimeOfDay().get());

    Task task6 = new Task(taskLine6);
    assertEquals("D", task6.getPriority().get());
    assertFalse(task6.getDay().isPresent());

    Task task7 = new Task(taskLine7);
    assertEquals("D", task7.getPriority().get());
    assertFalse(task7.getDay().isPresent());

    Task task8 = new Task(taskLine8);
    assertEquals(WEDNESDAY, task8.getDay().get());
    assertEquals(AFTERNOON, task8.getTimeOfDay().get());
  }
  
  @Test
  public void parseContexts()
  {
    Task task1 = new Task("01 (A) Baz @Prospero");
    assertSetEquals(new HashSet<String>(asList("Prospero")), task1.getContexts());
    
    Task task2 = new Task("02 (D) Bar @Mab");
    assertSetEquals(new HashSet<String>(asList("Mab")), task2.getContexts());
  }
  
  @Test 
  public void parseTypicalExample()
  {
    new Task("01 (A) +Health +AH Passeggiata after every meal -- 15m * day:F tod:A");
  	  new Task("01 (A) +Health +AH Passeggiata after every meal -- 15m * day:F tod:A\n");
  }

  @Test(expected=IllegalArgumentException.class)
  public void parsingEmptyLines1() { new Task(""); }

  @Test(expected=IllegalArgumentException.class)
  public void parsingEmptyLines2() { new Task("       "); }

  @Test(expected=IllegalArgumentException.class)
  public void parsingEmptyLines3() { new Task("\t"); }
  
  @Test(expected=IllegalArgumentException.class)
  public void parsingInvalidLines1() { new Task("41  (A) foo"); }
  
  @Test(expected=IllegalArgumentException.class)
  public void parsingInvalidLines2() { new Task("03 (A)foo"); }
  
  @Test(expected=IllegalArgumentException.class)
  public void parsingInvalidLines3() { new Task("12 X (A) foo"); }
  
  @Test(expected=IllegalArgumentException.class)
  public void parsingInvalidDayLines1() { new Task("12 (A) day:Blarg foo"); }
  
  @Test(expected=IllegalArgumentException.class)
  public void parsingInvalidDayLines2() { new Task("12 (A) day:M foo day:Tu"); }
  
  @Test(expected=IllegalArgumentException.class)
  public void parsingInvalidTimeOfDayLines1() { new Task("12 (A) foo tod:X"); }
  
  @Test(expected=IllegalArgumentException.class)
  public void parsingInvalidTimeOfDayLines2() { new Task("12 (A) tod:A foo tod:M"); }
  
  /**
   * An order-independent comparison. If Set.equals doesn't work, can use guavas sets.difference
   */
  private void assertSetEquals(HashSet<String> left, Set<String> right)
  {
    assertTrue("Sets not equal: " + left + " != " + right, left.size() == right.size() && left.containsAll(right));
  }
}
