package Compilador;

/* EQUIPO 4
 * 
 * Alumnos:
 * Luis Fernando Sánchez Cazares - 17420613
 * Jose Antonio del Toro Padilla - 17420541
 * Cesar Alejandro Ochoa Garibay - 17420589
 * */

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;

public class AnalisisLexico {
	private String cadena;
	private ArrayList<Token> listaTokens;
	private ArrayList<String> lista, list;
	private String tipoDato = "";
	private InterfazCompilador r;
	private String[] simbolos;
	private boolean variablesDuplicadas;
	
	public AnalisisLexico(String cadena, InterfazCompilador r, String[] simbolos) 
	{
		lista = new ArrayList<String>();
		listaTokens = new ArrayList<Token>();
		list = new ArrayList<String>();
		this.r = r;
		this.simbolos = simbolos;
		this.cadena = cadena;

		crearTokens();
	}
	
	public void crearTokens()
	{
		variablesDuplicadas = false;
		int pos;
		cadena = quitarEspaciosCadena(cadena);
		boolean banderaAux = false;
		boolean noMasDeclaraciones = false;

		while(!cadena.equals(""))
		{
			
			
			if(!noMasDeclaraciones)
				pos = !banderaAux ? encontrarSimbolo(0,cadena) : encontrarSimbolo(13,cadena);
			else
				pos = encontrarSimbolo(13, cadena);
			
			if(cadena.length() > 1 && cadena.charAt(0) == '=' && cadena.charAt(1) == '=')
				pos++;
			
			if(pos == -1)
			{
				
				String cadtem = cadena;
				int cont = 0;
				
				try
				{
					while(pos == -1)
					{		
						cadtem = cadtem.substring(1, cadtem.length());
						pos = encontrarSimbolo(13, cadtem);
						cont++;
					}
					
					lista.add(cadena.substring(0, cont));
					cadena = cadena.substring(cont, cadena.length());
				} catch(StringIndexOutOfBoundsException e) {
					lista.add(cadena);
					cadena = "";
				}
			}
			else
			{
				lista.add(cadena.substring(0, simbolos[pos].length()));
				cadena = cadena.substring(simbolos[pos].length(), cadena.length());
				
				if(pos <= 12)
					banderaAux = true;
				
				if(pos == 14 && !noMasDeclaraciones)
				{
					banderaAux = false;
					if(cadena.startsWith("while") || cadena.startsWith("if"))
						noMasDeclaraciones = true;
					else
					{
						String cadtem = cadena, res = "";
						while(cadtem.contains("=") && cadtem.charAt(0) != '=')
						{
							res += cadtem.charAt(0);
							cadtem = cadtem.substring(1, cadtem.length());
						}
						
						for(int i=0;i<lista.size();i++)
							if(res.equals(lista.get(i)))
								noMasDeclaraciones = true;
					}
				}
			}	
		}
		
		for(int i=0;i<lista.size();i++)
		{
			switch(lista.get(i))
			{
			case "int":
				listaTokens.add(new Token(lista.get(i), "Palabra Reservada"));
				tipoDato = "int";
				for (int g=i;g<lista.size() && !lista.get(g).equals(";");g++) 
					if(!lista.get(g).equals(",") && !lista.get(g).equals("int"))
						list.add(lista.get(g));
				
				break;
			case "float":
				listaTokens.add(new Token(lista.get(i), "Palabra Reservada"));
				tipoDato = "float";
				for (int g=i;g<lista.size() && !lista.get(g).equals(";");g++) 
					if(!lista.get(g).equals(",") && !lista.get(g).equals("float"))
						list.add(lista.get(g));
				break;
			case "char":
				listaTokens.add(new Token(lista.get(i), "Palabra Reservada"));
				tipoDato = "char";
				for (int g=i;g<lista.size() && !lista.get(g).equals(";");g++) 
					if(!lista.get(g).equals(",") && !lista.get(g).equals("char"))
						list.add(lista.get(g));
				break;
				
			case "if": case "else": case "while": case "print": case "leer":
				listaTokens.add(new Token(lista.get(i), "Palabra Reservada"));
				break;
			case ",":
				listaTokens.add(new Token(lista.get(i), "Separador"));
				break;
			case ";": 
				listaTokens.add(new Token(lista.get(i), "Punto y Coma"));
				tipoDato = "";		
				break;
			case "+": 
				listaTokens.add(new Token(lista.get(i), "Suma"));
				break;
			case "-": 
				listaTokens.add(new Token(lista.get(i), "Resta"));
				break;
			case "*": 
				listaTokens.add(new Token(lista.get(i), "Multiplicacion"));
				break;
			case "/": 
				listaTokens.add(new Token(lista.get(i), "Division"));
				break;
			case "(": 
				listaTokens.add(new Token(lista.get(i), "Parentesis de Apertura"));
				break;
			case ")": 
				listaTokens.add(new Token(lista.get(i), "Parentesis de Cierre"));
				break;
			case "=": 
				listaTokens.add(new Token(lista.get(i), "Asignacion"));
				break;
			case "==": 
				listaTokens.add(new Token(lista.get(i), "Comparacion"));
				break;
			case "<": 
				listaTokens.add(new Token(lista.get(i), "menor que"));
				break;
			case ">": 
				listaTokens.add(new Token(lista.get(i), "mayor que"));
				break;
			case "{": 
				listaTokens.add(new Token(lista.get(i), "Llave de Apertura"));
				break;
			case "}": 
				listaTokens.add(new Token(lista.get(i), "Llave de Cierre"));
				break;
			default:
				if(lista.get(i).matches("^0|[1-9][0-9]*$"))
					listaTokens.add(new Token(lista.get(i), "numero entero", "int", lista.get(i)));
				else
					if(lista.get(i).matches("^0[.]0|(0[.][0-9]*[1-9]|[1-9][0-9]*[.](0|[0-9]*[1-9]))$"))
						listaTokens.add(new Token(lista.get(i), "numero flotante", "float", lista.get(i)));
					else
						if(lista.get(i).matches("^[']([a-z]|[A-Z]|[0-9])[']$"))
							listaTokens.add(new Token(lista.get(i), "caracter", "char", lista.get(i)));
						else
							if(lista.get(i).matches("^[\"]([a-z]|[A-Z]|[0-9]|[_ ?@,;=()+*/&%$#!.:¿<>])*[\"]$"))
								listaTokens.add(new Token(lista.get(i), "cadena", " ", lista.get(i)));
							else
								crearIdentificadores(i);
				break;
			}
		}
		
		agregarTokensTabla();
		for (int g=0;g<list.size();g++) 
			if(Collections.frequency(list, list.get(g)) > 1)
				variablesDuplicadas = true;
		
	}
	
