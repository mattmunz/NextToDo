package mattmunz.todo.cli;

import static java.lang.System.out;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;  
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;

import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Range;

import mattmunz.cli.commandline.CommandLine;
import mattmunz.cli.commandline.Parameter;
import mattmunz.cli.commandline.Parser;
import mattmunz.time.DayHelper;
import mattmunz.time.TimeOfDay;
import mattmunz.todo.Task;

public class TaskMover
{
  public static void main(String[] arguments) throws IOException
  {
    new TaskMover(arguments).moveTasks();
  }

  private final DayOfWeek day;
  private final TimeOfDay timeOfDay;
  private final Set<Integer> lineNumbers;
  private final Path todoTxtFile;
  private final Path archiveDirectory;

  private TaskMover(String[] arguments) throws IOException
  {
    CommandLine commandLine = new Parser(emptyList(), 3, 50).parse(arguments);
    
    todoTxtFile = commandLine.getParameter(0, Path.class).get().getValue();
    
    if (!exists(todoTxtFile)) 
    {
      throw new IllegalArgumentException("ToDo File doesn't extist: " + todoTxtFile); 
    }
    
    archiveDirectory = todoTxtFile.getParent().resolve("archive");
    
    createDirectories(archiveDirectory);
    
    day = commandLine.getParameter(1, DayOfWeek.class).get().getValue();
    timeOfDay = commandLine.getParameter(2, TimeOfDay.class).get().getValue();
    lineNumbers 
      = commandLine.getParameters(Range.greaterThan(2), Integer.class).stream()
                   .map(Parameter::getValue).collect(toSet());

    if (lineNumbers.isEmpty()) 
    { 
      throw new IllegalArgumentException("No line numbers given"); 
    }
    
    if (lineNumbers.contains(new Integer(0)))
    {
      throw new IllegalArgumentException("Task id 0 not allowed: " + lineNumbers);
    }
  }

  /**
   * 1) Read in all lines to memory, closing file
   * 2) Map those lines, preserving most but modifying only selected lines to the chosen day/times (see mvd design)
   * 3) Write all lines back to the original file (Overwrite)
   * 4) And write all lines into a backup file
   * 
   * TODO Unit test this! 
   */
  private void moveTasks() throws IOException
  {
    List<String> newLines = moveMatchingLines(readAllLines(todoTxtFile));
    
    write(todoTxtFile, newLines);
    
    String archiveFileName 
      = "" + todoTxtFile.getFileName() + "." + currentTimeMillis() + ".old";

    // TODO Perhaps it would be better to archive the original file here...
    write(archiveDirectory.resolve(archiveFileName), newLines);
    
    out.println("" + lineNumbers.size() + " task(s) were moved to " + day + " " + timeOfDay 
                + ": " + lineNumbers + ".");
  }

  private List<String> moveMatchingLines(List<String> lines)
  {
    List<String> numberedLines = new ArrayList<String>();

    int i = 1;

		for (String line : lines) 
		{
			if (!line.trim().isEmpty()) { numberedLines.add("" + i + " "  + line); }
			
			i++;
		}
    
    List<String> newLines 
      = numberedLines.stream().map(Task::new).map(this::getNewTask)
                              .map(Task::getLineTextWithoutIdentifier).collect(toList());

    if (lines.size() != newLines.size())
    {
      throw new IllegalStateException("Input/output mismatch. Input lines size: " + lines.size() 
                                      + "; new lines size: " + newLines.size());
    }
    
    validateLines(newLines);
    
    return newLines;
  }

  private void validateLines(List<String> newLines)
  {
    for (String line : newLines)
    {
      if (line.contains("\n"))
    	  {
    		  throw new IllegalStateException("Line contains newline!: [" + line + "].");
    	  }
    	
    	  if (line.trim().isEmpty())
    	  {
    	    throw new IllegalStateException("Line is empty!: [" + line + "].");
    	  }
    }
  }
  
  /**
   * @return a new task with day and time of day tasks updated, as needed
   */
  private Task getNewTask(Task task)
  {
    if (!lineNumbers.contains(new Integer(task.getIdentifier()))) { return task; }
    
    String dayFieldRegex = " day:[\\p{Alpha}_]+";
    String todFieldRegex = " tod:[\\p{Alpha}_]+";

    DayHelper dayHelper = new DayHelper();
    
		String newLineText 
			= task.getLineText().replaceAll(dayFieldRegex, " day:" + dayHelper.getIdentifier(day))
			                    .replaceAll(todFieldRegex, " tod:" + timeOfDay.getIdentifier());

		
		return new Task(newLineText, task.getIdentifier(), task.isCompleted(), 
    								  task.getPriority(), task.getColorCode(), Optional.of(day), 
    								  Optional.of(timeOfDay), task.getProjects(), task.getContexts(), 
    								  task.getMessage());
  }
}