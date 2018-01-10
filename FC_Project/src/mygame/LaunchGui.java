/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;

/**
 *
 * @author phnk
 */
public class LaunchGui extends JFrame {

    //Initiate the variables needed for the JFrame
    private SpringLayout springLayout;
    JTextField hostField;
    JTextField portField;
    JButton btnNewButton;
    JLabel hostLabel = new JLabel("Host: ");
    JLabel portLabel = new JLabel("Port: ");
    JTextArea textBox;
    Border border = BorderFactory.createLineBorder(Color.BLACK);

    public LaunchGui() {
        construct();
    }

    private void construct() {
        btnNewButton = new JButton("Connect");
        portField = new JTextField();
        hostField = new JTextField();
        textBox = new JTextArea();

        //set layout for the JFrame
        springLayout = new SpringLayout();
        getContentPane().setLayout(springLayout);

        //Button Constraints
        springLayout.putConstraint(SpringLayout.NORTH, btnNewButton, 6, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, btnNewButton, -10, SpringLayout.EAST, getContentPane());
        springLayout.putConstraint(SpringLayout.SOUTH, btnNewButton, -6, SpringLayout.NORTH, textBox);

        //HostLabel constraints
        springLayout.putConstraint(SpringLayout.NORTH, hostLabel, 6, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, hostLabel, 6, SpringLayout.WEST, getContentPane());

        //PortLabel constraints
        springLayout.putConstraint(SpringLayout.WEST, portLabel, 6, SpringLayout.WEST, getContentPane());
        springLayout.putConstraint(SpringLayout.NORTH, portLabel, 10, SpringLayout.SOUTH, hostLabel);

        //HostField constraints
        hostField.setEditable(true);
        springLayout.putConstraint(SpringLayout.NORTH, hostField, 6, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, hostField, 6, SpringLayout.EAST, hostLabel);
        springLayout.putConstraint(SpringLayout.EAST, hostField, -6, SpringLayout.WEST, btnNewButton);

        //PortField constraints
        portField.setEditable(true);
        springLayout.putConstraint(SpringLayout.NORTH, portField, 6, SpringLayout.SOUTH, hostField);
        springLayout.putConstraint(SpringLayout.WEST, portField, 6, SpringLayout.EAST, portLabel);
        springLayout.putConstraint(SpringLayout.EAST, portField, -6, SpringLayout.WEST, btnNewButton);

        //Text Area constraints
        textBox.setEditable(false);
        springLayout.putConstraint(SpringLayout.NORTH, textBox, 6, SpringLayout.SOUTH, portField);
        springLayout.putConstraint(SpringLayout.SOUTH, textBox, -6, SpringLayout.SOUTH, getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, textBox, 6, SpringLayout.WEST, getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, textBox, -6, SpringLayout.EAST, getContentPane());

        textBox.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        getContentPane().add(hostField);
        getContentPane().add(portField);
        getContentPane().add(hostLabel);
        getContentPane().add(portLabel);
        getContentPane().add(btnNewButton);
        getContentPane().add(textBox);

        hostField.setColumns(10);
        portField.setColumns(10);

        //JFrame configurations
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setSize(300, 300);
        this.setVisible(true);

        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (hostField.getText().length() > 0 && portField.getText().length() > 0) {
                    textBox.append("Connecting to:" + hostField.getText() + ":" + portField.getText() + "\n");
                    Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            GameMessage.initSerializer();
                            new TheClient(hostField.getText(), Integer.valueOf(portField.getText())).start();
                        }
                    });

                    t.start();

                } else {
                    textBox.append("plz gief actual s0rv3r and p0rt\n");
                }
            }
        });

    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("1");
    }

    public static void main(String[] args) {
        new LaunchGui();

    }
}
