package mattmunz.todo;

import static java.util.logging.Logger.getLogger;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static mattmunz.todo.TaskLineParser.RegexGroup.COLOR_CODE;
import static mattmunz.todo.TaskLineParser.RegexGroup.COMPLETED;
import static mattmunz.todo.TaskLineParser.RegexGroup.IDENTIFIER;
import static mattmunz.todo.TaskLineParser.RegexGroup.PRIORITY;
import static mattmunz.todo.TodoLineFieldType.CONTEXT;
import static mattmunz.todo.TodoLineFieldType.DAY;
import static mattmunz.todo.TodoLineFieldType.PROJECT;
import static mattmunz.todo.TodoLineFieldType.TIME_OF_DAY;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import mattmunz.time.DayHelper;
import mattmunz.time.TimeOfDay;

/**
 * TODO This class is a little big. Extract out the generic regex group / regex code, 
 * leaving only the task-specifics here.
 */
class TaskLineParser
{
  enum RegexGroup 
  {
    COLOR_CODE("ColorCode"), IDENTIFIER("Identifier"), COMPLETED("Completed"), 
    PRIORITY("Priority");
    
    private final String name;
    
    private RegexGroup(String name) { this.name = name; }
    
    private String getName() { return name; }
  }

  private static final String FIELD_REGEX 
    = "(?<FieldLabel>day:|tod:|\\+|\\@)(?<FieldValue>[\\p{Alpha}_]+)";

  private static final Logger logger = getLogger(TaskLineParser.class.getName());
  
  private final String lineText;
  private final Matcher frontItemsMatcher;
  private final Map<TodoLineFieldType, Set<TodoLineField>> fieldListPartitions;
  private final DayHelper dayHelper = new DayHelper();

  TaskLineParser(String lineText)
  {
    this.lineText = lineText;
    
    logger.fine("Parsing line: [" + lineText + "]");
    
    if (lineText.isEmpty()) 
    {
      throw new IllegalArgumentException("Line is empty or starts with whitespace: [" + lineText + "]."); 
    }
    
    frontItemsMatcher = getFrontItemsMatcher(lineText);
    
    fieldListPartitions 
      = parseFieldValues(lineText).stream().collect(groupingBy(TodoLineField::getType, toSet()));
  }

  /**
   * Visible for testing only.
   */
  Set<TodoLineField> parseFieldValues(String lineText) 
  {
    Matcher fieldMatcher = getMatcher(" " + FIELD_REGEX, lineText);
    
    Set<TodoLineField> values = new HashSet<TodoLineField>();
    
    while (fieldMatcher.find()) 
    { 
      String label = fieldMatcher.group("FieldLabel");
      String value = fieldMatcher.group("FieldValue");
      
      values.add(new TodoLineField(label, value)); 
    }
    
    return values;
  }

  String getLineText() { return lineText; }

  String getIdentifier() { return getGroupText(IDENTIFIER); }

  boolean getIsCompleted() { return " x".equals(getGroupText(COMPLETED)); }

  Optional<String> getPriority() { return getOptionalGroupText(PRIORITY); }

  Optional<String> getColorCode() { return getOptionalGroupText(COLOR_CODE); }

  Optional<DayOfWeek> getDay() { return getSingleField(DAY, "day", dayHelper::getDayOfWeek); }

  Optional<TimeOfDay> getTimeOfDay()
  {
    return getSingleField(TIME_OF_DAY, "tod", TimeOfDay::forIdentifier);
  }

  Set<String> getProjects() { return getFieldValues(PROJECT); }

  Set<String> getContexts() { return getFieldValues(CONTEXT); }

  String getMessage() 
  {
    return lineText.replaceAll(getPreambleRegex(), "").replaceAll(FIELD_REGEX, "").trim(); 
  }

  private Optional<String> getOptionalGroupText(RegexGroup group)
  {
    return Optional.ofNullable(getGroupText(group));
  }

