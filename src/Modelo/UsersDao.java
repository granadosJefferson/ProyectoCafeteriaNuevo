/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Personal
 */
public class UsersDao {

    ArrayList<Users> list;

    public UsersDao() {
        list = new ArrayList<>();
        cargarDesdeTxt();

    }

    public void cargarDesdeTxt() {
        list.clear();

        try (BufferedReader br = new BufferedReader(new FileReader("Users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String ced = parts[0];
                    String name = parts[1];
                    String user = parts[2];
                    String password = parts[3];
                    String mail = parts[4];
                    int phone = Integer.parseInt(parts[5]);

                    list.add(new Users(user, password, mail, phone, ced, name));
                }
            }
        } catch (java.io.FileNotFoundException e) {
            
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
    }

    public boolean addUser(Users a) {

    // 1) bloquear duplicados
    if (userExist(a.getCedula(), a.getUser())) {
        return false;
    }

    // 2) guardar SOLO el nuevo usuario
    try (BufferedWriter bw = new BufferedWriter(new FileWriter("Users.txt", true))) {
        bw.write(a.getCedula() + "," + a.getName() + "," + a.getUser() + "," +
                 a.getPassword() + "," + a.getMail() + "," + a.getPhone());
        bw.newLine();
    } catch (IOException e) {
        System.out.println("Error writing file.");
        return false;
    }

    // 3) actualizar memoria
    list.add(a);
    return true;
}


    public boolean userExist(String ced, String user) {
        for (Users us : list) {
            if (us.getCedula().equals(ced) || us.getUser().equals(user)) {
                return true;
            }
        }
        return false;
    }

    public boolean validUser(String user, String pass) {

        for (Users us : list) {
            if (us.getUser().equals(user) && us.getPassword().equals(pass)) {
                return true;
            }
        }

        return false;
    }

}
