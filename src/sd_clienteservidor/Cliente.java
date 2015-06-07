package sd_clienteservidor;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cliente implements Serializable {

    static public void main(String args[]) {
        try {            
            System.setSecurityManager(new RMISecurityManager());
            IServidor iServer = (IServidor) Naming.lookup("rmi://localhost/myserver");
            switch (args[0]) {
                case "Leitor":
                    iServer.le(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                    break;
                case "Escritor":
                    iServer.escreve(args[1], Integer.parseInt(args[2]), args[3]);
                    break;
                default:
                    break;
            }
        } catch (NotBoundException | MalformedURLException | RemoteException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
