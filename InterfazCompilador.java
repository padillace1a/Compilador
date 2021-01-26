package Compilador;

/* EQUIPO 4
 * 
 * Alumnos:
 * Luis Fernando Sánchez Cazares - 17420613
 * Jose Antonio del Toro Padilla - 17420541
 * Cesar Alejandro Ochoa Garibay - 17420589
 * */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

class Teclado extends KeyAdapter {
	public static boolean CONTROL, V, X, BORRAR;
	
	private boolean[] keys = new boolean[256];
	private InterfazCompilador r;
	
	public Teclado(InterfazCompilador r)
	{
		CONTROL = V = X = BORRAR = false;
		this.r = r;
	}
	
	public void actualizar()
	{
		CONTROL = keys[KeyEvent.VK_CONTROL];
		V = keys[KeyEvent.VK_V];
		X = keys[KeyEvent.VK_X];
		BORRAR = keys[KeyEvent.VK_BACK_SPACE];
	}
	
	public void keyPressed(KeyEvent e)
	{	
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
			r.areaTextoNumeros.append(" " + (r.areaTexto.getLineCount()+1) + "\n");
		else
			if(e.getKeyCode() <= 255)
				keys[e.getKeyCode()] = true;
	}
	
	public void keyReleased(KeyEvent e)
	{
		if(e.getKeyCode() <= 255)
			keys[e.getKeyCode()] = false;
	}
}

public class InterfazCompilador extends JPanel implements Runnable {
	JFrame f;
	JScrollPane jsn, jsa, jsgeneobj;
	JTextArea areaTexto, areaTextoNumeros;
	JButton analizar, limpiar;
	AnalisisSintacticoYSemantico AnalisisSS;
	JScrollBar scrollAT;
	Teclado teclado;
	boolean estaCorriendo = false;
	Thread hilo;
	
	JScrollPane jsLexico;
	DefaultTableModel modeloLexico;
	JTextArea ATobjeto;
	
	String columnas[] = {"id","entero","flotante","caracter","cadena","leer","print","int","float","char","if","else","while",",",";","+","-","*","/","(",")","{","}","=","==","<",">","$","A","Tipo","B","C","D","E","F","G","H","I","J","K","L"};
	String simbolos[] = {"id","entero","flotante","caracter","cadena","leer","print","int","float","char","if","else","while",",",";","+","-","*","/","(",")","{","}","=","==","<",">","$"};
	int nfilas = 77;
	String filas[];
	String NTProducciones[] = {"A'","A","A","Tipo","Tipo","Tipo","B","B","C","C","C","C","C","C","D","E","E","E","F","F","F","G","G","H","H","I","I","I","J","K","G","G","G","L","C","C","L","L"};
	String producciones[] = {"A","Tipo id B","C","int","float","char","; A",", id B","D C","H C","K C","D","H","K","id = E ;","E + F","E - F","F","F * G","F / G","G","( E )","id","if J","if J else { C }","==","<",">","( G I G ) { C }","while J","entero","flotante","caracter","print ( E ) ;","L C","L","print ( cadena ) ;","leer ( id ) ;"};
	
