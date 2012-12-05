package edu.emory.library.namedropper.ui;

// swing imports
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JScrollPane;
// awt imports
import java.awt.Color;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DBPediaPanel extends JPanel {

    private JScrollPane scrollPane;
    private JPanel scrollContent;
    private JPanel fillerPanel;
    private JButton btnAcceptAll;
    private int currentRow = 0;

    public DBPediaPanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);

        this.add(getScrollPane(), BorderLayout.CENTER);

        btnAcceptAll = new JButton("Add Row");
        btnAcceptAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addStringToList("row " + currentRow);
                System.out.println("test");
            }
        });
        this.add(btnAcceptAll, BorderLayout.SOUTH);
    }

    private JScrollPane getScrollPane() {
        scrollContent = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        //gbc.gridx = gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fillerPanel = new JPanel();
        fillerPanel.setBackground(Color.GREEN);
        scrollContent.add(fillerPanel, gbc);

        scrollPane = new JScrollPane(scrollContent);
        return scrollPane;
    }

    private GridBagConstraints createGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.anchor = (x == 0) ? GridBagConstraints.FIRST_LINE_START : GridBagConstraints.FIRST_LINE_START;
        //gbc.fill = (x == 0) ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;

        gbc.insets = (y == 0) ? new Insets(3, 5, 3, 5) : new Insets(0, 5, 3, 5);
        gbc.weightx = (x == 0) ? 0.1 : 1.0;
        gbc.weighty = 0.0;

        return gbc;
    }

    public void addStringToList(String str) {
        System.out.println("in addStringToList");
        System.out.println("attempting to add " + str + " to the list");
        scrollContent.remove(fillerPanel);

        GridBagConstraints gbc = createGbc(0, currentRow);
        scrollContent.add(new JCheckBox(), gbc);

        gbc = createGbc(1, currentRow);
        scrollContent.add(new JTextField(str), gbc);

        gbc = createGbc(0, currentRow+1);
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        scrollContent.add(fillerPanel, gbc);
        // revalidate and repaint the scrollContent to ensure it gets added to the view

        scrollContent.layout();
        scrollContent.revalidate();
        scrollContent.repaint();

        currentRow++;
    }

}