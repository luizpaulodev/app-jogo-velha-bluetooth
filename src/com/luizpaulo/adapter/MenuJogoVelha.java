package com.luizpaulo.adapter;

public class MenuJogoVelha {

	private String nome;
	private int imagem;
	
	public MenuJogoVelha() {
		super();
	}
	
	public MenuJogoVelha(String nome) {
		super();
		this.nome = nome;
		//this.imagem = imagem;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getImagem() {
		return imagem;
	}

	public void setImagem(int imagem) {
		this.imagem = imagem;
	}
}
