package sd_clienteservidor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor extends UnicastRemoteObject implements IServidor {

    File arquivo1 = new File("arquivo1.txt");
    File arquivo2 = new File("arquivo2.txt");
    File arquivo3 = new File("arquivo3.txt");
    File[] arquivos = {arquivo1, arquivo2, arquivo3};
    Semaphore escritor1 = new Semaphore(1);
    Semaphore escritor2 = new Semaphore(1);
    Semaphore escritor3 = new Semaphore(1);
    Semaphore escritores[] = {escritor1, escritor2, escritor3};
    int leitores[] = new int[3];

    public Servidor() throws RemoteException {

    }

    static public void main(String args[]) {
        try {
            Registry r = LocateRegistry.getRegistry();
            r.bind("myserver", new Servidor());
            System.out.println("The server has been started.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public String le(String nomeArquivo, int numLinha, int qntLinhas) throws RemoteException {
        String lido = "";
        try {
            //Leitura não precisa travar o acesso pois pode ser lido mutuamente
            //Qual arquivo abrir?
            Scanner read;
            int numArquivo = 0;
            if (nomeArquivo.length() > 8) {
                numArquivo = Integer.parseInt(nomeArquivo.substring(7, 8)) - 1;
            }
            //Semáforo para esperar escritor sair do arquivo
            if (escritores[numArquivo].isFair()) {
                read = new Scanner(arquivos[numArquivo]);
                leitores[numArquivo] = leitores[numArquivo] + 1;
                //Consome o stream até a linha especificada se existir
                while (numLinha > 0 && read.hasNextLine()) {
                    read.nextLine();
                    numLinha--;
                }
                if (numLinha > 0) {
                    System.err.println("Invalid line specified! Please check it!");
                } else {
                    while (qntLinhas > 0 && read.hasNextLine()) {
                        //nextLine consome o caracter quebra de linha
                        lido += read.nextLine() + "\n";
                        qntLinhas--;
                    }
                    if (qntLinhas > 0) {
                        System.err.println("There specified quantity of lines required is not avaiable. Returning all the lines read...");
                    }
                }
                leitores[numArquivo] = leitores[numArquivo] - 1;
            }
        } catch (NumberFormatException nf) {
            System.err.println("Invalid name file provided!");
            return null;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lido;
    }

    @Override
    public boolean escreve(String nomeArquivo, int qntLinhas, String dados) throws RemoteException {
        try {
            FileWriter write;
            int numArquivo = 0;
            if (nomeArquivo.length() > 8) {
                numArquivo = Integer.parseInt(nomeArquivo.substring(7, 8)) - 1;
            }
            while (leitores[numArquivo] > 0 && escritores[numArquivo].isFair()) {
                //wait
                //this.wait();
                //Fila de escritores?
            }
            //Toma o arquivo para escrita
            escritores[numArquivo].acquire();
            write = new FileWriter(arquivos[numArquivo]);
            write.write(dados);
            write.flush();
            write.close();
            escritores[numArquivo].release();
        } catch (NumberFormatException nf) {
            System.err.println("Invalid name file provided!");
            return false;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }   
}
