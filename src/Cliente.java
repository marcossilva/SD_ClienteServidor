

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cliente implements IServidor, Serializable {

    IServidor iServer;

    public Cliente() {
        try {
            System.setSecurityManager(new SecurityManager());
            iServer = (IServidor) Naming.lookup("rmi://localhost/myserver");
        } catch (NotBoundException | MalformedURLException | RemoteException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static public void main(String args[]) {
        Cliente cli1 = new Cliente();
        Cliente cli2 = new Cliente();
        Cliente cli3 = new Cliente();
        
        try {
            System.out.println("Cliente 1 - Lendo linha 0 - Ler 1 linha - Leu:");
            System.out.println(cli1.le("arquivo1.txt", 0, 1));
            System.out.println(cli2.le("arquivo2.txt", 0, 1));
            System.out.println(cli3.le("arquivo3.txt", 0, 1));
            
        } catch (RemoteException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String le(String nomeArquivo, int numLinha, int qntLinhas) throws RemoteException {
        return iServer.le(nomeArquivo, numLinha, qntLinhas);
    }

    @Override
    public boolean escreve(String nomeArquivo, int qntLinhas, String dados) throws RemoteException {
        return iServer.escreve(nomeArquivo, qntLinhas, dados);
    }
}
