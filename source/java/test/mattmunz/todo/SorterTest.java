package mattmunz.todo;

import static java.util.Arrays.asList;  
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.SATURDAY;

import java.time.DayOfWeek;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mattmunz.todo.cli.Sorter;

import org.junit.Test;

public class SorterTest
{
  @Test
  public void sortTestFileItems()
  {
    List<String> inputTasks 
      = asList("04 x 2016-03-11 2016-03-10 Buy cat litter",
               "03 (A) Merge in all todos into this file @Mab +Productivity d:Th",
               "02 (B) 2016-03-05 Get lily photos on phone @Mab +Productivity",
               "09 (A) Test 4 on:M +Health @Home",
               "13 (A) Test 8 on:A +Health @Mab",
               "11 (A) Test 6 on:M +Productivity @Mab",
               "06 (B) Test 1 on:Mon +Productivity @Mab",
               "07 (B) Test 2 on:Tu +Productivity @Mab",
               "10 (B) Test 5 on:M +Productivity @Home",
               "12 (B) Test 7 on:A +Productivity @Mab",
               "08 (C) Test 3 on:Tu +Productivity @Prospero",
               "05 (C) 2016-03-11 Get more text news readers for phone",
               "01 (D) 2016-03-05 Test");

    List<String> expectedTasks 
      = asList("05 (C) 2016-03-11 Get more text news readers for phone",
               "01 (D) 2016-03-05 Test",
               "09 (A) Test 4 on:M +Health @Home",
               "10 (B) Test 5 on:M +Productivity @Home",
               "13 (A) Test 8 on:A +Health @Mab",
               "03 (A) Merge in all todos into this file @Mab +Productivity d:Th",
               "11 (A) Test 6 on:M +Productivity @Mab",
               "02 (B) 2016-03-05 Get lily photos on phone @Mab +Productivity",
               "06 (B) Test 1 on:Mon +Productivity @Mab",
               "07 (B) Test 2 on:Tu +Productivity @Mab",
               "12 (B) Test 7 on:A +Productivity @Mab",
               "08 (C) Test 3 on:Tu +Productivity @Prospero",
               "04 x 2016-03-11 2016-03-10 Buy cat litter");

    assertSortedTasksEqual(expectedTasks, inputTasks, MONDAY);
  }

  @Test
  public void sortByContext()
  {
    assertSortedTasksEqual(asList("03 (C) Foo @Mab", "02 (D) Bar @Mab", "01 (A) Baz @Prospero"), 
                           asList("01 (A) Baz @Prospero", "02 (D) Bar @Mab", "03 (C) Foo @Mab"),
                           MONDAY);
  }

  @Test
  public void sortByDay()
  {
    List<String> inputTasks 
      = asList("01 (C) Baz day:M", "02 (B) Bar day:W", "03 (B) Foo day:Sa");
    
    List<String> expectedTasks1 
      = asList("01 (C) Baz day:M", "02 (B) Bar day:W", "03 (B) Foo day:Sa");

    assertSortedTasksEqual(expectedTasks1, inputTasks, MONDAY);
    
    List<String> expectedTasks2 
      = asList("03 (B) Foo day:Sa", "01 (C) Baz day:M", "02 (B) Bar day:W");

    assertSortedTasksEqual(expectedTasks2, inputTasks, THURSDAY);
  }
  
  @Test
  public void getDayNumber()
  {
    Sorter sorter = new Sorter(THURSDAY);
    
    assertEquals(1, sorter.getDayNumber(THURSDAY));
    assertEquals(2, sorter.getDayNumber(FRIDAY));
    assertEquals(3, sorter.getDayNumber(SATURDAY));
    assertEquals(5, sorter.getDayNumber(MONDAY));
    assertEquals(7, sorter.getDayNumber(WEDNESDAY));
  }

  private <T> void assertStreamsEqual(Stream<T> expected, List<T> actual)
  {
    Iterator<T> expectedIterator = expected.iterator();
    Iterator<T> actualIterator = actual.iterator();

    while (expectedIterator.hasNext() && actualIterator.hasNext())
    {
      assertEquals(expectedIterator.next(), actualIterator.next());
    }
    
    assertFalse(expectedIterator.hasNext() || actualIterator.hasNext());
  }

	private void assertSortedTasksEqual(List<String> expectedTaskLines, 
	                                    List<String> inputTaskLines, DayOfWeek sortDay)
	{
		Stream<Task> inputTasks = inputTaskLines.stream().map(Task::new);
		Stream<Task> expectedTasks = expectedTaskLines.stream().map(Task::new);

    List<Task> sortedTasks = new Sorter(sortDay).sort(inputTasks).collect(Collectors.toList());
		
		System.out.println("Sorted tasks: ");
		sortedTasks.stream().map(Task::getLineText).forEachOrdered(System.out::println);
		
		assertStreamsEqual(expectedTasks, sortedTasks);
	}
}
