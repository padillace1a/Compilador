package Compilador;

/* EQUIPO 4
 * 
 * Alumnos:
 * Luis Fernando Sánchez Cazares - 17420613
 * Jose Antonio del Toro Padilla - 17420541
 * Cesar Alejandro Ochoa Garibay - 17420589
 * */

import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class AnalisisSintacticoYSemantico {
	String filas[], columnas[], tabla[][], NTProducciones[], producciones[], simbolos[];
	ArrayList<Token> tokensSintactico, tokensLexico;
	InterfazCompilador r;
	Stack<String> pila, pilaEtiquetas;
	Stack<Integer> pilaSemantica;
	int x, y;
	boolean error;
	AnalisisLexico AL;
	
	int mapeoTiposDatos[][] = {{1, 2, 1},
			          		   {2, 2, 2},
			          		   {1, 2, 3}};
	String tiposDatos[] = {"int", "float", "char"};

	public AnalisisSintacticoYSemantico(String filas[], String columnas[], String tabla[][], String NTProducciones[], String producciones[], InterfazCompilador r, String[] simbolos)
	{
		this.filas = filas;
		this.columnas = columnas;
		this.tabla = tabla;
		this.NTProducciones = NTProducciones;
		this.producciones = producciones;
		this.simbolos = simbolos;
		pila = new Stack<String>();
		pilaSemantica = new Stack<Integer>();
		pilaEtiquetas = new Stack<String>();
		this.r = r;		
	}
	
	public void establecerCadena(String cadena)
	{
		error = false;
		pila.clear();
		pila.push("I0");
		pilaSemantica.clear();
		pilaEtiquetas.clear();
		AL = new AnalisisLexico(cadena, r, simbolos);
		tokensLexico = new ArrayList<Token>(AL.obtenerTokens());
		tokensSintactico = new ArrayList<Token>(AL.obtenerTokens());
		if(AL.hayVariablesDuplicadas())
			JOptionPane.showMessageDialog(null, "Hay declaraciones de indentificadores duplicadas", "ERROR SEMANTICO", JOptionPane.ERROR_MESSAGE);
		else
			analisisSintacticoSemantico();
	}
	
	public void analisisSintacticoSemantico()
	{
		int posProduccion = -1, et_if = 1, et_else = 1, et_while = 1;
		tokensSintactico.add(new Token("$", ""));
		Token Asignacion = null, varAsignacion = null;
		int auxiliarMapeo = 0;
		Stack<String> pilaEtFin = new Stack<String>(), pilaEstructura = new Stack<String>();
		pilaEtFin.clear();
		pilaEstructura.clear();
		String resPrint = "", codigoObjeto = "";
		boolean ejecutandoPrint = false, haySentencias = false, ejecutandoSentencias = false;
		
		if(!existenTodasLasVariables())
			JOptionPane.showMessageDialog(null, "El identificador \"" + tokensLexico.get(posicionVariableQueNoExiste).getComponente() + "\" no esta declarado", "ERROR SEMANTICO", JOptionPane.ERROR_MESSAGE);
		else
		{
			while(!error && posProduccion != 0)
			{
				try
				{
					x = encuentraPosicionFila(pila.peek());
					y = encuentraPosicionColumna(tokensSintactico.get(0).getComponente());
				if(x != -1)
				{
					if(y == -1)
						if(tokensSintactico.get(0).getComponente().matches("^0|[1-9][0-9]*$"))
							y = 1;
						else
							if(tokensSintactico.get(0).getComponente().matches("^0[.]0|(0[.][0-9]*[1-9]|[1-9][0-9]*[.](0|[0-9]*[1-9]))$"))
								y = 2;
							else
								if(tokensSintactico.get(0).getComponente().matches("^[']([a-z]|[A-Z]|[0-9])[']$"))
									y = 3;
								else
									if(tokensSintactico.get(0).getComponente().matches("^[\"]([a-z]|[A-Z]|[0-9]|[_ ?@,;=()+*/&%$#!.:¿<>])*[\"]$"))
										y = 4;
									else
										y = 0;
					
					if(esEstado(tabla[x][y]))
					{
						if(y <= 4)
						{
							Asignacion = tokensSintactico.get(0);
							
							switch(pila.peek())
							{
							case "I0": case "I7": case "I8": case "I9": case "I22": case "I51": case "I52": case "I60":
								varAsignacion = tokensSintactico.get(0);
								break;
							}
						}
						
						switch(Integer.parseInt(tabla[x][y].substring(1,tabla[x][y].length())))
						{
						// int float char
						case 4: case 5: case 6:
							codigoObjeto += columnas[y]+" ";
							break;
							
						// variable que se asigna realmente
						case 28:
							if(Asignacion.getTipoDato().equals(""))
							{
								JOptionPane.showMessageDialog(null, "El identificador \"" + Asignacion.getComponente() + "\" no esta declarado" ,"ERROR SEMANTICO", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else
								if(Asignacion.getValor().equals("sin valor"))
								{
									JOptionPane.showMessageDialog(null, "El identificador \"" + Asignacion.getComponente() + "\" no puede ser utilizado\nporque no tiene un valor" ,"ERROR SEMANTICO", JOptionPane.ERROR_MESSAGE);
									error = true;
								}
								if(!pila.peek().equals("I19") && !pila.peek().equals("I38") && !ejecutandoPrint)
									codigoObjeto += Asignacion.getComponente();
							break;
							
						// variables que se van a asignar
						case 13: case 31:
							codigoObjeto += Asignacion.getComponente();
							break;
							
						// ,
						case 23:
							codigoObjeto += columnas[y];
							break;
							
						// ;
						case 22: case 32:
							codigoObjeto += columnas[y]+"\n\r";
							break;
							
						// variable a la que se hace la asignacion
						case 10:
							codigoObjeto += varAsignacion.getComponente();
							break;
							
						// =
						case 17:
							codigoObjeto += columnas[y];

							for(int i=0;i<tokensLexico.size();i++)
								if(tokensLexico.get(i).getComponente().equals(varAsignacion.getComponente()))
									tokensLexico.get(i).setValor(tokensSintactico.get(1).getValor());
							recargarTablaSimbolos();
							break;
							
						// + - * /
						case 33: case 34: case 35: case 36:
							if(!ejecutandoPrint)
								codigoObjeto += columnas[y];
							break;
							
						// if
						case 11:
						    codigoObjeto += "V1 = " + tokensSintactico.get(2).getComponente() + tokensSintactico.get(3).getComponente() + tokensSintactico.get(4).getComponente() + ";\n\rif(! V1)\n\r   goto else_" + et_else + ";\n\r";
							pilaEtiquetas.push("goto end_if_" + et_if + ";\n\relse_" + et_else++ + ":\n\r");
						    pilaEtFin.push("goto end_if_" + et_if + ";\n\rend_if_" + et_if++ + ":\n\r");
							pilaEstructura.push(columnas[y]);
							haySentencias = ejecutandoSentencias = true;
							break;
							
						// while
						case 12:
							codigoObjeto += "while_" + et_while + ":\n\rV1 = " +tokensSintactico.get(2).getComponente() + tokensSintactico.get(3).getComponente() + tokensSintactico.get(4).getComponente() + ";\n\rif(! V1)\n\r   goto end_while_" + et_while + ";\n\r";
							pilaEtFin.push("goto while_" + et_while + ";\n\rend_while_" + et_while++ + ":\n\r");
							pilaEstructura.push(columnas[y]);
							haySentencias = ejecutandoSentencias = true;
							break;
							
						// )
						case 50:
							ejecutandoSentencias = false;
							break;
							
						// } I55 llave cierre de else
						case 55: 
							codigoObjeto += pilaEtFin.pop();
							break;
							
						// } I56 llave cierre de if y while
						case 56:
							if(pilaEstructura.pop().equals("if"))
							{
								codigoObjeto += pilaEtiquetas.pop();
								if(!tokensSintactico.get(1).getComponente().equals("else"))
									codigoObjeto += pilaEtFin.pop();
							}
							else
								codigoObjeto += pilaEtFin.pop();
							break;
							
						// entero flotante caracter
						case 57: case 58: case 59:
							if(!pila.peek().equals("I19") && !pila.peek().equals("I38") && !ejecutandoPrint)
								codigoObjeto += tokensSintactico.get(0).getComponente();
							break;
							
						// print
						case 61:
							ejecutandoPrint = true;
							for(int i=2;!tokensSintactico.get(i).getComponente().equals(";");resPrint += tokensSintactico.get(i).getComponente(), i++);
							break;
							
						// identifador dentro de leer
						case 71:
							for(int i=0;i<tokensSintactico.size();i++)
								if(Asignacion.getComponente().equals(tokensSintactico.get(i).getComponente()))
									tokensSintactico.get(i).setValor("valor leido");

							for(int i=0;i<tokensLexico.size();i++)
								if(tokensLexico.get(i).getComponente().equals(Asignacion.getComponente()))
									tokensLexico.get(i).setValor("valor leido");
							recargarTablaSimbolos();
							break;
						}
						tokensSintactico.remove(0);
						pila.push(columnas[y]);
						pila.push(tabla[x][y]);
					}
					else
					{
						posProduccion = Integer.parseInt(tabla[x][y].substring(1, tabla[x][y].length()));

						switch(posProduccion)
						{
						case 22: case 30: case 31: case 32: // Lo que se le va a asignar
							if(!ejecutandoSentencias)
								switch(Asignacion.getTipoDato())
								{
								case "int":
									pilaSemantica.push(1);
									break;
								case "float":
									pilaSemantica.push(2);
									break;
								case "char":
									pilaSemantica.push(3);
									break;
								}
							break;
						case 15: case 16: case 18: case 19:
							if(pilaSemantica.size() >= 2)
							{
								int fila = pilaSemantica.pop();
								int columna = pilaSemantica.pop();
								pilaSemantica.push(mapeoTiposDatos[fila-1][columna-1]);
							}
							break;
							
						case 14: // La variable a la que sera asignado
							switch(varAsignacion.getTipoDato())
							{
							case "int":
								auxiliarMapeo = 1;
								break;
							case "float":
								auxiliarMapeo = 2;
								break;
							case "char":
								auxiliarMapeo = 3;
								break;
							}

							
							if((auxiliarMapeo == 1 && pilaSemantica.peek() == 2) || (auxiliarMapeo == 3 && pilaSemantica.peek() == 2))
							{
								JOptionPane.showMessageDialog(null, "No se puede asignar un tipo de dato " + tiposDatos[pilaSemantica.peek()-1] + " a un tipo de dato " + tiposDatos[auxiliarMapeo-1], "ERROR SEMANTICO", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else
								pilaSemantica.pop();
							break;
							
						case 33: // Obtiene el tipo de dato resultante en el print
							codigoObjeto += "printf(\"%" + obtenerFormato(pilaSemantica.pop()) + "\\n\"," + resPrint + ";\n\r";
							resPrint = "";
							ejecutandoPrint = false;
							break;
							
						case 36:
							codigoObjeto += "printf(" + resPrint + ";\n\r";
							resPrint = "";
							ejecutandoPrint = false;
							break;
						
						case 37:
							switch(Asignacion.getTipoDato())
							{
							case "int":
								auxiliarMapeo = 1;
								break;
							case "float":
								auxiliarMapeo = 2;
								break;
							case "char":
								auxiliarMapeo = 3;
								break;
							}
							codigoObjeto += "scanf(\"%" + obtenerFormato(auxiliarMapeo) + "\",&" + Asignacion.getComponente() + ");\n\r";
							break;
						}
						
						if(posProduccion != 0)
						{
							StringTokenizer staux = new StringTokenizer(producciones[posProduccion]);
							
							while(staux.hasMoreTokens())
							{
								pila.pop();
								pila.pop();
								staux.nextToken();
							}
							
							pila.push(NTProducciones[posProduccion]);
							pila.push(tabla[encuentraPosicionFila(pila.get(pila.size()-2))][encuentraPosicionColumna(pila.peek())]);
						}
					}
				}
				else
					error = true;
				} catch(NullPointerException e) {
					error = true;
				}
			}
			
			if(error)
				JOptionPane.showMessageDialog(null, "El proceso de compilacion ha fallado", "ERROR DE COMPILACION", JOptionPane.ERROR_MESSAGE);
			else
			{	
				r.ATobjeto.setText("#include<stdio.h>\n\rint main() {\n\r" + (haySentencias?"int V1;\n\r":"") + codigoObjeto + "  return 0;\n\r}");
				JOptionPane.showMessageDialog(null, "El proceso de compilacion fue satisfactorio", "", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	int posicionVariableQueNoExiste = 0;
	
	public int encuentraPosicionColumna(String simbolo)
	{
		for(int i=0;i<columnas.length;i++)
			if(simbolo.equals(columnas[i]))
				return i;
		return -1;
	}
	
	public int encuentraPosicionFila(String simbolo)
	{
		for(int i=0;i<filas.length;i++)
			if(simbolo.equals(filas[i]))
				return i;
		return -1;
	}
	
	public boolean esEstado(String simbolo)
	{
		for(int i=0;i<filas.length;i++)
			if(simbolo != null && simbolo.equals(filas[i]))
				return true;
		return false;
	}
	
	public boolean existenTodasLasVariables()
	{
		for(int i=0;i<tokensLexico.size();i++)
			if(tokensLexico.get(i).getTipoDato().equals(""))
			{
				for(int j=5;j<=7;j++)
					if(tokensLexico.get(i).getComponente().contains(simbolos[j]))
						return true;

						posicionVariableQueNoExiste = i;
						return false;
					
			}
		return true;
	}
	
	public void recargarTablaSimbolos()
	{
		r.modeloLexico.setRowCount(0);
		
		for(int i=0;i<tokensLexico.size();i++)
			r.modeloLexico.addRow(new Object[] {tokensLexico.get(i).getComponente(), tokensLexico.get(i).getTipo(), tokensLexico.get(i).getTipoDato(), tokensLexico.get(i).getValor()});
	}
	
	public String obtenerFormato(int tipo)
	{
		switch(tipo)
		{
		case 1:
			return "d";
			
		case 2:
			return "f";
			
		case 3:
			return "c";
		}
		return "";
	}
}