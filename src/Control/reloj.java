/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Control;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.Timer;

/**
 *
 * @author Personal
 */
public class reloj {
    private Timer timer;

    public void iniciar(JLabel lblDay, JLabel lblTime) {

        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd 'de' MMMM 'del' yyyy");
        SimpleDateFormat formatoHora  = new SimpleDateFormat("hh:mm:ss a");

        timer = new Timer(1000, e -> {
            Date ahora = new Date();
            lblDay.setText(formatoFecha.format(ahora));
            lblTime.setText(formatoHora.format(ahora).toLowerCase());
        });

        timer.start();
    }

    public void detener() {
        if (timer != null) timer.stop();
    }
}
