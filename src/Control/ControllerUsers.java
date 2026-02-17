/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.UsersDao;
import Vista.Register;
import Modelo.Users;
import Vista.Login;
import javax.swing.JOptionPane;

/**
 *
 * @author Personal
 */
public class ControllerUsers {

    private UsersDao usDao;
    private Register visRegister;
    private Login visLog;

    public ControllerUsers(UsersDao usDao, Register visRegister, Login visLog) {
        this.usDao = usDao;
        this.visRegister = visRegister;
        this.visLog = visLog;
        this.visRegister.getBtnRegister().addActionListener(evento -> addUser());
        this.visRegister.getBtnBack().addActionListener(evento -> back());
    }

    public void back() {
        visRegister.setVisible(false);
        visLog.setVisible(true);
    }

    public void addUser() {

        String ced = visRegister.getTxtCed().getText();
        String name = visRegister.getTxtName().getText();
        String user = visRegister.getTxtUser().getText();
        String password = visRegister.getTxtPassword().getText();
        String mail = visRegister.getTxtMail().getText();
        int phone = Integer.parseInt(visRegister.getTxtPhone().getText());
        usDao.addUser(new Users(user, password, mail, phone, ced, name));
        JOptionPane.showMessageDialog(null, "Añadido");

        visRegister.setVisible(false);
        visLog.setVisible(true);

    }

}
