/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Control;

import Modelo.UsersDao;
import Vista.Register;
import Modelo.Users;
import Vista.Login;
import Vista.Mensajes;
import Vista.Principal;

/**
 *
 * @author Personal
 */
public class ControllerLogin {

    private UsersDao usDao;
    private Register visRegister;
    private Login visLog;
    private Mensajes visMsg;
    private Principal visPrin;

    public ControllerLogin(UsersDao usDao, Register visRegister, Login visLog, Mensajes visMsg, Principal visPrin) {
        this.usDao = usDao;
        this.visRegister = visRegister;
        this.visLog = visLog;
        this.visMsg = visMsg;
        this.visPrin = visPrin;
        this.visRegister.getBtnRegister().addActionListener(evento -> addUser());
        this.visRegister.getBtnBack().addActionListener(evento -> back());
        this.visLog.getBtnLogin().addActionListener(evento -> start());
        this.visLog.getBtnRegister().addActionListener(evento -> irRegistro());

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

        Users us = new Users(user, password, mail, phone, ced, name);

        boolean ok = usDao.addUser(us);

        if (!ok) {
            visMsg.message("La cédula o el usuario ya están en uso");
        } else {
            visMsg.message("Añadido");
            visRegister.setVisible(false);
            visLog.setVisible(true);
        }

    }

    public void irRegistro() {
        visLog.setVisible(false);
        visRegister.setVisible(true);
    }

    public void start() {
        String user = visLog.getTxtUser().getText();
        String pass = visLog.getTxtPassword().getText();

        boolean exist = usDao.validUser(user, pass);
        String msg = "Bienvenido";

        if (exist) {
            visMsg.message(msg);
            visPrin.setVisible(true);
        } else {
            msg = "El usuario o la contraseña es incorrecta";
            visMsg.message(msg);
        }

    }
}