  /**
   * TODO This regex code is still pretty gnarly to read, and work with. A real problem!
   *      Should probably be redesigned (again) to use multiple passes instead of a single 
   *      pass to parse the front items!
   */
  private Matcher getFrontItemsMatcher(String lineText)
  {
    // TODO The color code patterns are similar and can be refactored together
    String colorResetCodeGroup = "(\\u001B\\[\\d+m)";
    
    String firstBodyWord = "[\\S&&[^\\(X]]\\S*";
    String body = firstBodyWord + ".*";

    // TODO instead of this \\n?\\z business just trim the line before parsing it
    String frontItemsRegex = getPreambleRegex() + body + colorResetCodeGroup + "?" + "\\n?\\z";
    
    Matcher frontItemsMatcher = getMatcher(frontItemsRegex, lineText);

    if (!frontItemsMatcher.matches()) 
    {
      String message = "The line is invalid: [" + lineText + "]. Length: [" + lineText.length() + "].";
      throw new IllegalArgumentException(message);
    }
    
    return frontItemsMatcher;
  }

  // TODO Store this in state instead of recomputing it every time.
  // TODO Document
  // TODO Incorporate the timestamp as well
  private String getPreambleRegex()
  {
    String priorityMarkGroup 
      = "(\\(" + createGroupExpression(PRIORITY, "\\p{Upper}") + "\\))";
    String indexCompletedMarkAndPriority 
      = createGroupExpression(IDENTIFIER, "\\d+") + createGroupExpression(COMPLETED, " x") 
        + "?( " + priorityMarkGroup + ")?";
    String colorCodeGroup = createGroupExpression(COLOR_CODE, "\\u001B\\[\\d+;\\d+m");
    
    return "^" + colorCodeGroup + "?" + indexCompletedMarkAndPriority + " {1}";
  }

  private String getGroupText(RegexGroup group)
  {
    return getGroupText(group, frontItemsMatcher);
  }

  private String getGroupText(RegexGroup group, Matcher matcher)
  {
    return matcher.group(group.getName());
  }

  private String createGroupExpression(RegexGroup group, String expression)
  {
    return "(?<" + group.getName() + ">" + expression + ")";
  }

  private <V> Optional<V> 
    getSingleField(TodoLineFieldType fieldType, String fieldName, Function<String, V> fieldValueParser)
  {
    return getSingleField(fieldListPartitions, fieldType, fieldName, fieldValueParser, lineText);
  }

  private <V> Optional<V> 
    getSingleField(Map<TodoLineFieldType, Set<TodoLineField>> fieldListPartitions,
                   TodoLineFieldType fieldType, String fieldName, 
                   Function<String, V> fieldValueParser, String lineText)
  {
    Set<TodoLineField> matchingFields = fieldListPartitions.get(fieldType);
    
    if (matchingFields == null || matchingFields.isEmpty()) { return Optional.empty(); }
      
    if (matchingFields.size() > 1) 
    {
      throw new IllegalArgumentException("Too many " + fieldName + "s found in line: " + lineText); 
    }
      
    return Optional.of(fieldValueParser.apply(matchingFields.iterator().next().getValue()));
  }

  private Set<String> getFieldValues(TodoLineFieldType fieldType)
  {
    return getFieldValues(fieldListPartitions, fieldType);
  }

  private Set<String> 
    getFieldValues(Map<TodoLineFieldType, Set<TodoLineField>> fieldListPartitions,
                   TodoLineFieldType fieldType)
  {
    return Optional.ofNullable(fieldListPartitions.get(fieldType))
                         .map(this::getValues).orElse(Collections.emptySet());
  }

  private Set<String> getValues(Set<TodoLineField> fields)
  {
    return fields.stream().map(TodoLineField::getValue).collect(toSet());
  }

  private Matcher getMatcher(String regex, String text)
  {
    return compile(regex).matcher(text);
  }
}
