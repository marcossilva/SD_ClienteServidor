
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Cliente implements IServidor, Serializable {

    IServidor iServer;

    public Cliente() {
        try {
            iServer = (IServidor) Naming.lookup("rmi://localhost/myserver");
        } catch (NotBoundException | MalformedURLException | RemoteException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static public void main(String args[]) {
        Thread t1 = new Thread(new Leitor());
        Thread t2 = new Thread(new Leitor());
        Thread t3 = new Thread(new Leitor());
        System.out.println("");
        t1.start();
        t2.start();
        t3.start();
    }

    @Override
    public boolean escreve(String nomeArquivo, int qntLinhas, String dados) throws RemoteException {
        return iServer.escreve(nomeArquivo, qntLinhas, dados);
    }
}

class Leitor extends Cliente implements Runnable {

    public Leitor() {
        super();
    }

    @Override
    public void run() {
        try {
            System.out.println(le("arquivo1.txt", 0, 1));
        } catch (RemoteException ex) {
            Logger.getLogger(Leitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String le(String nomeArquivo, int numLinha, int qntLinhas) throws RemoteException {
        return iServer.le(nomeArquivo, numLinha, qntLinhas);
    }

}
