/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;

import Control.ControllerLogin;
import Modelo.loginDao;
import Vista.Login;
import Vista.Register;

/**
 *
 * @author Personal
 */
public class main {
    public static void main(String[] args) {
       loginDao usDao = new loginDao(); 
       Register rg = new Register();
       Login lg = new Login();
       new ControllerLogin(usDao,rg,lg);
       rg.setVisible(true);
    }
}
