package Control;

import Exceptions.InvalidEmailException;
import Exceptions.InvalidPhoneException;
import Exceptions.RequiredFieldException;
import Exceptions.RowValidationException;
import Modelo.UsersDao;
import Vista.Register;
import Modelo.Users;
import Vista.Login;
import Vista.Mensajes;
import Vista.Principal;
import Utils.SoloNumerosFilter;
import javax.swing.text.AbstractDocument;

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

        aplicarFiltrosRegistro();

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

        try {
            String ced = valor(visRegister.getTxtCed().getText());
            String name = valor(visRegister.getTxtName().getText());
            String user = valor(visRegister.getTxtUser().getText());
            String password = valor(visRegister.getTxtPassword().getText());
            String mail = valor(visRegister.getTxtMail().getText());
            String phoneStr = valor(visRegister.getTxtPhone().getText());

           
            if (ced.isEmpty()) throw new RequiredFieldException("Cédula");
            if (name.isEmpty()) throw new RequiredFieldException("Nombre");
            if (user.isEmpty()) throw new RequiredFieldException("Usuario");
            if (password.isEmpty()) throw new RequiredFieldException("Contraseña");
            if (mail.isEmpty()) throw new RequiredFieldException("Correo");
            if (phoneStr.isEmpty()) throw new RequiredFieldException("Teléfono");

           
            validarGmail(mail);
            int phone = parsePhone(phoneStr);

            Users us = new Users(user, password, mail, phone, ced, name);

            boolean ok = usDao.addUser(us);

            if (!ok) {
                visMsg.message("La cédula o el usuario ya están en uso");
            } else {
                visMsg.message("Añadido");
                visRegister.setVisible(false);
                visLog.setVisible(true);
            }

        } catch (RowValidationException ex) {
            visMsg.message(ex.getMessage());
        } catch (Exception ex) {
            visMsg.message("Error registrando usuario");
        }
    }

    public void irRegistro() {
        visLog.setVisible(false);
        visRegister.setVisible(true);
    }

    public void start() {
        String user = valor(visLog.getTxtUser().getText());
        String pass = valor(visLog.getTxtPassword().getText());

        if (user.isEmpty() || pass.isEmpty()) {
            visMsg.message("Usuario y contraseña son obligatorios");
            return;
        }

        boolean exist = usDao.validUser(user, pass);
        String msg = "Bienvenido";

        if (exist) {
            visMsg.message(msg);
            visPrin.setVisible(true);
            visLog.setVisible(false);
        } else {
            msg = "El usuario o la contraseña es incorrecta";
            visMsg.message(msg);
        }
    }

    private void aplicarFiltrosRegistro() {
        try {
            AbstractDocument docPhone = (AbstractDocument) visRegister.getTxtPhone().getDocument();
            docPhone.setDocumentFilter(new SoloNumerosFilter());
        } catch (Exception ex) {}
    }

    private String valor(String s) {
        return (s == null) ? "" : s.trim();
    }

    private void validarGmail(String mail) throws InvalidEmailException {
        String m = (mail == null) ? "" : mail.trim().toLowerCase();
        if (!m.endsWith("@gmail.com")) {
            throw new InvalidEmailException();
        }
    }

    private int parsePhone(String phoneStr) throws InvalidPhoneException {
        String p = (phoneStr == null) ? "" : phoneStr.trim();

        // por si pegan texto o llega raro
        if (!p.matches("\\d+")) {
            throw new InvalidPhoneException();
        }

        try {
            return Integer.parseInt(p);
        } catch (Exception ex) {
            
            throw new InvalidPhoneException();
        }
    }
}