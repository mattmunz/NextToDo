package mattmunz.todo;

import static org.junit.Assert.assertEquals;
import mattmunz.todo.cli.ListDisplayFilter;

import org.junit.Test;

public class FinalFilterTest
{
  @Test
  public void addPadding()
  {
    ListDisplayFilter filter = new ListDisplayFilter();
    
    assertEquals("foo", filter.addPadding("foo", 3));
    assertEquals("foo  ", filter.addPadding("foo", 5));
    assertEquals("foo    ", filter.addPadding("foo", 7));
  }
}
