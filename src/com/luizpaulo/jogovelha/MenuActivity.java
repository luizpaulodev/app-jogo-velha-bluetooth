package com.luizpaulo.jogovelha;

import java.util.ArrayList;
import java.util.List;

import com.luizpaulo.Banco.DatabaseHelper;
import com.luizpaulo.adapter.MenuJogoVelha;
import com.luizpaulo.adapter.MenuJogoVelhaAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

public class MenuActivity extends ListActivity {
 
	private List<MenuJogoVelha> lista;
	protected Typeface font;
	@SuppressWarnings("unused")
	private DatabaseHelper helper;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lista = new ArrayList<MenuJogoVelha>();

		lista.add(new MenuJogoVelha("logo"));
		lista.add(new MenuJogoVelha(getResources().getString(R.string.jogar)));
		lista.add(new MenuJogoVelha(getResources().getString(R.string.vitorias)));
		lista.add(new MenuJogoVelha(getResources().getString(R.string.compartilhar)));
		lista.add(new MenuJogoVelha(getResources().getString(R.string.sair)));
			
		helper = new DatabaseHelper(this);
						
		font = Typeface.createFromAsset(getAssets(), "fonts/Cocogoose.otf");

		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		//int height = display.getHeight();
		int width = display.getWidth();
		
		ListView list = getListView();

		list.setBackgroundResource(R.drawable.fundo_gradient);
		list.setDividerHeight(0);
		
		int tela = 0;
		if(width <= 240){
			tela = 320;
		}
		
		if(getResources().getBoolean(R.bool.isTablet)){
			tela = 600;
		}
		
		setListAdapter(new MenuJogoVelhaAdapter(this, lista, tela));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		switch (position) {
		case 1: startActivity(new Intent(this, MenuJogarActivity.class));
			break;

		case 2: startActivity(new Intent(this, EstatisticaActivity.class));
			break;
			
		case 3: 
			Intent share = new Intent(Intent.ACTION_SEND);
			share.setType("text/plain");
			share.putExtra(
					Intent.EXTRA_TEXT, getResources().getString(R.string.mensagemCompartilhar));
			startActivity(Intent.createChooser(share, getResources().getString(R.string.compartilhar) + "..."));
			break;

		default:			
			finish();
			break;
		}
	}
}
