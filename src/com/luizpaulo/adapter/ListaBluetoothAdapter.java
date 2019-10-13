package com.luizpaulo.adapter;

import java.util.List;

import com.luizpaulo.jogovelha.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListaBluetoothAdapter extends BaseAdapter{

	private Context context;
	private List<ListaBluetooth> lista;
	private ContextWrapper ctxw;
	
	public ListaBluetoothAdapter(Context context, List<ListaBluetooth> lista) {
		super();
		this.context = context;
		this.lista = lista;
		ctxw = new ContextWrapper(context);
	}

	@Override
	public int getCount() {
		return lista.size();
	}

	@Override
	public Object getItem(int posicao) {
		return lista.get(posicao);
	}

	@Override
	public long getItemId(int posicao) {
		return posicao;
	}

	@Override
	public View getView(int posicao, View view, ViewGroup parent) {
		
		final ListaBluetooth item = lista.get(posicao);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.layout_lista_bluetooth, null);
		
		TextView nome = (TextView) v.findViewById(R.id.nomeDevice);
		TextView estado = (TextView) v.findViewById(R.id.estadoDevice);
		ImageView opcao = (ImageView) v.findViewById(R.id.opcaoDevice);
		
		nome.setText(item.getDevice().getName());
		estado.setText(item.getEstado());
		
		opcao.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				AlertDialog.Builder alerta = new AlertDialog.Builder(context);
				
				alerta.setTitle(ctxw.getResources().getString(R.string.informacoes));
				alerta.setMessage(
						ctxw.getResources().getString(R.string.informacaoes_dispostivos_bluetooth,
								item.getDevice().getName(),
								item.getDevice().getAddress(),
								item.getEstado()));

				
				alerta.show();
			}
		});
		
		
		return v;
	}
	
	

}
