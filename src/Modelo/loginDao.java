/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Personal
 */
public class loginDao {

    ArrayList<Users> list;

    public loginDao() {
        list = new ArrayList<>();
    }

    public boolean addUser(Users a) {
        list.add(a);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("Users.txt",true))) {
            for (Users us : list) {
                bw.write(us.getCedula() + "," + us.getName() + "," + us.getUser() + "," + us.getPassword() + "," + us.getMail() + "," + us.getPhone());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing file.");
        }
        return true;
    }
   
    public void validUser(){
    
         
        
    }
    
    
}
