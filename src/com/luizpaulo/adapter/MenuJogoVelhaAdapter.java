package com.luizpaulo.adapter;

import java.util.List;

import com.luizpaulo.jogovelha.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

@SuppressLint("ResourceAsColor") 
public class MenuJogoVelhaAdapter extends BaseAdapter{

	private Context context;
	private List<MenuJogoVelha> lista;
	protected Typeface font;
	private ContextWrapper ctxw;
	private int tela;
	
	public MenuJogoVelhaAdapter(Context context, List<MenuJogoVelha> lista, int tela) {
		super();
		this.context = context;
		this.lista = lista;	
		this.tela = tela;
		ctxw = new ContextWrapper(context);
		font = Typeface.createFromAsset(ctxw.getAssets(), "fonts/Cocogoose.otf"); 
	}

	@Override
	public int getCount() {
		return lista.size();
	}

	@Override
	public Object getItem(int id) {
		return lista.get(id);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(int posicao, View view, ViewGroup parent) {
		
		MenuJogoVelha menu = lista.get(posicao);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v;
		
		if(menu.getNome().equals("logo")){

			if(tela == 320){
				v = inflater.inflate(R.layout.layout_jogo_2, null);
			} else {
				v = inflater.inflate(R.layout.layout_jogo, null);
			}
			
			for(int i = 1; i<=9; i++){
				Button b = (Button) v.findViewWithTag("quadrado" + i);
				b.setTypeface(font);
			}
						
			TextView text = (TextView) v.findViewById(R.id.bluetooth);
			text.setTypeface(font);
			text.setTextColor(Color.CYAN);
			
		} else {
			v = inflater.inflate(R.layout.layout_lista_menu, null); 
			
			TextView nome = (TextView) v.findViewById(R.id.nomeItem);
			
			if(lista.get(posicao).getNome().contains("Blue")){
				nome.setTextColor(Color.CYAN);
			}
					
			nome.setText(menu.getNome());
			
			if(tela == 600){
				nome.setTextSize(30);
			}
			
			nome.setTypeface(font);
		}
		return v;
	}	
}
