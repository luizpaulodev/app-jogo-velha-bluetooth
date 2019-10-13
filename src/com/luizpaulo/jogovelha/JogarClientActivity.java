package com.luizpaulo.jogovelha; 

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class JogarClientActivity extends MainActivity {

	private static final UUID uuid = UUID
			.fromString("5c3229f0-3dd4-11e4-916c-0800200c9a66");

	private BluetoothSocket socket;

	private InputStream in;
	private OutputStream out;
	private Button btnConectar;
	
	private boolean running;
	
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		btnConectar = (Button) getView().findViewById(R.id.btnConectar);
		btnConectar.setVisibility(View.VISIBLE);
		btnNovo.setVisibility(View.GONE);
		
		AlertDialog.Builder info = new AlertDialog.Builder(this);
		
		info.setMessage(getResources().getString(R.string.informacao_modo_bluetooth_cliente));
		info.setPositiveButton(getResources().getString(R.string.ok), null);
		info.show();
		
		setTitle(getResources().getString(R.string.jogador_o));

		jogar = false;
		comecar = false;

		btnConectar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (device == null) {
					Toast.makeText(JogarClientActivity.this, getResources().getString(R.string.selecione_usuario), Toast.LENGTH_SHORT).show();
				} else {
					conectar();
				}
			}
		});
	}

	private void conectar() {

		dialog = ProgressDialog.show(this, null,
				getResources().getString(R.string.conectando), false, true);
			
		new EscutaServidor().start();		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultado, Intent data) {
		// Aqui você pode conferir se o usuario ativou ou não o bluetooth
		switch (resultado) {
		case Activity.RESULT_CANCELED:
			Toast.makeText(this, getResources().getString(R.string.bluetooth_ativado),
					Toast.LENGTH_SHORT).show();
			finish();
			break;

		case 9:
			device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			break;
		}
	}

	@Override
	public void clickQuadrado(View v) {
		
		if (jogar) {
			if (((Button) v).getText().equals("")) {
				((Button) v).setTypeface(font);
				((Button) v).setText(BOLA);

				String msg = ((Button) v).getTag().toString();
				msg += ";" + BOLA;
				try {
					if (out != null) {
						out.write(msg.getBytes());
						vibrar();
						jogar = false;
					}
				} catch (IOException e) {}
				isFim();
			}
		}
	}

	class EscutaServidor extends Thread {

		@Override
		public void run() {

			try {
				socket = device.createRfcommSocketToServiceRecord(uuid);
				socket.connect();
				
				out = socket.getOutputStream();
				in = socket.getInputStream();

				running = true;
			} catch (IOException e) {
				e.printStackTrace();
				dialog.dismiss();
				running = false;
				toastThread(getResources().getString(R.string.erro_servidor));
				return;
			} 
			
			if (out != null) {
				conectado(getResources().getString(R.string.conectado) + device.getName());
				dialog.dismiss();
			}
			
			try {

				if (socket != null) {

					in = socket.getInputStream();
					out = socket.getOutputStream();

					byte[] bytes = new byte[1024];
					int length;

					// Fica em looping para receber mensagens
					while (running) {

						length = in.read(bytes);
						String mensagemRecebida = new String(bytes, 0, length);

						final String s = mensagemRecebida;
						
						if (s.contains(NOVO_JOGO)) {
							newGame();

							String[] split = s.split(";");

							if (split[1].equals("true")) {
								jogar = true;
								setEnableQuadrado(true);
							} else {
								jogar = false;
								setEnableQuadrado(false);
							}

						}
						if (s.contains(QUADRADO)) {
							jogar = true;
							setEnableQuadrado(true);
							atualizarJogo(s);
						}
						
						if(s.contains("Empate")){
							String[] splt = s.split(";");
							empate(splt[1]);
						}
					}
				}

			} catch (IOException e) {
				Log.e("Chat", "Erro servidor... " + e.getMessage());
				running = false;
			}
		}
	}
	
	private void conectado(final String s){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				TextView conect = (TextView) getView().findViewById(R.id.prxJogada);				
				conect.setText(s);
				btnConectar.setVisibility(View.GONE);
			}
		});
	}
	
	private void toastThread(final String s){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				//Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
				exibirToast(s);
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
	
	public void empate(final String s){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				//Toast.makeText(JogarClientActivity.this, getResources().getString(R.string.empate_jogo), Toast.LENGTH_SHORT).show();
				exibirToast(getResources().getString(R.string.empate_jogo));
				getPlacar("Empate").setText(String.valueOf(s));
			}
		});
	}

	// Exibe na tela a mensagem recebida
	public void atualizarJogo(final String s) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String[] array = s.split(";");

				Button btn = (Button) getView().findViewWithTag(array[0]);
				btn.setTypeface(font);
				btn.setText(array[1]);
				setLastPlay(array[1]);
				isFim();
			}
		});
	}

	public void newGame() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				for (int i = 1; i <= 9; i++) {
					if (getQuadrado(i) != null) {
						getQuadrado(i).setText("");
						getQuadrado(i).setTextColor(
								getResources().getColor(R.color.branco));
					}
				}

				vibrar();
				ganhador = false;
				//showInterstial();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
		}
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
		}
	}
}
