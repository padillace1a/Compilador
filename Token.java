package Compilador;

/* EQUIPO 4
 * 
 * Alumnos:
 * Luis Fernando Sánchez Cazares - 17420613
 * Jose Antonio del Toro Padilla - 17420541
 * Cesar Alejandro Ochoa Garibay - 17420589
 * */

public class Token {
	private String componente;
	private String tipo;
	private String tipoDato;
	private String valor;
	
	public Token(String componente, String tipo)
	{
		this.componente = componente;
		this.tipo = tipo;
		this.tipoDato = " ";
		this.valor = "";
	}
	
	public Token(String componente, String tipo, String tipoDato, String valor)
	{
		this.componente = componente;
		this.tipo = tipo;
		this.tipoDato = tipoDato;
		this.valor = valor;
	}
	
	public String getComponente() 
	{
		return componente;
	}

	public String getTipo() 
	{
		return tipo;
	}

	public String getTipoDato() 
	{
		return tipoDato;
	}
	
	public String getValor()
	{
		return valor;
	}
	
	public void setValor(String valor)
	{
		this.valor = valor;
	}
}
