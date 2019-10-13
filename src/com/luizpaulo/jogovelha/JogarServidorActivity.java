package com.luizpaulo.jogovelha;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class JogarServidorActivity extends MainActivity{

	private static final UUID uuid = UUID.fromString("5c3229f0-3dd4-11e4-916c-0800200c9a66");	
	
	private BluetoothServerSocket serverSocket;
	private BluetoothSocket socket;
	
	private InputStream in;
	private OutputStream out;
	
	private boolean running;
	private boolean jogar = false;
	private boolean conectado = false;
		
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
			
		btnNovo.setText(getResources().getString(R.string.iniciar));
		
		setTitle(getResources().getString(R.string.jogador_x));
		comecar = true;
		
		//Se o bluetooth estiver ligado vamos iniciar a thread
		if(bluetooth != null && bluetooth.isEnabled()){
			running = true;
			new ThreadServidor().start();
		}
	}

	@Override
	public void clickQuadrado(View v){
		
		if(jogar){
			if(((Button)v).getText().equals("")){
				((Button) v).setTypeface(font);
				((Button)v).setText(XIS);
				
				String msg = ((Button)v).getTag().toString();
				msg += ";" + XIS;
				try{
					if(out != null){
						Log.i("Chat", msg);
						out.write(msg.getBytes());
					}
				} catch(IOException e){}
				vibrar();
				jogar = false;
				isFim();
			}
		}
	}

	@Override
	public void newGame(View v){
		super.newGame(v);
		
		if(conectado){
			btnNovo.setText(getResources().getString(R.string.novo_jogo));
			try{
				if(out != null){
					out.write((NOVO_JOGO + (comecar ? ";false" : ";true")).getBytes());
					
					setEnableQuadrado(inverteJogada(comecar ? false : true));
					jogar = inverteJogada(comecar ? false : true);
					
					comecar = (comecar ? false : true);
					
					ganhador = false;
				}
			} catch(IOException e){}
			vibrar();
		} else {
			//showInterstial();
			Toast.makeText(this, getResources().getString(R.string.aguardando_conexao), Toast.LENGTH_SHORT).show();
		}
	}
	
	private boolean inverteJogada(boolean bool){
		if(bool)
			comecar = false;
		else
			comecar = true;
		
		return comecar;
	}
	
	class ThreadServidor extends Thread{
		
		@Override
		public void run(){
			
			try{
				//Abre o socket servidor(quem for conectar precisa utilizar o mesmo uuid)
				BluetoothServerSocket serverSocket = bluetooth.listenUsingRfcommWithServiceRecord("LivroAndroid", uuid);
				
				//Aguarda conexões
				socket = serverSocket.accept();

				if(socket != null){
					//Alguem conectou, entao encerre o socket server, nao precisamos mais dele
					serverSocket.close();
					conectado = true;

					in = socket.getInputStream();
					out = socket.getOutputStream();
					
					//Recupera o device cliente que fez a conexão
					final BluetoothDevice device = socket.getRemoteDevice();
					
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							prxJogada.setText(getResources().getString(R.string.conectado) + device.getName());
						}
					});

					byte[] bytes = new byte[1024];
					int length;
					
					//Fica em looping para receber mensagens
					while(running){
						//Lê a mensagem (esta chama fica bloqueada ate alguem escrever)
						length = in.read(bytes);
						String mensagemRecebida = new String(bytes, 0, length);
						
						final String s = mensagemRecebida;
						
						if(s.contains(QUADRADO)){
							jogar = true;
							setEnableQuadrado(true);
							atualizarJogo(s);
						}
					}
				}

			} catch(IOException e){
				Log.i("Chat", "Erro servidor... " + e.getMessage());
				running = false;
				conectado = false;
				
			}
		}
	}
	
	//Exibe na tela a mensagem recebida
	public void atualizarJogo(final String s){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				String[] array = s.split(";");
				
				Button btn = (Button) getView().findViewWithTag(array[0]);
				btn.setTypeface(font);
				btn.setText(array[1]);
				isFim();
			}
		});
	}
	
	@Override
	public void isFim(){
		super.isFim();
		
		contadorVelha = 0;
		for(int i = 1; i <= 9; i++){
			String jogada = getQuadrado(i).getText().toString();
			if(!jogada.equals("")){
				contadorVelha++;
			}
		}
		
		if(!ganhador && contadorVelha == 9){
			//Toast.makeText(getBaseContext(), getResources().getString(R.string.empate_jogo), Toast.LENGTH_SHORT).show();
			exibirToast(getResources().getString(R.string.empate_jogo));
			empate++;
			getPlacar("Empate").setText(String.valueOf(empate));
			atualizarPontos("empate", "b");
			showInterstial();
		}
	}
			
	@Override
	public void onDestroy(){
		super.onDestroy();
		running = false;
		
		try{
			if(socket != null){
				socket.close();
			}
			
			if(serverSocket != null){
				serverSocket.close();
			}
			
		} catch(IOException e){}
	}
}
