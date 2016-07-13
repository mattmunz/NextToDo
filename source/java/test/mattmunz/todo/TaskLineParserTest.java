package mattmunz.todo;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;

public class TaskLineParserTest
{
  @Test
  public void parseFieldValues()
  {
    TaskLineParser parser = new TaskLineParser("01 Foo");

    Set<TodoLineField> values4 = parser.parseFieldValues("01 +Fun Foo");

    assertEquals(1, values4.size());
    assertEquals("Fun", values4.iterator().next().getValue());

    Set<TodoLineField> values6 = parser.parseFieldValues("01 @Computer Foo");

    assertEquals(1, values6.size());
    assertEquals("Computer", values6.iterator().next().getValue());

    Set<TodoLineField> values5 = parser.parseFieldValues("01 @Computer +Fun Foo");
  
    assertEquals(2, values5.size());

    Set<TodoLineField> values3 = parser.parseFieldValues("01 @Computer +Fun Foo @Home +Health");
  
    assertEquals(4, values3.size());

    Set<TodoLineField> values2 
      = parser.parseFieldValues("01 (C) @Computer +Fun Play mind-enhanicng computer games @Home +Health");
  
    assertEquals(4, values2.size());

    Set<TodoLineField> values1 
      = parser.parseFieldValues("01 (C) @Computer +Fun Play mind-enhanicng computer games @Home +Health\n");
    
    assertEquals(4, values1.size());
  }
}
