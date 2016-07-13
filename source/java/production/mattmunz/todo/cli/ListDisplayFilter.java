package mattmunz.todo.cli;

import static java.util.Arrays.asList; 
import static java.util.stream.Stream.concat;
import static java.util.stream.Collectors.toList;
import static com.google.common.base.Strings.repeat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mattmunz.lang.SystemHelper;
import mattmunz.todo.TaskTableRow;

public class ListDisplayFilter
{
  private static final String COLOR_RESET_TOKEN = "\u001B[0m";
  
  public static void main(String[] arguments) 
  { 
    new ListDisplayFilter().filterSystemInput();
  }

  public String addPadding(String cell, int minimumWidth)
  {
    return cell + getPadding(cell, minimumWidth);
  }
  
  private String getPadding(String cell, int minimumWidth)
  {
    int cellWidth = cell.length();
    return cellWidth >= minimumWidth ? "" : repeat(" ", minimumWidth - cellWidth);
  }

  private void filterSystemInput()
  {
    TaskTableRow headerRow 
      = new TaskTableRow(Optional.empty(), "#", "P", "D/T", "Contexts", "Projects", 
                         "Message");
    
    Stream<TaskTableRow> taskRows 
      = new SystemHelper().readLinesFromSystemIn(1000000).map(TaskTableRow::new);
  
    List<TaskTableRow> rows = concat(asList(headerRow).stream(), taskRows).collect(toList());
    
    List<Integer> minimumWidths = asList(0, 0, 0, 0, 0, 0);
    rows.stream().forEach(row -> { updateMinimumWidths(minimumWidths, row); });
    
    rows.stream().map(row -> { return addPadding(minimumWidths, row); })
                 .forEach(this::printRow);
  }

  // TODO This is inefficient (in iteration)
  private TaskTableRow addPadding(List<Integer> minimumWidths, TaskTableRow row)
  {
    List<String> cells = row.getCells();
    
    if (minimumWidths.size() < cells.size()) 
    {
      throw new IllegalArgumentException("TaskTableRow too big!");
    }
    
    List<String> resultCells = new ArrayList<String>(cells.size());
    
    int i = 0;
    
    for (String cell : cells)
    {
      Integer minimumWidth = minimumWidths.get(i);
      
      resultCells.add(addPadding(cell, minimumWidth)); 
      
      i++;
    }
    
    return new TaskTableRow(row.getColorCode(), resultCells);
  }

  // TODO Uses side effects :( Also it is inefficient (in iteration)
  private void updateMinimumWidths(List<Integer> maximumWidths, TaskTableRow row)
  {
    List<String> cells = row.getCells();

    if (maximumWidths.size() < cells.size()) 
    {
      throw new IllegalArgumentException("TaskTableRow too big!");
    }
    
    int i = 0;
    
    for (String cell : cells)
    {
      int cellWidth = cell.length();
      
      if (cellWidth > maximumWidths.get(i)) { maximumWidths.set(i, cellWidth); }
      
      i++;
    }
  }

  private void printRow(TaskTableRow row)
  {
    System.out.println(row.getColorCode().orElse("") 
                       + row.getCells().stream().collect(Collectors.joining(" ")) 
                       + COLOR_RESET_TOKEN);
  }
}
