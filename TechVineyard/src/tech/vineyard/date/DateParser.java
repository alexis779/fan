package tech.vineyard.date;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

public class DateParser {
  private static final Logger LOGGER = Logger.getLogger(DateParser.class);
  
  private Parser parser = new Parser();

  private void parse(String dateString) {
    LOGGER.info("Parsing: " + dateString);
    
    List<DateGroup> groups = parser.parse(dateString);
    for (DateGroup dateGroup: groups) {
      List<Date> dates = dateGroup.getDates();
      for (Date date: dates) {
        LOGGER.info("Parsed date: " + date);
      }
    }
  }
  
  public static void main(String[] args) {
    String dateString = "Posted on Fri Mar 11 2011 | 9:38";
    String notDateString = "090 is a non date format.";
    
    DateParser dateParser = new DateParser();
    dateParser.parse(dateString);
    dateParser.parse(notDateString);
  }
}
