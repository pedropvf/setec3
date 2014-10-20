package com.example.p1_gatinhos;

public class Gato {
	
	public String nome;
	public int idade;
	
	public Gato(String name, int age){
		nome = name;
		idade = age;
	}
	
	public String getName(){
		return nome;
	}
	
	public int getAge(){
		return idade;
	}

}