	public void crearIdentificadores(int i)
	{
		boolean ban = false;
		for(int j=0;j<listaTokens.size();j++)
			if(lista.get(i).equals(listaTokens.get(j).getComponente()))
			{
				listaTokens.add(new Token(lista.get(i), "identificador", listaTokens.get(j).getTipoDato(), "sin valor"));
				ban = true;
				break;
			}
		
		if(!ban)
			listaTokens.add(new Token(lista.get(i), "identificador", tipoDato, "sin valor"));
	}
	
	public void agregarTokensTabla()
	{
		for(int i=0;i<listaTokens.size();i++)
			r.modeloLexico.addRow(new Object[] {listaTokens.get(i).getComponente(), listaTokens.get(i).getTipo(),listaTokens.get(i).getTipoDato(), listaTokens.get(i).getValor()});
	}
	
	public ArrayList<Token> obtenerTokens()
	{
		return listaTokens;
	}
	
	public int encontrarSimbolo(int posInicio, String cad)
	{
		for(int i=posInicio;i<simbolos.length;i++)
			if(cad.startsWith(simbolos[i]))
				return i;
		return -1;
	}
	
	public String quitarEspaciosCadena(String cad)
	{
		boolean noQuitarEspacios = false;
		String res = "";
		for(int i=0;i<cad.length();i++)
			if(cad.charAt(i) != 10 && cad.charAt(i) != 13 && cad.charAt(i) != 9)
			{
				if(cad.charAt(i) == '"')
					noQuitarEspacios = !noQuitarEspacios;
				
				if(noQuitarEspacios)
					res += cad.charAt(i)+"";
				else
					if(cad.charAt(i) != ' ')
						res += cad.charAt(i)+"";
			}
		return res;
	}
	
	public boolean hayVariablesDuplicadas()
	{
		return variablesDuplicadas;
	}
	
}
