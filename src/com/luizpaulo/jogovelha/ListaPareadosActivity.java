package com.luizpaulo.jogovelha;

import java.util.ArrayList;
import java.util.List;

import com.luizpaulo.adapter.ListaBluetooth;
import com.luizpaulo.adapter.ListaBluetoothAdapter;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ListaPareadosActivity extends MainActivity implements OnItemClickListener {

	protected List<BluetoothDevice> lista;
	protected ListView listView;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_list);

		listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setEnabled(true);

		// Registra o receiver para receber as mensagens de dispositivos
		// pareados
		this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

		// Registra em broadcasts quando a descoberta for concluída
		this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		
		Button btn = (Button) findViewById(R.id.btnBuscar);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				buscar();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (bluetooth != null) {
			lista = new ArrayList<BluetoothDevice>(bluetooth.getBondedDevices());
			updateLista();
		}
	}

	public void updateLista() {

		List<ListaBluetooth> lisBlu = new ArrayList<ListaBluetooth>();

		for (BluetoothDevice device : lista) {
			
			boolean pareado = device.getBondState() == BluetoothDevice.BOND_BONDED;			
			lisBlu.add(new ListaBluetooth(device, pareado ? getResources().getString(R.string.pareado) : ""));
		}

		listView.setAdapter(new ListaBluetoothAdapter(this, lisBlu));
	}

	@Override
	public void onBackPressed() {
		setResult(CLOSE);
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int posicao,
			long id) {

		BluetoothDevice device = lista.get(posicao);

		if (device != null) {
			Intent intent = new Intent();
			intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
			setResult(9, intent);
		}

		finish();
	}

	private void buscar() {
		// Garante que nao existe outra busca sendo realizada
		if (bluetooth.isDiscovering()) {
			bluetooth.cancelDiscovery();
		}

		// Dispara a busca
		bluetooth.startDiscovery();

		dialog = ProgressDialog.show(this, getResources().getString(R.string.app_name),
				getResources().getString(R.string.buscando_aparelhos), false, true);
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
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				// Apenas insere na lista os devices que ainda não estão
				// pareados
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					lista.add(device);
					Toast.makeText(context,getResources().getString(R.string.encontrou) + device.getName() + ":" + device.getAddress(), Toast.LENGTH_SHORT).show();
					count++;
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				// Iniciou a busca
				count = 0;
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// Terminou a busca
				Toast.makeText(context, getResources().getString(R.string.busca_finalizada, count), Toast.LENGTH_LONG).show();

				dialog.dismiss();

				// Atualiza o listview. Agora via possuir todos os devices
				// pareados, mais os novos que foram encontrados.
				updateLista();
			}
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Garante que a busca é cancelada ao sair
		if (bluetooth != null) {
			bluetooth.cancelDiscovery();
		}

		this.unregisterReceiver(mReceiver);
	}

}
