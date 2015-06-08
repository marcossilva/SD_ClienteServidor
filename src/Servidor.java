
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
    int[] counter_leitores = new int[3];
    Semaphore leitor1 = new Semaphore(3);
    Semaphore leitor2 = new Semaphore(3);
    Semaphore leitor3 = new Semaphore(3);
    Semaphore[] leitores = {leitor1, leitor2, leitor3};
    private final int prioridade;

    public Servidor(int prioridade) throws RemoteException, Exception {
        if (prioridade > 3 || prioridade < 1) {
            throw new Exception("Invalid server priority.");
        }
        this.prioridade = prioridade;
    }

    static public void main(String args[]) {
        //args[0] define prioridade        
        try {
            Registry r = LocateRegistry.getRegistry();
            r.rebind("myserver", new Servidor(Integer.parseInt(args[0])));
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
            System.out.println("Abrindo arquivo " + nomeArquivo);
            System.out.println(numArquivo);
            System.out.println(escritores[numArquivo].availablePermits());
            //Semáforo para esperar escritor sair do arquivo
            switch (prioridade) {
                case 1: //Prioridade para Leitor
                    //Impede leitores de acessarem esse arquivo
                    if (escritores[numArquivo].availablePermits() > 0) {
                        escritores[numArquivo].acquire();
                    }
                    //Abre um novo recurso para um leitor
                    leitores[numArquivo].acquire();
                    break;
                case 2: //Prioridade para Escritor
                case 3: //Sem prioridade
                    //Impede leitores de acessarem esse arquivo
                    if (escritores[numArquivo].availablePermits() > 0) {
                        escritores[numArquivo].acquire();
                    }
                    //Abre um novo recurso para um leitor
                    leitores[numArquivo].acquire();
                    break;
            }
            //Fim do semaforo
            read = new Scanner(arquivos[numArquivo]);
            System.out.println("Lendo " + nomeArquivo);
            counter_leitores[numArquivo] = counter_leitores[numArquivo] + 1;
            System.out.println("Total de leitores lendo o arquivo no momento:\t" + counter_leitores[numArquivo]);
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
            counter_leitores[numArquivo] = counter_leitores[numArquivo] - 1;
            //Libera recursos
            switch (prioridade) {
                case 1: //Prioridade para Leitor
                    //Libera o recurso do leitor
                    leitores[numArquivo].release();
                    //Libera a escrita sss não houver mais nenhum leitor na fila
                    if (!leitores[numArquivo].hasQueuedThreads()) {
                        escritores[numArquivo].release();
                    }
                    break;
                case 2: //Prioridade para Escritor
                    //Precisa esperar todos os leitores atuais acabarem
                    if (!escritores[numArquivo].hasQueuedThreads()) {
                        escritores[numArquivo].release();
                    }
                    break;
                case 3: //Sem prioridade
                    //Libera o recurso do leitor
                    leitores[numArquivo].release();
                    //Libera a escrita sss não houver mais nenhum leitor utilizando o recurso
                    escritores[numArquivo].release();
                    break;
            }
        } catch (NumberFormatException nf) {
            System.err.println("Invalid name file provided!");
            return null;
        } catch (FileNotFoundException | InterruptedException ex) {
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
            //Semáforo
            switch (prioridade) {
                case 1: //Prioridade para leitor
                    while (leitores[numArquivo].hasQueuedThreads()) {
                        Thread.sleep(10);
                    }
                    escritores[numArquivo].acquire();
                    break;
                case 2:
                case 3:
                    //Reserva o recurso de escrita
                    escritores[numArquivo].acquire();
                    while (leitores[numArquivo].availablePermits() > 0) {
                        //Enquanto houver leitores no arquivo aguarde
                        Thread.sleep(10);
                    }
                    break;
            }
            write = new FileWriter(arquivos[numArquivo], true);
            write.write(dados);
            write.flush();
            write.close();
            switch (prioridade) {
                case 1: //Prioridade para leitor
                    //Libera o recurso de escrita
                    escritores[numArquivo].release();
                    break;
                case 2: //Prioridade para escritor
                    //Verifica se existe algum leitor na fila

                    escritores[numArquivo].release();
                    break;
                case 3:
                    escritores[numArquivo].release();
                    break;
            }
        } catch (NumberFormatException nf) {
            System.err.println("Invalid name file provided!");
            return false;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
