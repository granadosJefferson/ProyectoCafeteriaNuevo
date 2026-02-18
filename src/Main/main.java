/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;

import Control.ControllerLogin;
import Modelo.UsersDao;
import Vista.Login;
import Vista.Mensajes;
import Vista.Principal;
import Vista.Register;

/**
 *
 * @author Personal
 */
public class main {
    public static void main(String[] args) {
       UsersDao usDao = new UsersDao(); 
       Register rg = new Register();
       Login lg = new Login();
       Mensajes ms = new Mensajes();
       Principal pr = new Principal();
       new ControllerLogin(usDao,rg,lg,ms,pr);
       lg.setVisible(true);
    }
}
