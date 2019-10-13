package com.luizpaulo.jogovelha;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

public class BuscarDevicesActivity extends ListaPareadosActivity {

	private ProgressDialog dialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		//Registra o receiver para receber as mensagens de dispositivos pareados
		this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		
		//Registra em broadcasts quando a descoberta for concluída
		this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

		buscar();
	}
		
	private void buscar(){
		//Garante que nao existe outra busca sendo realizada
		if(bluetooth.isDiscovering()){
			bluetooth.cancelDiscovery();
		}
		
		//Dispara a busca
		bluetooth.startDiscovery();
		
		dialog = ProgressDialog.show(this, getResources().getString(R.string.bluetooth), getResources().getString(R.string.buscando_aparelhos) + "...", false, true);
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		// Quantidade de dispositivos encontrados
		private int count;

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// Se um device foi encontrado
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Recupera o device da intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				// Apenas insere na lista os devices que ainda não estão pareados
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					lista.add(device);
					updateLista();
					Toast.makeText(context, getResources().getString(R.string.encontrou)+ device.getName()+":"+device.getAddress(), Toast.LENGTH_SHORT).show();
					count++;
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				// Iniciou a busca
				count = 0;
				Toast.makeText(context, "Busca iniciada.", Toast.LENGTH_SHORT).show();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// Terminou a busca
				Toast.makeText(context, getResources().getString(R.string.busca_finalizada, count), Toast.LENGTH_LONG).show();
				
				dialog.dismiss();
				
				// Atualiza o listview. Agora via possuir todos os devices pareados, mais os novos que foram encontrados.
				updateLista();
			}
		}
	};
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		//Garante que a busca é cancelada ao sair
		if(bluetooth != null){
			bluetooth.cancelDiscovery();
		}
		
		this.unregisterReceiver(mReceiver);
	}
}
