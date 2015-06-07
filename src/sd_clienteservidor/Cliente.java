package sd_clienteservidor;

import java.rmi.*;
import java.rmi.registry.*;

public class Cliente {

    private class Leitor {

        Leitor() {

        }
    }

    private class Escritor {

        Escritor() {

        }
    }

    static public void main(String args[]) {
        switch (args[0]) {
            case "Leitor":
                new Leitor();
                break;
            case "Escritor":
                new Escritor();
                break;
            default:
                break;
        }
    }
}
