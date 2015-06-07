/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sd_clienteservidor;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author marcos
 */
//defines the methods that can be invoked from the client
/*
 Trabalho cliente servidor:
 3 clientes
 3 arquivos 
 3 sistemas- prioridade para leitura, prioridade para escrita, sem prioridades PARA CADA ARQUIVO.
 Parâmetros de leitura: nome do arquivo, numero da linha e qtd de linhas
 Parametros de escrita: nome do arquivo, quantidade de linhas e os dados. Sempre começar do final do arquivo, a escrita.
 Data de entrega: 09/06/2015
 */
public interface IServidor extends Remote {

    public String le(String nomeArquivo, int numLinha, int qntLinhas) throws RemoteException;

    public boolean escreve(String nomeArquivo, int qntLinhas, String dados) throws RemoteException;
}
