package edu.emory.library.utils;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


/**
 * Extend JTextField to only allow integer values.
 *
 * Inspired by
 * http://www.java2s.com/Code/Java/Swing-JFC/extendsJTextFieldtocreateintegerJTextField.htm
 */
public class IntegerField extends JTextField {

  public IntegerField() { super(); }

  public IntegerField(int cols) { super(cols); }

  /**
   * Retrieve the contents of this field as an integer.
   */
  public int getInt() {
    final String text = getText();
    if (text == null || text.length() == 0) {
      return 0;
    }
    return Integer.parseInt(text);
  }

  /**
   * Set the value of this field.
   */
  public void setInt(int value) {
    setText(String.valueOf(value));
  }

  /*
  * Set contents of the field via string,
  * ensuring it is a valid integer.
  */
  public void setValue(String value) {
    this.setInt(Integer.parseInt(value));
  }

  /**
   * Create a new document model for this control that only accepts
   * integral values.
   *
   * @return  The new document model.
   */
  protected Document createDefaultModel() {
    return new IntegerDocument();
  }

  /**
   * This document only allows integral values to be added to it.
   */
  static class IntegerDocument extends PlainDocument {
    public void insertString(int offs, String str, AttributeSet a)
      throws BadLocationException {
      if (str != null) {
        try {
          Integer.decode(str);
          super.insertString(offs, str, a);
        } catch (NumberFormatException ex) {
          Toolkit.getDefaultToolkit().beep();
        }
      }
    }
  }

}
