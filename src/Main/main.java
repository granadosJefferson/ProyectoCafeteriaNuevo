/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;

import Controlador.ControllerUsers;
import Modelo.UsersDao;
import Vista.Login;
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
       new ControllerUsers(usDao,rg,lg);
       rg.setVisible(true);
    }
}
