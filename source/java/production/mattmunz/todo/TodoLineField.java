package mattmunz.todo;

import static mattmunz.todo.TodoLineFieldType.DAY; 
import static mattmunz.todo.TodoLineFieldType.TIME_OF_DAY;
import static mattmunz.todo.TodoLineFieldType.CONTEXT;
import static mattmunz.todo.TodoLineFieldType.PROJECT;

import java.util.List; 

import mattmunz.property.PropertiedObject;
import mattmunz.property.Property;
import mattmunz.property.PropertyListBuilder;

class TodoLineField implements PropertiedObject
{
  private final String value;
  private final TodoLineFieldType type;

  TodoLineField(String label, String value)
  {
    this.value = value;
    type = getType(label);
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
    return new PropertyListBuilder().add("type", type).add("value", value).build();
  }
  
  TodoLineFieldType getType() { return type; }

  String getValue() { return value; }
  
  private TodoLineFieldType getType(String label)
  {
    // TODO Replace with map lookup
    switch (label)
    {
      case "day:": return DAY;
      case "tod:": return TIME_OF_DAY;
      case "+": return PROJECT;
      case "@": return CONTEXT;
      default: throw new IllegalArgumentException("Unknown field label: " + label);
    }
  }
}