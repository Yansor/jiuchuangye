package com.fsy.task.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.fsy.task.APIController;
import com.fsy.task.domain.ImportUser;
import com.fsy.task.util.UserImportUtil;
import org.htmlparser.util.ParserException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;

/**
 *
 * @author vincent
 */
public class Main extends JFrame {

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        this.setTitle("就创业自动看课软件V1.0 --看视频 , 写测评");
        try {
            //make ui more beaut
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        jButton2 = new JButton();
        jButton3 = new JButton();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        jButton2.setText("导入学生对应账号");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    jButton2ActionPerformed(evt);
                } catch (IOException e) {
                    JOptionPane.showOptionDialog( Main.this , "导入失败 " + e.getMessage() , "通知" , OK_CANCEL_OPTION , QUESTION_MESSAGE , null , null , null);
                    //e.printStackTrace();
                }
            }
        });

        jButton3.setText("开始");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    jButton3ActionPerformed(evt);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(52, 52, 52)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jButton3)
                                        //.addComponent(importAnswerB)
                                        .addComponent(jButton2)
                                        //.addComponent(jButton1))

                               //
        ).addContainerGap(174, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                //.addComponent(jButton1)
                                .addGap(18, 18, 18)
                                //.addComponent(importAnswerB)
                                .addGap(18 ,18 ,18)
                                .addComponent(jButton2)
                                .addGap(54, 54, 54)
                                .addComponent(jButton3)
                                .addContainerGap(123, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    private void jButton3ActionPerformed(ActionEvent evt) throws IOException {

        validateParam();

        startDo();
    }

    private void validateParam() throws IOException {
        if(APIController.userList == null || APIController.userList.size() ==0 ){
            JOptionPane.showOptionDialog( Main.this , "学生账户为空列表 ， 请导入"  , "通知" , OK_CANCEL_OPTION , QUESTION_MESSAGE , null , null , null);
            return;
        }
    }

    private void startDo() {
        for(ImportUser user : APIController.userList){
            new APIController(user);
        }
    }

    private void jButton2ActionPerformed(ActionEvent evt) throws IOException {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.showOpenDialog(this);
        if(jFileChooser.getSelectedFile() != null){
            APIController.userList = UserImportUtil.getImportUserList(jFileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    private JButton jButton2;
    private JButton jButton3;

    // End of variables declaration
}

