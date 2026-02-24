
package Main;

import Control.ControllerLogin;
import Control.ControllerPrincipal;
import Modelo.UsersDao;
import Vista.Login;
import Vista.Mensajes;
import Vista.Principal;
import Vista.Register;




public class main {
    
    public static ControllerPrincipal controllerPrincipal;

    public static void main(String[] args) {

        UsersDao usDao = new UsersDao();
        Register rg = new Register();
        Login lg = new Login();
        Mensajes ms = new Mensajes();
        Principal pr = new Principal(); 
        
        
       controllerPrincipal = new ControllerPrincipal(pr);
      
       new ControllerLogin(usDao, rg, lg, ms, pr);

        lg.setVisible(true);
    }
}
