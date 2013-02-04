package edu.emory.library.utils;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.AbstractDocument;

/**
 * Extend JTextField to only allow double values.
  */
public class DoubleField extends JTextField {

  public DoubleField() {
    super();
  }

  public DoubleField(int cols) {
    super(cols);
  }

  /**
   * Retrieve the contents of this field as a double.
   */
  public Double getDouble()
  {
    final String text = getText();
    if (text == null || text.length() == 0)
    {
      return 0.0;
    }
    return Double.parseDouble(text);
  }

  /**
   * Set the contents of this field to the passed double.
   *
   * @param value The new value for this field.
   */
  public void setDouble(Double value)
  {
    setText(String.valueOf(value));
  }

  /*
  * Set contents of the field via string.
  */
  public void setValue(String value) {
    this.setDouble(Double.parseDouble(value));
  }

  /**
   * Create a new document model for this control that only accepts
   * double values.
   *
   * @return  The new document model.
   */
  protected Document createDefaultModel() {
    return new DoubleDocument();
  }

  /**
   * This document only accepts inserted values if the
   *  document content is a valid double.
   */
  static class DoubleDocument extends PlainDocument {
    public void insertString(int offs, String str, AttributeSet a)
      throws BadLocationException {
      if (str != null) {
        try {
          // insert the string
          super.insertString(offs, str, a);
          // check the whole value
          Double.parseDouble(this.getText(0, this.getLength()));
        } catch (NumberFormatException ex) {
          Toolkit.getDefaultToolkit().beep();
          // remove inserted content if it is not a valid Double
          this.remove(offs, str.length());
        }

      }
    }
  }

}