	String mat[][] = 
	{{"I10",null ,null ,null ,null ,"I67","I61","I4" ,"I5" ,"I6" ,"I11",null ,"I12",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I1" ,"I2" ,null ,"I3" ,"I7" ,null ,null ,null ,"I8" ,null ,null ,"I9" ,"I60"},
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P0" ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I13",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P2" ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"P3" ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"P4" ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"P5" ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I10",null ,null ,null ,null ,"I67","I61",null ,null ,null ,"I11",null ,"I12",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P11",null ,null ,null ,null ,"P11",null ,null ,null ,"I14","I7" ,null ,null ,null ,"I8" ,null ,null ,"I9" ,"I60"},
     {"I10",null ,null ,null ,null ,"I67","I61",null ,null ,null ,"I11",null ,"I12",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P12",null ,null ,null ,null ,"P12",null ,null ,null ,"I14","I7" ,null ,null ,null ,"I8" ,null ,null ,"I9" ,"I60"},
     {"I10",null ,null ,null ,null ,"I67","I61",null ,null ,null ,"I11",null ,"I12",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P13",null ,null ,null ,null ,"P13",null ,null ,null ,"I14","I7" ,null ,null ,null ,"I8" ,null ,null ,"I9" ,"I60"},
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I17",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I19",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I18",null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I19",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I20",null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I23","I22",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I21",null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P8" ,null ,null ,null ,null ,"P8" ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P9" ,null ,null ,null ,null ,"P9" ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P10",null ,null ,null ,null ,"P10",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I28","I57","I58","I59",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I27",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I24","I25","I26",null ,null ,null ,null ,null },
     {"P23",null ,null ,null ,null ,"P23","P23",null ,null ,null ,"P23","I49","P23",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P23",null ,null ,null ,null ,"P23",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I28","I57","I58","I59",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I29",null ,null ,null ,null ,null },
     {"P29",null ,null ,null ,null ,"P29","P29",null ,null ,null ,"P29",null ,"P29",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P29",null ,null ,null ,null ,"P29",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P1" ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I10",null ,null ,null ,null ,"I67","I61","I4" ,"I5" ,"I6" ,"I11",null ,"I12",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I30","I2" ,null ,"I3" ,"I7" ,null ,null ,null ,"I8" ,null ,null ,"I9" ,"I60"},
     {"I31",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I32","I33","I34",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P17","P17","P17","I35","I36",null ,"P17",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P20","P20","P20","P20","P20",null ,"P20",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I28","I57","I58","I59",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I27",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I37","I25","I26",null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P22","P22","P22","P22","P22",null ,"P22",null ,null ,null ,"P22","P22","P22",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I39","I40","I41",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I38",null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P6" ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I23","I22",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I42",null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"P14",null ,null ,null ,null ,"P14","P14",null ,null ,null ,"P14",null ,"P14",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P14",null ,null ,null ,null ,"P14",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I28","I57","I58","I59",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I27",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I43","I26",null ,null ,null ,null ,null },
     {"I28","I57","I58","I59",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I27",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I44","I26",null ,null ,null ,null ,null },
     {"I28","I57","I58","I59",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I27",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I45",null ,null ,null ,null ,null },
     {"I28","I57","I58","I59",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I27",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I46",null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I33","I34",null ,null ,null ,"I47",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I28","I57","I58","I59",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I48",null ,null ,null ,null ,null },
     {"P25","P25","P25","P25",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P25",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"P26","P26","P26","P26",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P26",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"P27","P27","P27","P27",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P27",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P7" ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P15","P15","P15","I35","I36",null ,"P15",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P16","P16","P16","I35","I36",null ,"P16",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P18","P18","P18","P18","P18",null ,"P18",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P19","P19","P19","P19","P19",null ,"P19",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P21","P21","P21","P21","P21",null ,"P21",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I50",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I51",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I52",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I10",null ,null ,null ,null ,"I67","I61",null ,null ,null ,"I11",null ,"I12",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I53","I7" ,null ,null ,null ,"I8" ,null ,null ,"I9" ,"I60"},
     {"I10",null ,null ,null ,null ,"I67","I61",null ,null ,null ,"I11",null ,"I12",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I54","I7" ,null ,null ,null ,"I8" ,null ,null ,"I9" ,"I60"},
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I55",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I56",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"P24",null ,null ,null ,null ,"P24","P24",null ,null ,null ,"P24",null ,"P24",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P24",null ,null ,null ,null ,"P24",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"P28",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P28","P28","P28",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P28",null ,null ,null ,null ,"P28",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P30","P30","P30","P30","P30",null ,"P30",null ,null ,null ,"P30","P30","P30",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P31","P31","P31","P31","P31",null ,"P31",null ,null ,null ,"P31","P31","P31",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P32","P32","P32","P32","P32",null ,"P32",null ,null ,null ,"P32","P32","P32",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I10",null ,null ,null ,null ,"I67","I61",null ,null ,null ,"I11",null ,"I12",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P35",null ,null ,null ,null ,"P35",null ,null ,null ,"I62","I7" ,null ,null ,null ,"I8" ,null ,null ,"I9" ,"I60"},
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I63",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"P34",null ,null ,null ,null ,"P34",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I28","I57","I58","I59","I68",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I27",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I64","I25","I26",null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I33","I34",null ,null ,null ,"I65",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I66",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"P33",null ,null ,null ,null ,"P33","P33",null ,null ,null ,"P33",null ,"P33",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P33",null ,null ,null ,null ,"P33",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I69",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I70",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"I71",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I72",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I73",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"P36",null ,null ,null ,null ,"P36","P36",null ,null ,null ,"P36",null ,"P36",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P36",null ,null ,null ,null ,"P36",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,"I74",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },
     {"P37",null ,null ,null ,null ,"P37","P37",null ,null ,null ,"P37",null ,"P37",null ,null ,null ,null ,null ,null ,null ,null ,null ,"P37",null ,null ,null ,null ,"P37",null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null ,null },};
	
	public InterfazCompilador()
	{
		teclado = new Teclado(this);
		hilo = new Thread(this);
		hilo.start();
		estaCorriendo = true;
		f = new JFrame("Compilador - Equipo 4");
		f.setResizable(false);
		f.setLocationRelativeTo(null);
		inicializarComponentes();
		f.setVisible(true);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void setFilas(int nfilas)
	{
		filas = new String[nfilas];
		
		for(int i=0;i<nfilas;i++)
			filas[i] = "I" + i;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
	}
	
	public void run()
	{
		JTextArea recurso = new JTextArea();
		while(estaCorriendo)
		{
			teclado.actualizar();
			if(Teclado.BORRAR || Teclado.CONTROL && (Teclado.V || Teclado.X))
			{
				recurso.setText("");
				for(int i=0;i<areaTexto.getLineCount();i++)
					recurso.append(" " + (i+1) + "\n");
				areaTextoNumeros.setText(recurso.getText());
			}
			repaint();
		}
	}
	
	public void inicializarComponentes()
	{
		setLayout(null);
		setBackground(new Color(44, 123, 255));
		modeloLexico = new DefaultTableModel();
		modeloLexico.addColumn("Componente");
		modeloLexico.addColumn("Tipo");
		modeloLexico.addColumn("Tipo Dato");
		modeloLexico.addColumn("Valor");
		
		jsLexico = new JScrollPane(new JTable(modeloLexico));
		jsLexico.setBounds(770, 10, 580, 300);
		
		areaTexto = new JTextArea();
		areaTexto.setFont(new Font("Arial",Font.PLAIN,18));
		areaTexto.addKeyListener(teclado);
		
		jsa = new JScrollPane(areaTexto);
		jsa.setBounds(60, 10, 700, 720);
		
		areaTextoNumeros = new JTextArea();
		areaTextoNumeros.setFont(new Font("Arial",Font.PLAIN,18));
		areaTextoNumeros.setEditable(false);
		areaTextoNumeros.append(" 1\n");
		jsn = new JScrollPane(areaTextoNumeros);
		jsn.setBounds(10, 10, 50, 720);
		
		scrollAT = new JScrollBar();
		scrollAT.setUnitIncrement(30);
		
		jsn.setVerticalScrollBar(scrollAT);
		jsa.setVerticalScrollBar(scrollAT);
		
		setFilas(nfilas);
		AnalisisSS = new AnalisisSintacticoYSemantico(filas, columnas, mat, NTProducciones, producciones, this, simbolos);
		
		analizar = new JButton("Analizar");
		analizar.setBounds(830, 670, 200, 50);
		analizar.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				ATobjeto.setText("");
				modeloLexico.setRowCount(0);
				AnalisisSS.establecerCadena(areaTexto.getText());
			}
		});
		
		limpiar = new JButton("Limpiar");
		limpiar.setBounds(1090, 670, 200, 50);
		limpiar.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				modeloLexico.setRowCount(0);
				areaTextoNumeros.setText(" 1\n");
				areaTexto.setText("");
				ATobjeto.setText("");
			}
		});
		
		ATobjeto = new JTextArea();
		jsgeneobj = new JScrollPane(ATobjeto);
		jsgeneobj.setBounds(770, 350, 580, 300);
		
		add(jsgeneobj);
		
		JPanel panelCodObj = new JPanel();
		panelCodObj.setBounds(770, 320, 580, 100);
		JLabel tituloCodObj = new JLabel("Codigo Objeto", SwingConstants.CENTER);
		panelCodObj.add(tituloCodObj);
		add(panelCodObj);
		
		add(jsLexico);
		add(jsa);
		add(jsn);
		add(analizar);
		add(limpiar);
		f.add(this, BorderLayout.CENTER);
	}
	
	public static void main(String[] args) {
		new InterfazCompilador();
	}
}
