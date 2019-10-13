package com.luizpaulo.Banco;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String BANCO_DADOS = "BancoJogoVelha";
	private final String TABELA = "jogovelha";
	private static int VERSAO = 1;
	
	public DatabaseHelper(Context context) {
		super(context, BANCO_DADOS, null, VERSAO);
	}

	@Override
	public void onCreate(SQLiteDatabase sql) {
		
		String SQL = "CREATE TABLE jogovelha(" +
				"_id INTEGER PRIMARY KEY," +
				"jogador1 INTEGER," +
				"jogador2 INTEGER," +
				"empate INTEGER," +
				"tipo TEXT);";
		sql.execSQL(SQL);
		
		ContentValues values1 = new ContentValues();
		values1.put("jogador1", 0);
		values1.put("jogador2", 0);
		values1.put("empate", 0);
		values1.put("tipo", "f");
		
		sql.insert(TABELA, null, values1);
		
		ContentValues values2 = new ContentValues();
		values2.put("jogador1", 0);
		values2.put("jogador2", 0);
		values2.put("empate", 0);
		values2.put("tipo", "m");
		
		sql.insert(TABELA, null, values2);
		
		ContentValues values3 = new ContentValues();
		values3.put("jogador1", 0);
		values3.put("jogador2", 0);
		values3.put("empate", 0);
		values3.put("tipo", "d");
		
		sql.insert(TABELA, null, values3);
		
		ContentValues values4 = new ContentValues();
		values4.put("jogador1", 0);
		values4.put("jogador2", 0);
		values4.put("empate", 0);
		values4.put("tipo", "pp");
		
		sql.insert(TABELA, null, values4);
		
		ContentValues values5 = new ContentValues();
		values5.put("jogador1", 0);
		values5.put("jogador2", 0);
		values5.put("empate", 0);
		values5.put("tipo", "b");
		
		sql.insert(TABELA, null, values5);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
}
